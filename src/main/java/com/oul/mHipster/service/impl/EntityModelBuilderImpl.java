package com.oul.mHipster.service.impl;

import com.oul.mHipster.layersConfig.Layer;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.*;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.EntityModelBuilder;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.oul.mHipster.service.JavaFileMakerService;
import com.oul.mHipster.util.ClassUtils;
import com.squareup.javapoet.TypeSpec;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityModelBuilderImpl implements EntityModelBuilder {

    private LayersConfig layersConfig;
    private JavaFileMakerService javaFileMakerService;
    private final GenerateLayerStrategyFactory generateLayerStrategyFactory = new GenerateLayerStrategyFactory();

    public EntityModelBuilderImpl(LayersConfig layersConfig) {
        this.layersConfig = layersConfig;
        this.javaFileMakerService = new JavaFileMakerServiceImpl();
    }

    @Override
    public Entity mapSourceToEntity(Class<?> clazz) {
        Entity.Builder builder = Entity.builder();
        builder.infoFields(clazz);

        Field[] fields = clazz.getDeclaredFields();
        builder.attributes(Arrays.stream(fields).map(field -> findRelation(field, clazz)).collect(Collectors.toList()));

        return builder.build();
    }

    private Attribute findRelation(Field field, Class clazz) {
        Annotation annM2M = field.getAnnotation(ManyToMany.class);
        Annotation annO2M = field.getAnnotation(OneToMany.class);
        Annotation annO2O = field.getAnnotation(OneToOne.class);
        Annotation annM2O = field.getAnnotation(ManyToOne.class);
        //onaj drugi case:
        //manytoone owner je uvek onaj drugi
        //ako ima mappedBy owner je onaj drugi

        //ti si owner:
        //kad ti je mappedBy prazan i nisi manytoone
        return Stream.of(annM2M, annO2M, annO2O, annM2O).filter(Objects::nonNull)
                .map(annotation -> resolveRelation(annotation, field, clazz))
                .findFirst()
                .orElse(new Attribute(field.getType(), field.getName()));
    }

    private Attribute resolveRelation(Annotation annotation, Field field, Class clazz) {
        Class<? extends Annotation> type = annotation.annotationType();

        for (Method method : type.getDeclaredMethods()) {
            Object value = null;
            try {
                value = method.invoke(annotation, (Object[]) null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            if (method.getName().equals("mappedBy") && !value.equals("")) {
                ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                Class<?> relationDomainClass = (Class<?>) genericType.getActualTypeArguments()[0];
                return new RelationAttribute(field.getType(), field.getName(), relationDomainClass.getSimpleName(),
                        RelationType.valueOf(ClassUtils.getClassName(type).toUpperCase()));
            } else if (method.getName().equals("mappedBy") && value.equals("")) {
                return new RelationAttribute(field.getType(), field.getName(), clazz.getSimpleName(),
                        RelationType.valueOf(ClassUtils.getClassName(type).toUpperCase()));
            }
        }
        return new RelationAttribute(field.getType(), field.getName(), clazz.getSimpleName(), RelationType.MANYTOONE);
    }


    @Override
    public void buildLayers(SourceDomainLayer sourceDomainLayer) {
        sourceDomainLayer.getEntities().forEach(entity -> {
            Map<String, ClassNamingInfo> layersMap = buildLayerClass(entity, sourceDomainLayer);
            entity.setLayers(layersMap);
        });

        EntityManagerFactory entityManagerFactory = EntityManagerFactoryImpl.getInstance();
        entityManagerFactory.createEntityManager(sourceDomainLayer);

        sourceDomainLayer.getEntities().forEach(entityModel -> layersConfig.getLayers().forEach(layer -> {
            GenerateLayerStrategy generateLayerStrategy = generateLayerStrategyFactory.getLayerStrategy(LayerName.valueOf(layer.getName()));
            TypeSpec typeSpec = generateLayerStrategy.generate(entityModel);
            entityModel.setTypeSpec(typeSpec);
            if(layer.getName().equals(LayerName.SERVICE_IMPL.toString())) System.out.println(typeSpec);
        }));

        javaFileMakerService.makeJavaFiles(sourceDomainLayer);
    }

    @Override
    public Map<String, ClassNamingInfo> buildLayerClass(Entity entity, SourceDomainLayer sourceDomainLayer) {
        return layersConfig.getLayers().stream().collect(Collectors.toMap(Layer::getName, layer -> {
            String className = entity.getClassName() + layer.getNamingSuffix();
            String instanceName = ClassUtils.instanceNameBuilder(className);
            String packageName = sourceDomainLayer.getRootPackageName() + "." + layer.getPackageName();
            return new ClassNamingInfo(className, instanceName, packageName);
        }));
    }

}

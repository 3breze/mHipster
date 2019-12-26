package com.oul.mHipster.service.impl;

import com.oul.mHipster.layersConfig.Layer;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.*;
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
import java.util.*;
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
        builder.attributes(Arrays.stream(fields).map(this::findRelation).collect(Collectors.toList()));

        return builder.build();
    }

    private Attribute findRelation(Field field) {
        Annotation annM2M = field.getAnnotation(ManyToMany.class);
        if (annM2M != null) {
            Class<? extends Annotation> type = annM2M.annotationType();
            System.out.println("Values of " + type.getName());

            for (Method method : type.getDeclaredMethods()) {
                Object value = null;
                try {
                    value = method.invoke(annM2M, (Object[]) null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                System.out.println(" " + method.getName() + ": " + value);
            }
            return new RelationAttribute(field.getType(), field.getName(), field.getType().toString(), RelationType.MANYTOMANY);
        }
        List<Annotation> annotations = new ArrayList<>();
        annotations.add(field.getAnnotation(ManyToOne.class));
        annotations.add(field.getAnnotation(OneToMany.class));
        annotations.add(field.getAnnotation(OneToOne.class));
        for (Annotation annotation : annotations) {
            if (annotation != null) {
                Class<? extends Annotation> type = annotation.annotationType();
                System.out.println(">>> " + type.getSimpleName());
                return new RelationAttribute(field.getType(), field.getName(),
                        field.getType().toString(), RelationType.valueOf(type.getSimpleName().toUpperCase()));
            }
        }
        return new Attribute(field.getType(), field.getName());
    }


    @Override
    public void buildLayers(SourceDomainLayer sourceDomainLayer) {
        sourceDomainLayer.getEntities().forEach(entity -> {
            Map<String, LayerClass> layersMap = buildLayerClass(entity, sourceDomainLayer);
            entity.setLayers(layersMap);
        });

        sourceDomainLayer.getEntities().forEach(entityModel -> layersConfig.getLayers().forEach(layer -> {
            GenerateLayerStrategy generateLayerStrategy = generateLayerStrategyFactory.getLayerStrategy(LayerName.valueOf(layer.getName()));
            TypeSpec typeSpec = generateLayerStrategy.generate(entityModel);
            entityModel.setTypeSpec(typeSpec);
        }));

        javaFileMakerService.makeJavaFiles(sourceDomainLayer);
    }

    @Override
    public Map<String, LayerClass> buildLayerClass(Entity entity, SourceDomainLayer sourceDomainLayer) {
        return layersConfig.getLayers().stream().collect(Collectors.toMap(Layer::getName, layer -> {
            String className = entity.getClassName() + layer.getNamingSuffix();
            String instanceName = ClassUtils.instanceNameBuilder(className);
            String packageName = sourceDomainLayer.getRootPackageName() + "." + layer.getPackageName();
            return new LayerClass(className, instanceName, packageName);
        }));
    }

}

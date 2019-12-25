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

        Map<Boolean, List<Field>> isRelationAttributeMap = Arrays.stream(fields).collect(Collectors.partitioningBy(this::isRelation));
        builder.attributes(isRelationAttributeMap.get(false).stream().map(field ->
                new Attribute(field.getType(), field.getName())).collect(Collectors.toList()));
        builder.relationAttributes(isRelationAttributeMap.get(true).stream().map(field ->
                new RelationAttribute(field.getType(), field.getName(), field.getType().toString(), "MMM"))
                .collect(Collectors.toList()));

        return builder.build();
    }

    private Boolean isRelation(Field field) {
        Annotation annM2O = field.getAnnotation(ManyToOne.class);
        Annotation annM2M = field.getAnnotation(ManyToMany.class);
        Annotation annO2M = field.getAnnotation(OneToMany.class);
        Annotation annO2O = field.getAnnotation(OneToOne.class);
        return (Stream.of(annM2M, annM2O, annO2M, annO2O).anyMatch(Objects::nonNull));
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

package com.oul.mHipster.service.impl;

import com.oul.mHipster.layersConfig.Layer;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.Attribute;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.LayerClass;
import com.oul.mHipster.model.SourceDomainLayer;
import com.oul.mHipster.service.EntityModelBuilder;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.oul.mHipster.service.JavaFileMakerService;
import com.oul.mHipster.util.ClassUtils;
import com.squareup.javapoet.TypeSpec;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<Attribute> attributes = new ArrayList<>();
        for (Field field : fields) {
//            Annotation annM2O = field.getAnnotation(ManyToOne.class);
////            Annotation annM2M = field.getAnnotation(ManyToMany.class);
////            if (annM2O != null) {
////                System.out.println("Logika za m2o");
////                RelationAttribute attr = new RelationAttribute();
////                attributes.add(attr);
////                continue;
////            }
////            if (annM2M != null) {
////                System.out.println("Logika za m2m");
////                RelationAttribute attr = new RelationAttribute();
////                attributes.add(attr);
////                continue;
////            }
            Attribute attr = new Attribute();
            attr.setType(field.getType());
            attr.setValue(field.getName());
//            System.out.println("attr = " + attr.getValue() + " - " + attr.getType());
            attributes.add(attr);
        }
        builder.attributes(attributes);
        return builder.build();
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

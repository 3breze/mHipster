package com.oul.mHipster.service;

import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.Attribute;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.todelete.OldShitModel;
import com.oul.mHipster.util.Util;
import com.squareup.javapoet.TypeSpec;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.oul.mHipster.layersConfig.enums.LayerName.*;

public class EntityModelBuilderService {

    private LayersConfig layersConfig;
    private JavaFileMakerService javaFileMakerService;
    private final GenerateLayerStrategyFactory generateLayerStrategyFactory = new GenerateLayerStrategyFactory();

    public EntityModelBuilderService(LayersConfig layersConfig) {
        this.layersConfig = layersConfig;
        this.javaFileMakerService = new JavaFileMakerService();
    }

    public Entity entityMapper(Class<?> clazz) {
        Entity.Builder builder = Entity.builder();
        builder.infoFields(clazz);

        Field[] fields = clazz.getDeclaredFields();
        List<Attribute> attributes = new ArrayList<>();
        for (Field field : fields) {
            Annotation annM2O = field.getAnnotation(ManyToOne.class);
            Annotation annM2M = field.getAnnotation(ManyToMany.class);
            if (annM2O != null) {
                System.out.println("Logika za m2o");
                RelationAttribute attr = new RelationAttribute();
                attributes.add(attr);
                continue;
            }
            if (annM2M != null) {
                System.out.println("Logika za m2m");
                RelationAttribute attr = new RelationAttribute();
                attributes.add(attr);
                continue;
            }
            Attribute attr = new Attribute();
            attr.setType(field.getType());
            attr.setValue(field.getName());
            attributes.add(attr);
        }
        builder.attributes(attributes);
        return builder.build();
    }

    public void buildLayers(List<Entity> entityModelList) {

        List<OldShitModel> oldShitModelList = sourceDomainLayer.getEntities().stream().map(entity ->
                new OldShitModel.EntityModelBuilder().classAndInstanceName(entity.getClassName())
                        .requestClassAndInstanceName(domainSpecificLayersMap.get(REQUEST_DTO).getNamingSuffix())
                        .responseClassAndInstanceName(domainSpecificLayersMap.get(RESPONSE_DTO).getNamingSuffix())
                        .packageName(Util.getValue(DOMAIN))
                        .entity(entity)
                        .build())
                .collect(Collectors.toList());

        entityModelList.forEach(entityModel -> layersConfig.getLayers().forEach(layer -> {
            GenerateLayerStrategy generateLayerStrategy = generateLayerStrategyFactory.getLayerStrategy(LayerName.valueOf(layer.getName()));
            TypeSpec typeSpec = generateLayerStrategy.generate(entityModel);
            entityModel.setTypeSpec(typeSpec);
        }));

        javaFileMakerService.makeJavaFiles(oldShitModelList);
    }

}

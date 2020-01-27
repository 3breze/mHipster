package com.oul.mHipster.service.helper.impl;

import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.EntityManagerService;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

public class AttributeBuilderService extends RelationAttributeService {

    private EntityManagerService entityManagerService;

    public AttributeBuilderService() {
        this.entityManagerService = EntityManagerFactory.getInstance();
    }

    /**
     * Na top nivou ide provera da li je generic polje, ako jeste radim split, pa za genericType i typeArgument ide
     * dalja provera -> da li je u modelu (key-evi: classNames ili dependencies) ili je obican field (ne treba nam
     * typeArgument kao u slucaju parametara methoda)
     */
    public FieldTypeNameWrapper getReturnTypeName(String entityName, String typeArgument, String instanceName) {
        return typeArgument.contains("<") ? parameterizedTypeTokenSplit(typeArgument, entityName, instanceName) :
                entityManagerService.getProperty(entityName, typeArgument, instanceName);
    }

    /**
     * Eg. splitujemo "Page&lt;responseClazz&gt;"
     * ParameterizedTypeName vraca typeName
     */
    private FieldTypeNameWrapper parameterizedTypeTokenSplit(String genericField, String entityName, String instanceName) {
        FieldTypeNameWrapper genericType = entityManagerService.getProperty(entityName, genericField.substring(0, genericField.indexOf("<")),
                null);
        FieldTypeNameWrapper typeArgument = entityManagerService.getProperty(entityName, genericField.substring(genericField.indexOf("<") + 1,
                genericField.indexOf(">")), null);

        TypeName typeName = ParameterizedTypeName.get((ClassName) genericType.getTypeName(),
                typeArgument.getTypeName());
        return new FieldTypeNameWrapper(typeName, instanceName);
    }

    public List<FieldSpec> getFieldSpecList(Entity entity, String layer) {

        TypeName updateValidationGroupTypeName = ClassName.get("com.whatever.whatever.ValidationGroup",
                "Update");

        return entity.getAttributes().stream().map(attribute -> {
            FieldSpec.Builder fieldBuilder = FieldSpec
                    .builder(attribute.getType(), attribute.getFieldName()).addModifiers(Modifier.PRIVATE);
            if (layer.equals(LayerName.REQUEST_DTO.name()) && attribute.getFieldName().equals("id")) {
                fieldBuilder.addAnnotation(AnnotationSpec
                        .builder(NotNull.class)
                        .addMember("groups", "{ $T.$L }", updateValidationGroupTypeName, "class")
                        .addMember("message", "$S", attribute.getFieldName() + " cannot be null.")
                        .build());
            }
            return fieldBuilder.build();
        }).collect(Collectors.toList());
    }
}

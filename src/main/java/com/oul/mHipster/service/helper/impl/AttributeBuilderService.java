package com.oul.mHipster.service.helper.impl;

import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.EntityManagerService;
import com.oul.mHipster.util.ClassUtils;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

public class AttributeBuilderService extends RelationAttributeService {

    private EntityManagerService entityManagerService;
    private static final String TYPE_ARG_START = "<";


    public AttributeBuilderService() {
        this.entityManagerService = EntityManagerFactory.getInstance();
    }

    /**
     * Na top nivou ide provera da li je generic polje, ako jeste radim split, pa za genericType i typeArgument ide
     * dalja provera -> da li je u modelu (key-evi: classNames ili dependencies) ili je obican field (ne treba nam
     * typeArgument kao u slucaju parametara methoda)
     */
    public FieldTypeNameWrapper getReturnTypeName(String entityName, String typeArgument, String instanceName) {
        return typeArgument.contains(TYPE_ARG_START) ? parameterizedTypeTokenSplit(typeArgument, entityName, instanceName) :
                entityManagerService.getProperty(entityName, typeArgument, instanceName);
    }

    /**
     * Eg. splitujemo "Page&lt;responseClazz&gt;"
     * ParameterizedTypeName vraca typeName
     */
    private FieldTypeNameWrapper parameterizedTypeTokenSplit(String genericField, String entityName, String instanceName) {
        String genericTypeName = ClassUtils.getGenericTypeName(genericField);
        String typeArgumentName = ClassUtils.getTypeArgumentName(genericField);

        FieldTypeNameWrapper genericType = entityManagerService.getProperty(entityName, genericTypeName, null);
        FieldTypeNameWrapper typeArgument = entityManagerService.getProperty(entityName, typeArgumentName, null);

        TypeName typeName = ParameterizedTypeName.get((ClassName) genericType.getTypeName(),
                typeArgument.getTypeName());
        return new FieldTypeNameWrapper(typeName, instanceName);
    }

    public List<FieldSpec> getFieldSpecList(Entity entity, String layer) {

        FieldTypeNameWrapper validationGroupTypeNameWrapper = entityManagerService.getProperty("dependencies",
                "ValidationGroupUpdate", "update");

        return entity.getAttributes().stream()
                .filter(RelationAttribute.class::isInstance)
                .map(attribute -> {
                    System.out.println(attribute);
                    FieldTypeNameWrapper fieldSpec = entityManagerService.getProperty(entity.getClassName(),
                            ((RelationAttribute) attribute).getClassSimpleName(), attribute.getFieldName());
                    TypeName parameterized = ParameterizedTypeName.get(ClassName.bestGuess(attribute.getType().toString()),
                            fieldSpec.getTypeName());

                    FieldSpec.Builder fieldBuilder = FieldSpec
                            .builder(parameterized, fieldSpec.getInstanceName()).addModifiers(Modifier.PRIVATE);

                    if (attribute.getFieldName().equals("id") && layer.equals(LayerName.REQUEST_DTO.name())) {
                        fieldBuilder.addAnnotation(AnnotationSpec
                                .builder(NotNull.class)
                                .addMember("groups", "{ $T.$L }", validationGroupTypeNameWrapper.getTypeName(), "class")
                                .addMember("message", "$S", attribute.getFieldName() + " cannot be null.")
                                .build());
                    }
                    return fieldBuilder.build();
                }).collect(Collectors.toList());
    }
}

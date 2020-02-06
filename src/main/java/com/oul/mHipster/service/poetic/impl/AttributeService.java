package com.oul.mHipster.service.poetic.impl;

import com.oul.mHipster.layerconfig.enums.LayerName;
import com.oul.mHipster.model.Attribute;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.model.wrapper.TypeWrapper;
import com.oul.mHipster.util.ClassUtils;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AttributeService extends RelationAttributeService {

    /**
     * Na top nivou ide provera da li je generic polje, ako jeste radim split, pa za genericType i typeArgument ide
     * dalja provera -> da li je u modelu (key-evi: classNames ili dependencies) ili je obican field (ne treba nam
     * typeArgument kao u slucaju parametara methoda)
     */
    public TypeWrapper getTypeName(String entityName, String typeArgument, String instanceName) {
        return ClassUtils.isParameterizedField(typeArgument) ? parameterizedTypeTokenSplit(typeArgument, entityName, instanceName) :
                entityManagerService.getProperty(entityName, typeArgument, instanceName);
    }

    /**
     * Eg. splitujemo "Page&lt;responseClazz&gt;"
     * ParameterizedTypeName vraca typeName
     */
    private TypeWrapper parameterizedTypeTokenSplit(String genericField, String entityName, String instanceName) {
        String genericTypeName = ClassUtils.getGenericTypeName(genericField);
        String typeArgumentName = ClassUtils.getTypeArgumentName(genericField);

        TypeWrapper genericType = entityManagerService.getProperty(entityName, genericTypeName, null);
        TypeWrapper typeArgument = entityManagerService.getProperty(entityName, typeArgumentName, null);

        TypeName typeName = ParameterizedTypeName.get((ClassName) genericType.getTypeName(),
                typeArgument.getTypeName());
        return new TypeWrapper(typeName, instanceName);
    }

    public List<FieldSpec> getAttributeFieldSpecList(Entity entity, LayerName layerName) {
        TypeWrapper validationGroupTypeNameWrapper = entityManagerService.getProperty("dependencies",
                "ValidationGroupUpdate", "update");

        Predicate<Attribute> predicate = RelationAttribute.class::isInstance;
        return entity.getAttributes().stream()
                .filter(predicate.negate())
                .map(attribute -> {
                    FieldSpec.Builder fieldSpecBuilder = FieldSpec.
                            builder(attribute.getType(), attribute.getFieldName());
                    if (attribute.getFieldName().equals("id") && layerName.equals(LayerName.REQUEST_DTO)) {
                        fieldSpecBuilder.addAnnotation(AnnotationSpec
                                .builder(NotNull.class)
                                .addMember("groups", "{ $T.$L }", validationGroupTypeNameWrapper.getTypeName(), "class")
                                .addMember("message", "$S", attribute.getFieldName() + " cannot be null.")
                                .build());
                    }
                    return fieldSpecBuilder
                            .addModifiers(Modifier.PRIVATE)
                            .build();
                }).collect(Collectors.toList());
    }
}

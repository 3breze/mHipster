package com.oul.mHipster.service.helper.impl;

import com.oul.mHipster.model.wrapper.FieldTypeNameWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.EntityManagerService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

public class AttributeBuilderService extends RelationAttributeService {

    private EntityManagerService entityManagerService;

    public AttributeBuilderService() {
        this.entityManagerService = EntityManagerFactory.getInstance();
    }

    /**
     * Na top nivou ide provera da li je generic polje, ako jeste radim split, pa za genericType i typeArgument ide
     * dalja provera -> da li je u modelu (key-evi: classNames ili dependencies) ili je obican field (ne treba nam
     * fieldName kao u slucaju parametara methoda)
     */
    public TypeName getReturnTypeName(String entityName, String fieldName) {
        return fieldName.contains("<") ? parameterizedTypeTokenSplit(fieldName, entityName) :
                entityManagerService.getProperty(entityName, fieldName, null).getTypeName();
    }

    /**
     * Eg. splitujemo "Page&lt;responseClazz&gt;"
     * ParameterizedTypeName vraca typeName
     */
    private TypeName parameterizedTypeTokenSplit(String genericField, String entityName) {
        FieldTypeNameWrapper genericType = entityManagerService.getProperty(entityName, genericField.substring(0, genericField.indexOf("<")),
                null);
        FieldTypeNameWrapper typeArgument = entityManagerService.getProperty(entityName, genericField.substring(genericField.indexOf("<") + 1,
                genericField.indexOf(">")), null);

        return ParameterizedTypeName.get((ClassName) genericType.getTypeName(),
                typeArgument.getTypeName());
    }
}

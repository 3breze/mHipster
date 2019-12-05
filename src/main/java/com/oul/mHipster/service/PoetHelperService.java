package com.oul.mHipster.service;

import com.oul.mHipster.domainApp.Attribute;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

public class PoetHelperService {
    private MethodSpec buildGetter(Attribute attribute) {
        String fieldName = attribute.getValue();
        String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1).toLowerCase();
        String packageName = String.join(".", entityBuilderConfig.getGroupName(), entityBuilderConfig.getArtifactName(), "domain");
        TypeName retType = ClassName.get(packageName, fieldName);
        return MethodSpec.methodBuilder(getterName).returns(retType).build();
    }
}

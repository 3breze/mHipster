package com.oul.mHipster.service;

import com.oul.mHipster.domainApp.Attribute;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

public class PoetHelperService {


    public MethodSpec buildGetter(Attribute attribute) {
        String fieldName = attribute.getValue();
        String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        return MethodSpec.methodBuilder(getterName).returns(ClassName.bestGuess(attribute.getType())).addModifiers(Modifier.PUBLIC).build();
    }

    public String buildGetterName(String field){
        return "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
    }

}

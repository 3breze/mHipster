package com.oul.mHipster.service;

import com.oul.mHipster.Util;
import com.oul.mHipster.domainApp.Attribute;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;

public class PoetHelperService {


    public MethodSpec buildGetter(Attribute attribute) {
        String fieldName = attribute.getValue();
        String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        TypeName retType = null;
        if (Util.getValue(attribute.getType()) == null) {
            retType = ClassName.get(String.class);
        } else {
            String packageName = Util.getValue("domain");
            retType = ClassName.get(packageName, attribute.getType());
        }
        return MethodSpec.methodBuilder(getterName).returns(retType).addModifiers(Modifier.PUBLIC).build();
    }

}

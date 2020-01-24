package com.oul.mHipster.service;

import com.oul.mHipster.layersConfig.Method;
import com.oul.mHipster.model.Entity;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;

import java.util.List;

public interface MethodBuilderHelperService {

    CodeBlock processMethodBody(Entity entity, String methodBody);

    List<ParameterSpec> resolveMethodParameters(Entity entity, Method method);

}

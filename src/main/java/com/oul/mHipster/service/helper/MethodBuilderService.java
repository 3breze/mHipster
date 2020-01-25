package com.oul.mHipster.service.helper;

import com.oul.mHipster.layersConfig.Method;
import com.oul.mHipster.layersConfig.Parameter;
import com.oul.mHipster.model.Entity;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;

import java.util.List;

public interface MethodBuilderService {

    CodeBlock processMethodBody(Entity entity, String methodBody);

    List<ParameterSpec> getMethodParameters(Entity entity, Method method, String layer);

    String getRequestMethod(String method);

    ParameterSpec.Builder processMethodSignature(Entity entity, Method method, Parameter parameter,
                                                 ParameterSpec.Builder parameterBuilder);

}

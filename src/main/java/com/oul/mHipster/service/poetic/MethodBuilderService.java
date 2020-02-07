package com.oul.mHipster.service.poetic;

import com.oul.mHipster.layerconfig.Method;
import com.oul.mHipster.layerconfig.Parameter;
import com.oul.mHipster.model.Entity;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;

import java.util.List;

public interface MethodBuilderService {

    CodeBlock processMethodBody(Entity entity, Method method);

    List<ParameterSpec> getMethodParameters(Entity entity, Method method, String layer);

    String getRequestMethod(String method);

    ParameterSpec.Builder processMethodSignature(Entity entity, Method method, Parameter parameter,
                                                 ParameterSpec.Builder parameterBuilder);

}

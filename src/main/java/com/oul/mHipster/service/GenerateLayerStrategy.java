package com.oul.mHipster.service;

import com.oul.mHipster.model.Entity;
import com.oul.mHipster.service.helper.MethodBuilderService;
import com.oul.mHipster.service.helper.impl.MethodBuilderServiceImpl;
import com.squareup.javapoet.TypeSpec;

public interface GenerateLayerStrategy {
    public TypeSpec generate(Entity entity);

}

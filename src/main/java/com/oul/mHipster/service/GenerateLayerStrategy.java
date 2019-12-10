package com.oul.mHipster.service;

import com.oul.mHipster.domain.EntityModel;
import com.squareup.javapoet.TypeSpec;

public interface GenerateLayerStrategy {
    public TypeSpec generate(EntityModel entityModel);
}

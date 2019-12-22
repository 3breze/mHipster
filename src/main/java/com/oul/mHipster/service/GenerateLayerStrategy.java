package com.oul.mHipster.service;

import com.oul.mHipster.todelete.OldShitModel;
import com.squareup.javapoet.TypeSpec;

public interface GenerateLayerStrategy {
    public TypeSpec generate(OldShitModel oldShitModel);
}

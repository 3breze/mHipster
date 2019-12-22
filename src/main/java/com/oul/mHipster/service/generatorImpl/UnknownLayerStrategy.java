package com.oul.mHipster.service.generatorImpl;

import com.oul.mHipster.todelete.OldShitModel;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.squareup.javapoet.TypeSpec;

public class UnknownLayerStrategy implements GenerateLayerStrategy {
    @Override
    public TypeSpec generate(OldShitModel oldShitModel) {
        return null;
    }
}

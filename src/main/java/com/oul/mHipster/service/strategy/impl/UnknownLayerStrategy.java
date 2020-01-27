package com.oul.mHipster.service.strategy.impl;

import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.service.strategy.GenerateLayerStrategy;

public class UnknownLayerStrategy implements GenerateLayerStrategy {
    @Override
    public TypeSpecWrapper generate(Entity entity) {
        return null;
    }
}

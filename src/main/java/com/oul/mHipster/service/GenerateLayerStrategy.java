package com.oul.mHipster.service;

import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;

public interface GenerateLayerStrategy {
    TypeSpecWrapper generate(Entity entity);
}

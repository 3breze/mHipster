package com.oul.mHipster.service.strategy;

import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;
import com.oul.mHipster.service.helper.MethodBuilderService;
import com.oul.mHipster.service.helper.impl.MethodBuilderServiceImpl;

public interface GenerateLayerStrategy {

//    MethodBuilderService methodBuilderService;

    TypeSpecWrapper generate(Entity entity);

//    default MethodBuilderService getMethodBuilderService() {
//        return new MethodBuilderServiceImpl();
//    }
}

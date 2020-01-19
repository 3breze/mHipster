package com.oul.mHipster.service;

import com.oul.mHipster.model.ClassNamingInfo;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RootEntityModel;
import com.oul.mHipster.model.wrapper.LayerModelWrapper;

import java.util.Map;

public interface LayerModelService {

    Map<String, ClassNamingInfo> generateLayerClassNaming(Entity entity, RootEntityModel rootEntityModel);

    LayerModelWrapper initLayerModel();
}

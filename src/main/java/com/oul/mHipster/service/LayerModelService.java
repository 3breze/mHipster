package com.oul.mHipster.service;

import com.oul.mHipster.model.wrapper.LayerModelWrapper;

public interface LayerModelService {

    void generateLayerClassNaming();

    LayerModelWrapper initLayerModel();
}

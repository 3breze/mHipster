package com.oul.mHipster.service.model;

import com.oul.mHipster.model.wrapper.LayerModelWrapper;

public interface ModelService {

    void generateLayerClassNaming();

    LayerModelWrapper initLayerModel();
}

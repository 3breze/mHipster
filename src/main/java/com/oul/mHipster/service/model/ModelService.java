package com.oul.mHipster.service.model;

import com.oul.mHipster.model.wrapper.LayerModelWrapper;
import com.oul.mHipster.model.wrapper.TypeSpecWrapper;

import java.util.List;

public interface ModelService {

    void generateLayerClassNaming();

    LayerModelWrapper initLayerModel();

    List<TypeSpecWrapper> generateSharedClasses();
}

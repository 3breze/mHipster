package com.oul.mHipster.service.base.impl;

import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.RootEntityModel;
import com.oul.mHipster.model.wrapper.LayerModelWrapper;
import com.oul.mHipster.service.*;
import com.oul.mHipster.service.base.JavaFileMakerService;
import com.oul.mHipster.service.base.LayerGeneratorService;
import com.oul.mHipster.service.strategy.GenerateLayerStrategyFactory;
import com.oul.mHipster.service.impl.ModelServiceImpl;
import com.squareup.javapoet.TypeSpec;

public class LayerGeneratorServiceImpl implements LayerGeneratorService {

    private LayersConfig layersConfig;
    private JavaFileMakerService javaFileMakerService;
    private GenerateLayerStrategyFactory generateLayerStrategyFactory;

    /**
     * JavaFileMakerService bi cini mi se trebao da bude parametar konsturktora (dependency inversion principle)
     * Dao bi vecu fleksibilnost i mogucnost da za testiranje umesto imas dve implementacije,
     * jednu koja gradi java fajlove a drugu koja printa na konzolu rezultate.
     */
    public LayerGeneratorServiceImpl(LayersConfig layersConfig) {
        this.layersConfig = layersConfig;
        this.javaFileMakerService = new JavaFileMakerServiceImpl();
    }

    @Override
    public void generateLayers(RootEntityModel rootEntityModel) {

        /**
         * Nekako mi nadleznosti ModelServicea ne odgovaraju ocekivanom sudeci po nazivu, pre bih ga nazvao LayerNamingStrategyProvider
         * ili nesto slicno tome, jer ti zapravo generise strategiju imenovanja za dalje.
         * Takodje, zavisnost tog servisa je samo layer config, entity model je promenljiva kategorija i u mecu njih
         * dve dobijas sta ti je potrebno.
         * Znaci, rekao bih da initLayerModel prima rootEntityModel a da servis dobija samo layer config.
         * Slicno kao sto je sa ovim servisom odradjeno (LayerGeneratorServiceImpl), layerConfig je obavezan i nepromenljiv,
         * entity model je konfigurabilan
         */
        ModelService modelService = new ModelServiceImpl(layersConfig, rootEntityModel);
        LayerModelWrapper layerModelWrapper = modelService.initLayerModel();
        EntityManagerFactory.createEntityManager(layerModelWrapper);

        /**
         * Sva instanciranja bi trebalo uraditi u konstuktoru osim ako se radi o lazy servisima, tada bi trebalo graditi proxy
         */
        generateLayerStrategyFactory = new GenerateLayerStrategyFactory();

        rootEntityModel.getEntities().forEach(entityModel -> layersConfig.getLayers().forEach(layer -> {
            GenerateLayerStrategy generateLayerStrategy = generateLayerStrategyFactory.getLayerStrategy(LayerName.valueOf(layer.getName()));
            TypeSpec typeSpec = generateLayerStrategy.generate(entityModel);
            // Potrebne izmene jer ce jedan entity imati niz TypeSpecova
            // Izmene trebaju i u javaFileMakerService gde se setuje packageName
            if (layer.getName().equals(LayerName.API.toString()))
                entityModel.setTypeSpec(typeSpec);
        }));

        javaFileMakerService.makeJavaFiles(rootEntityModel);
    }
}

package com.oul.mHipster;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layerconfig.LayersConfig;
import com.oul.mHipster.model.RootEntityModel;
import com.oul.mHipster.model.wrapper.MavenInfoWrapper;
import com.oul.mHipster.service.main.LayerGeneratorService;
import com.oul.mHipster.service.main.SourceClassService;
import com.oul.mHipster.service.main.impl.LayerGeneratorServiceImpl;
import com.oul.mHipster.service.main.impl.SourceAttributeServiceImpl;
import com.oul.mHipster.util.ConfigUtil;
import com.oul.mHipster.util.Util;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import javax.xml.bind.JAXBException;

@Mojo(name = "gen")
public class MyMojo extends AbstractMojo {

    @Component
    private MavenProject project;

    public void execute() throws MojoExecutionException {

        MavenInfoWrapper mavenInfoWrapper = new MavenInfoWrapper(project);

        try {
            // Read layers config
            LayersConfig layersConfig = ConfigUtil.readConfig();
            Util.applyLayersConfig(layersConfig, mavenInfoWrapper);

            // Partially build entity model based on source project domain classes
            SourceClassService sourceClassService = new SourceAttributeServiceImpl(mavenInfoWrapper);
            RootEntityModel rootEntityModel = sourceClassService.buildRootEntityModel();

            // Generate CRUD classes / layers
            LayerGeneratorService layerGeneratorService = new LayerGeneratorServiceImpl(layersConfig);
            layerGeneratorService.generateLayers(rootEntityModel);

        } catch (JAXBException e) {
            throw new ConfigurationErrorException("Reading configuration failed!");
        }

    }

}

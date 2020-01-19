package com.oul.mHipster;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.model.RootEntityModel;
import com.oul.mHipster.model.wrapper.MavenInfoWrapper;
import com.oul.mHipster.service.LayerBuilderService;
import com.oul.mHipster.service.SourceClassService;
import com.oul.mHipster.service.impl.LayerBuilderServiceImpl;
import com.oul.mHipster.service.impl.SourceClassAttributeServiceImpl;
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
            SourceClassService sourceClassService = new SourceClassAttributeServiceImpl(mavenInfoWrapper);
            RootEntityModel rootEntityModel = sourceClassService.buildRootEntityModel();

            // Generate CRUD classes / layers
            LayerBuilderService layerBuilderService = new LayerBuilderServiceImpl(layersConfig);
            layerBuilderService.buildLayers(rootEntityModel);

        } catch (JAXBException e) {
            throw new ConfigurationErrorException("Reading configuration failed!");
        }

    }

}

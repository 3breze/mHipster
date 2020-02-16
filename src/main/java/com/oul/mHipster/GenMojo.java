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
import java.io.IOException;
import java.io.InputStream;

@Mojo(name = "gen")
public class GenMojo extends AbstractMojo {

    @Component
    private MavenProject project;

    public void execute() throws MojoExecutionException {

        MavenInfoWrapper mavenInfoWrapper = new MavenInfoWrapper(project);
        InputStream inputStream = this.getClass().getResourceAsStream("/layersConfig.xml");

        try {
            // Read layers config
            LayersConfig layersConfig = ConfigUtil.readConfig(inputStream);
            Util.applyLayersConfig(layersConfig, mavenInfoWrapper);

            // Partially build entity model based on source project domain classes
            SourceClassService sourceClassService = new SourceAttributeServiceImpl(mavenInfoWrapper);
            RootEntityModel rootEntityModel = sourceClassService.buildRootEntityModel();

            // Generate CRUD classes / layers
            LayerGeneratorService layerGeneratorService = new LayerGeneratorServiceImpl(layersConfig);
            layerGeneratorService.generateLayers(rootEntityModel);

        } catch (JAXBException | IOException e) {
            throw new ConfigurationErrorException("Reading configuration failed!");
        }

    }

}

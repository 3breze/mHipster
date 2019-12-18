package com.oul.mHipster;

import com.oul.mHipster.domainApp.EntitiesConfig;
import com.oul.mHipster.domainConfig.LayersConfig;
import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.service.EntityBuilderService;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

@Mojo(name = "gen")
public class MyMojo extends AbstractMojo {

    public void execute() throws MojoExecutionException {
        try {
            EntitiesConfig entitiesConfig = readConfig(EntitiesConfig.class);
            LayersConfig layersConfig = readConfig(LayersConfig.class);

            Util.applyLayersConfig(entitiesConfig, layersConfig);

            layersConfig.getLayers().forEach(layer -> layer.getMethods().forEach(method ->
                    method.getMethodSig().getParameters().forEach(parameter -> System.out.println(parameter.getType() + " : " + parameter.getName()))));
//            EntityBuilderService entityBuilderService = new EntityBuilderService(entitiesConfig, layersConfig);
//            entityBuilderService.buildEntityModel();
        } catch (JAXBException e) {
            e.printStackTrace();
//            throw new ConfigurationErrorException("Reading configuration failed!");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T readConfig(Class<T> clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        String pathname = clazz.equals(EntitiesConfig.class) ? Util.getEntityBuilderConfig() : Util.getLayersConfig();
        return (T) unmarshaller.unmarshal(new File(pathname));
    }
}

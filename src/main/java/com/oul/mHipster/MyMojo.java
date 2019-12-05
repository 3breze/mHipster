package com.oul.mHipster;

import com.oul.mHipster.domainApp.EntityBuilderConfig;
import com.oul.mHipster.domainConfig.LayersConfig;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

@Mojo(name = "gen")
public class MyMojo extends AbstractMojo {

    private String entityBuilderConfig = "C:\\Users\\jovan\\Documents\\mi\\mHipster\\src\\main\\resources\\entityBuilderConfig.xml";
    //    private String entityBuilderConfig = "/Users/mihajlo/Documents/best_in_class/mHipster/src/main/resources/entityBuilderConfig.xml";
    private String layersConfig = "C:\\Users\\jovan\\Documents\\mi\\mHipster\\src\\main\\resources\\layersConfig.xml";
    //    private String layersConfig = "/Users/mihajlo/Documents/best_in_class/mHipster/src/main/resources/layersConfig.xml";

    public void execute() throws MojoExecutionException {

        String[][] arg = {{"key", "value"}, {"key", "value"}, {"key", "value"}};
        Util.add(arg);

        EntityBuilderConfig entityBuilderConfig = null;
        LayersConfig layersConfig = null;
        try {
            entityBuilderConfig = readConfig(EntityBuilderConfig.class);
            layersConfig = readConfig(LayersConfig.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        TestGeneratorService testGeneratorService = new TestGeneratorService(entityBuilderConfig, layersConfig);
        testGeneratorService.build();
    }

    private <T> T readConfig(Class<T> clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        String pathname = clazz.equals(EntityBuilderConfig.class) ? entityBuilderConfig : layersConfig;
        return (T) jaxbUnmarshaller.unmarshal(new File(pathname));
    }
}

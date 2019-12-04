package com.oul.mHipster;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

@Mojo(name = "gen")
public class MyMojo extends AbstractMojo {

    //    private String maconfig = "C:\\Users\\jovan\\Documents\\mi\\mHipster\\maconfig.xml";
    private String maconfig = "/Users/mihajlo/Documents/best_in_class/mHipster/src/main/resources/maconfig.xml";
    //    private String layersConfig = "C:\\Users\\jovan\\Documents\\mi\\mHipster\\layersConfig.xml";
    private String layersConfig = "/Users/mihajlo/Documents/best_in_class/mHipster/src/main/resources/layersConfig.xml";

    public void execute() throws MojoExecutionException {

        Maconfig maconfig = null;
        LayersConfig layersConfig = null;
        try {
            maconfig = readConfig(Maconfig.class);
            layersConfig = readConfig(LayersConfig.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        TestGeneratorService testGeneratorService = new TestGeneratorService(maconfig, layersConfig);
        testGeneratorService.build();
    }

    private <T> T readConfig(Class<T> clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        String pathname = clazz.equals(Maconfig.class) ? maconfig : layersConfig;
        return (T) jaxbUnmarshaller.unmarshal(new File(pathname));
    }
}

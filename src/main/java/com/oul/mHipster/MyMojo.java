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

    public void execute() throws MojoExecutionException {
        Maconfig maconfig = readConfig();
        TestGeneratorService testGeneratorService = new TestGeneratorService(maconfig);
        testGeneratorService.build();
    }

    private Maconfig readConfig() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Maconfig.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            File file = new File("C:\\Users\\jovan\\Documents\\mi\\mHipster\\maconfig.xml");
//          File file = new File("/Users/mihajlo/Documents/best_in_class/mHipster/maconfig.xml");
            return (Maconfig) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return new Maconfig();
    }
}

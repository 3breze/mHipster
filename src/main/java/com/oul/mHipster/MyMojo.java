package com.oul.mHipster;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

@Mojo(name = "hello")
public class MyMojo extends AbstractMojo {

    public void execute() throws MojoExecutionException {
        Maconfig maconfig = test();
        TestGeneratorService testGeneratorService = new TestGeneratorService(maconfig);
        testGeneratorService.build();
//        TypeSpec person = testGeneratorService.emit();

//        List<JavaFile> javaFileList =  testGeneratorService.layerItUp(person);
//        File myFile = new File("./src/main/java");
//        for(JavaFile javaFile:javaFileList){
//            try {
//                javaFile.writeTo(myFile);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        javaFileList.forEach( javaFile -> {
////
//        });
        getLog().info(maconfig.getType());
        for (Layer layer : maconfig.getLayers()) {
            getLog().info(layer.getName());
        }
        for (Entity entity : maconfig.getEntities()) {
            getLog().info(entity.getName());
            for (Attribute attribute : entity.getAttributes()) {
                getLog().info(attribute.getType() + " - " + attribute.getValue());
            }
        }
//        maconfig.getEntities().forEach(entity -> getLog().info(entity.getName()));
//        maconfig.getEntities().forEach(entity -> entity.getAttributes().forEach(attribute ->
//                getLog().info(attribute.getType() + " - " + attribute.getValue())));
    }

    public Maconfig test() {
        try {
            File file = new File("C:\\Users\\jovan\\Documents\\mi\\mHipster\\maconfig.xml");
//            File file = new File("/Users/mihajlo/Documents/best_in_class/mHipster/maconfig.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Maconfig.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (Maconfig) jaxbUnmarshaller.unmarshal(file);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return new Maconfig();
    }
}

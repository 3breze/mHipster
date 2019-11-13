package com.oul.mHipster;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Mojo(name="hello")
public class MyMojo extends AbstractMojo {

    public void execute() throws MojoExecutionException {
        TestGeneratorService testGeneratorService = new TestGeneratorService();
        String name = test();
//        TypeSpec person = testGeneratorService.emit();
//
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
        getLog().info( name);
    }

    public String test(){
        try {

            File file = new File("C:\\Users\\jovan\\Documents\\mi\\mHipster\\config.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Customer.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Customer customer = (Customer) jaxbUnmarshaller.unmarshal(file);
            return customer.getName();

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return "nema ime";
    }
}

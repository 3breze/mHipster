package com.oul.mHipster;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.wrapper.MavenInfoWrapper;
import com.oul.mHipster.service.EntityModelBuilderService;
import com.oul.mHipster.util.Util;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mojo(name = "gen")
public class MyMojo extends AbstractMojo {

    @Component
    private MavenProject project;

    public void execute() throws MojoExecutionException {


        MavenInfoWrapper mavenInfoWrapper = new MavenInfoWrapper(project);

        try {

            LayersConfig layersConfig = readConfig();
            Util.applyLayersConfig(layersConfig, mavenInfoWrapper);
            EntityModelBuilderService entityModelBuilderService = new EntityModelBuilderService(layersConfig);

            ConfigurationBuilder configurationBuilder = createConfigurationBuilder();

            Reflections reflections = new Reflections(configurationBuilder);
            Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(javax.persistence.Entity.class);
            List<Entity> entityModelList = annotated.stream().map(entityModelBuilderService::entityMapper).collect(Collectors.toList());

            entityModelBuilderService.buildLayers(entityModelList);
        } catch (JAXBException e) {
            throw new ConfigurationErrorException("Reading configuration failed!");
        }

    }

    private ConfigurationBuilder createConfigurationBuilder() {
        try {
            List<String> classpathElements = project.getRuntimeClasspathElements();
            classpathElements.add(project.getBuild().getSourceDirectory());
            URL[] urls = new URL[classpathElements.size()];
            for (int i = 0; i < classpathElements.size(); ++i) {
                System.out.println(classpathElements.get(i));
                urls[i] = new File(classpathElements.get(i)).toURL();
            }
            URLClassLoader loader = new URLClassLoader(urls);
            return new ConfigurationBuilder().setUrls(urls).addClassLoader(loader);

        } catch (DependencyResolutionRequiredException | MalformedURLException e) {
            e.printStackTrace();
            throw new ConfigurationErrorException("Reading configuration failed!");
        }
    }


    private LayersConfig readConfig() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(LayersConfig.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (LayersConfig) unmarshaller.unmarshal(new File(Util.getLayersConfig()));
    }
}

package com.oul.mHipster;

import com.oul.mHipster.exception.ConfigurationErrorException;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.SourceDomainLayer;
import com.oul.mHipster.model.wrapper.MavenInfoWrapper;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.EntityModelBuilder;
import com.oul.mHipster.service.impl.EntityManagerFactoryImpl;
import com.oul.mHipster.service.impl.EntityModelBuilderImpl;
import com.oul.mHipster.util.ClassUtils;
import com.oul.mHipster.util.ConfigUtil;
import com.oul.mHipster.util.Util;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.http.converter.json.GsonBuilderUtils;

import javax.xml.bind.JAXBException;
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

            // Read layers config
            LayersConfig layersConfig = ConfigUtil.readConfig();
            Util.applyLayersConfig(layersConfig, mavenInfoWrapper);

            // Load domain classes
            URLClassLoader loader = ClassUtils.createCustomClassloader(project);
            Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(loader.getURLs()).addClassLoader(loader));
            Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(javax.persistence.Entity.class);

            // Generate classes and layers
            EntityModelBuilder entityModelBuilder = new EntityModelBuilderImpl(layersConfig);
            List<Entity> entityModelList = annotated.stream().map(entityModelBuilder::mapSourceToEntity).collect(Collectors.toList());
//            entityModelList.forEach(entity -> {
//                System.out.println(entity.getClassName() + " - attr:" + entity.getAttributes());
//                System.out.println("--  --  --  --");
//            });
            SourceDomainLayer sourceDomainLayer = new SourceDomainLayer(mavenInfoWrapper.getName(), entityModelList);
            entityModelBuilder.buildLayers(sourceDomainLayer);

        } catch (JAXBException e) {
            throw new ConfigurationErrorException("Reading configuration failed!");
        }

    }

}

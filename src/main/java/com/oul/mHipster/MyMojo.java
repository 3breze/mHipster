package com.oul.mHipster;

import ch.qos.logback.classic.BasicConfigurator;
import com.oul.mHipster.domainApp.EntitiesConfig;
import com.oul.mHipster.domainConfig.LayersConfig;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.persistence.Entity;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mojo(name = "gen")
public class MyMojo extends AbstractMojo {

    @Component
    private MavenProject project;

    @Component
    private PluginDescriptor descriptor;

    public void execute() throws MojoExecutionException {
//        try {
//            List<String> runtimeClasspathElements = project.getRuntimeClasspathElements();
//            ClassRealm realm = descriptor.getClassRealm();
//
//            for (String element : runtimeClasspathElements) {
//                System.out.println(element);
//                File elementFile = new File(element);
//                realm.addURL(elementFile.toURI().toURL());
//            }
//        } catch (MalformedURLException | DependencyResolutionRequiredException e) {
//            e.printStackTrace();
//        }


        List classpathElements = null;
        URL urls[] = null;
        try {
            classpathElements = project.getRuntimeClasspathElements();
            classpathElements.add(project.getBuild().getSourceDirectory());
            urls = new URL[classpathElements.size()];
            for (int i = 0; i < classpathElements.size(); ++i) {
                System.out.println((String) classpathElements.get(i));
                urls[i] = new File((String) classpathElements.get(i)).toURL();
            }
        } catch (DependencyResolutionRequiredException | MalformedURLException e) {
            e.printStackTrace();
        }
        URLClassLoader loader = new URLClassLoader(urls);

        Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(urls).addClassLoader(loader));
//        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Entity.class);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Entity.class);
        for (Class<?> aClass : annotated) {
            System.out.println("jedan");
            System.out.println(toStringa(aClass));
        }
        System.out.println(!annotated.isEmpty() ? "yee" : "naa");
//        try {
//
//            List<String> runtimeClasspathElements = project.getRuntimeClasspathElements();
//            ClassRealm realm = descriptor.getClassRealm();
//
//            for (String element : runtimeClasspathElements) {
//                File elementFile = new File(element);
//                realm.addURL(elementFile.toURI().toURL());
//            }
//
//
//        } catch (MalformedURLException | DependencyResolutionRequiredException e) {
//            e.printStackTrace();
//        }
//
//
//        List<String> classpathElements = null;
//        try {
//            classpathElements = project.getCompileClasspathElements();
//            List<URL> projectClasspathList = new ArrayList<URL>();
//            for (String element : classpathElements) {
//                try {
//                    projectClasspathList.add(new File(element).toURI().toURL());
//                } catch (MalformedURLException e) {
//                    throw new MojoExecutionException(element + " is an invalid classpath element", e);
//                }
//            }
//            System.out.println("project class paths: "+projectClasspathList);
//            URLClassLoader loader = new URLClassLoader(projectClasspathList.toArray(new URL[0]),Thread.currentThread().getContextClassLoader());
//            // ... and now you can pass the above classloader to Reflections
////            Set<Method> getters = getAllMethods(Layer.class,
////                    withModifier(Modifier.PUBLIC), withPrefix("get"), withParametersCount(0));
//            Reflections reflections = new Reflections(loader);
//
//            Set<Class<?>> annotated =
//                    reflections.getTypesAnnotatedWith(Entity.class);
//            for (Class<?> aClass : annotated) {
//                System.out.println("jedan");
//                System.out.println(toStringa(aClass));
//            }
//            System.out.println(!annotated.isEmpty() ? "yee" : "naa");
//
//        } catch (DependencyResolutionRequiredException e) {
//            throw new MojoExecutionException("Dependency resolution failed", e);
//        }


        try {
            EntitiesConfig entitiesConfig = readConfig(EntitiesConfig.class);
            LayersConfig layersConfig = readConfig(LayersConfig.class);

            Util.applyLayersConfig(entitiesConfig, layersConfig);

//            layersConfig.getLayers().forEach(layer -> layer.getMethods().forEach(method ->
//                    method.getMethodSig().getParameters().forEach(parameter -> System.out.println(parameter.getType() + " : " + parameter.getName()))));
//            EntityBuilderService entityBuilderService = new EntityBuilderService(entitiesConfig, layersConfig);
//            entityBuilderService.buildEntityModel();
        } catch (JAXBException e) {
            e.printStackTrace();
//            throw new ConfigurationErrorException("Reading configuration failed!");
        }
    }

    public String toStringa(Class<?> clazz) {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append(clazz.getName());
        result.append(" Object {");
        result.append(newLine);

        //determine fields declared in this class only (no fields of superclass)
        Field[] fields = clazz.getDeclaredFields();

        //print field names paired with their values
        for (Field field : fields) {
            result.append("  ");
            result.append(field.getName());
            result.append(": ");
            result.append(field.getType());
            result.append(newLine);
        }
        result.append("}");

        return result.toString();
    }

    @SuppressWarnings("unchecked")
    private <T> T readConfig(Class<T> clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        String pathname = clazz.equals(EntitiesConfig.class) ? Util.getEntityBuilderConfig() : Util.getLayersConfig();
        return (T) unmarshaller.unmarshal(new File(pathname));
    }
}

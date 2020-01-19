package com.oul.mHipster.util;

import com.oul.mHipster.exception.ConfigurationErrorException;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class ClassUtils {

    public static URLClassLoader createCustomClassloader(MavenProject project) {

        try {
            List<String> classpathElements = project.getRuntimeClasspathElements();
            classpathElements.add(project.getBuild().getSourceDirectory());
            URL[] urls = new URL[classpathElements.size()];
            for (int i = 0; i < classpathElements.size(); ++i) {
                System.out.println(classpathElements.get(i));
                urls[i] = new File(classpathElements.get(i)).toURL();
            }
            return new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());

        } catch (DependencyResolutionRequiredException | MalformedURLException e) {
            e.printStackTrace();
            throw new ConfigurationErrorException("Cannot find specified URL(s)!");
        }
    }

    public static String getClassName(Class c) {
        String FQClassName = c.getName();
        int firstChar;
        firstChar = FQClassName.lastIndexOf('.') + 1;
        if (firstChar > 0) {
            FQClassName = FQClassName.substring(firstChar);
        }
        return FQClassName;
    }

    // returns package and class name
    public static String getFullClassName(Class c) {
        return c.getName();
    }

    // returns the package without the classname, empty string if
    // there is no package
    public static String getPackageName(Class c) {
        String fullyQualifiedName = c.getName();
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return fullyQualifiedName.substring(0, lastDot);
    }

    public static String instanceNameBuilder(String className) {
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    public static String optionalNameBuilder(String className) {
        return "optional" + className;
    }
}

package com.oul.mHipster.util;

import com.oul.mHipster.model.wrapper.MavenInfoWrapper;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javax.persistence.Entity;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Set;

public class ReflectionUtil {

    public static Set<Class<?>> loadDomainClasses(MavenInfoWrapper mavenInfoWrapper) {
        URLClassLoader loader = ClassUtils.createCustomClassloader(mavenInfoWrapper.getMavenProject());
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(loader.getURLs())
                .addClassLoader(loader)
                .setScanners(new SubTypesScanner(true), new TypeAnnotationsScanner())
                .filterInputsBy(new FilterBuilder().include(".*class")));
        return reflections.getTypesAnnotatedWith(Entity.class);
    }

    public static Class<?> resolveParameterizedType(Field field) {
        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
        return (Class<?>) genericType.getActualTypeArguments()[0];
    }

    public static boolean isParameterizedType(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    public static Class<?> resolveTypeArgument(Field field) {
        if (Collection.class.isAssignableFrom(field.getType())) {
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            return (Class<?>) genericType.getActualTypeArguments()[0];
        }
        return field.getType();
    }

    public static Object methodInvoker(Method method, Annotation annotation) {
        Object value = null;

        try {
            value = method.invoke(annotation, (Object[]) null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return value;
    }
}

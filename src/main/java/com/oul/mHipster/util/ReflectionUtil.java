package com.oul.mHipster.util;

import com.oul.mHipster.model.wrapper.MavenInfoWrapper;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Set;

public class ReflectionUtil {

    public static Set<Class<?>> loadDomainClasses(MavenInfoWrapper mavenInfoWrapper) {
        URLClassLoader loader = ClassUtils.createCustomClassloader(mavenInfoWrapper.getMavenProject());
        Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(loader.getURLs()).addClassLoader(loader));
        return reflections.getTypesAnnotatedWith(javax.persistence.Entity.class);
    }

    public static Class<?> resolveTypeArgument(Field field) {
        if (Collection.class.isAssignableFrom(field.getType())) {
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            return (Class<?>) genericType.getActualTypeArguments()[0];
        }
        return field.getType();
    }
}

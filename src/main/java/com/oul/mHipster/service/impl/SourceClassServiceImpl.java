package com.oul.mHipster.service.impl;

import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RootEntityModel;
import com.oul.mHipster.model.wrapper.MavenInfoWrapper;
import com.oul.mHipster.service.SourceClassService;
import com.oul.mHipster.util.ClassUtils;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SourceClassServiceImpl extends RelationService implements SourceClassService {

    private MavenInfoWrapper mavenInfoWrapper;

    public SourceClassServiceImpl(MavenInfoWrapper mavenInfoWrapper) {
        this.mavenInfoWrapper = mavenInfoWrapper;
    }

    @Override
    public RootEntityModel buildRootEntityModel() {
        Set<Class<?>> annotated = loadDomainClasses();
        List<Entity> entityModelList = annotated.stream().map(this::mapSourceToEntity).collect(Collectors.toList());
        return new RootEntityModel(mavenInfoWrapper.getName(), entityModelList);
    }

    private Set<Class<?>> loadDomainClasses() {
        URLClassLoader loader = ClassUtils.createCustomClassloader(mavenInfoWrapper.getMavenProject());
        Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(loader.getURLs()).addClassLoader(loader));
        return reflections.getTypesAnnotatedWith(javax.persistence.Entity.class);
    }

    private Entity mapSourceToEntity(Class<?> clazz) {
        Entity.Builder builder = Entity.builder();
        builder.infoFields(clazz);

        Field[] fields = clazz.getDeclaredFields();
        builder.attributes(Arrays.stream(fields).map(field -> findRelation(field, clazz)).collect(Collectors.toList()));

        return builder.build();
    }

}

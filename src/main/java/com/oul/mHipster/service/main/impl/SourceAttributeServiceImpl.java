package com.oul.mHipster.service.main.impl;

import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RootEntityModel;
import com.oul.mHipster.model.wrapper.MavenInfoWrapper;
import com.oul.mHipster.service.main.SourceClassService;
import com.oul.mHipster.service.model.impl.SourceRelationAttributeService;
import com.oul.mHipster.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SourceAttributeServiceImpl extends SourceRelationAttributeService implements SourceClassService {

    private MavenInfoWrapper mavenInfoWrapper;

    public SourceAttributeServiceImpl(MavenInfoWrapper mavenInfoWrapper) {
        this.mavenInfoWrapper = mavenInfoWrapper;
    }

    @Override
    public RootEntityModel buildRootEntityModel() {
        Set<Class<?>> annotated = ReflectionUtil.loadDomainClasses(mavenInfoWrapper);
        List<Entity> entityModelList = annotated.stream().map(this::mapSourceToEntity).collect(Collectors.toList());
        return new RootEntityModel(mavenInfoWrapper.getName(), entityModelList);
    }

    private Entity mapSourceToEntity(Class<?> clazz) {
        Entity.Builder builder = Entity.builder();
        builder.infoFields(clazz);

        Field[] fields = clazz.getDeclaredFields();
        builder.attributes(Arrays.stream(fields).map(field -> findRelation(field, clazz)).collect(Collectors.toList()));

        return builder.build();
    }
}

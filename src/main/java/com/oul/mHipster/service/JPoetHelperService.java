package com.oul.mHipster.service;

import com.oul.mHipster.model.Entity;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;

import java.util.List;

public interface JPoetHelperService {

    CodeBlock buildFindByIdCodeBlock(Entity entity);

    MethodSpec buildConstructor(Entity entity, List<FieldSpec> fieldSpecList, String dependencyClass);

}

package com.oul.mHipster.service;

import com.oul.mHipster.model.Entity;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

public interface JPoetHelperService {

    CodeBlock buildFindByIdCodeBlock(Entity entity);

    MethodSpec buildConstructor(Entity entity, String dependencyClass);

}

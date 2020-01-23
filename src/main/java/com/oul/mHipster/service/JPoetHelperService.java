package com.oul.mHipster.service;

import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;

import java.util.List;

public interface JPoetHelperService {

    CodeBlock buildRelationFindByIdCodeBlock(Entity entity, List<RelationAttribute> relationAttributes);

    List<FieldSpec> buildFieldSpecs(List<RelationAttribute> relationAttributes);

    CodeBlock buildFindByIdCodeBlock(Entity entity);

    CodeBlock buildLombokBuilder(Entity entity);

    MethodSpec buildConstructor(Entity entity, List<FieldSpec> fieldSpecList, String dependencyClass);

}

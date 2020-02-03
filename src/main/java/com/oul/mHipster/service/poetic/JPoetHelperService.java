package com.oul.mHipster.service.poetic;

import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;

import java.util.List;

public interface JPoetHelperService {

    String injectImports(JavaFile javaFile, List<String> imports);

    CodeBlock buildRelationFindByIdCodeBlock(Entity entity, List<RelationAttribute> relationAttributes);

    List<FieldSpec> buildRelationFieldSpecList(List<RelationAttribute> relationAttributes);

    CodeBlock buildFindByIdCodeBlock(Entity entity);

    CodeBlock buildLombokBuilder(Entity entity);

    MethodSpec buildConstructor(List<FieldSpec> fieldSpecList, String dependencyClass);

    CodeBlock buildSetterCallsCodeBlock(Entity entity);

    List<MethodSpec> buildGetters(List<FieldSpec> fieldSpecList);

    List<MethodSpec> buildSetters(List<FieldSpec> fieldSpecList);
}

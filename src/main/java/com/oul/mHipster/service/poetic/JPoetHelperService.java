package com.oul.mHipster.service.poetic;

import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;

import java.util.List;
import java.util.Map;

public interface JPoetHelperService {

    CodeBlock buildFindRelationCodeBlock(Entity entity, Map<Boolean, List<RelationAttribute>> relationAttributes);

    List<FieldSpec> buildRelationFieldSpecList(List<RelationAttribute> relationAttributes);

    CodeBlock buildPageResponse(Entity entity);

    CodeBlock buildFindByIdCodeBlock(Entity entity);

    CodeBlock buildLombokBuilder(Entity entity);

    MethodSpec buildConstructor(List<FieldSpec> fieldSpecList, String dependencyClass);

    MethodSpec buildResponseConstructor(Entity entity, List<FieldSpec> attributeList);

    CodeBlock buildSetterCallsCodeBlock(Entity entity);

    List<MethodSpec> buildGetters(List<FieldSpec> fieldSpecList);

    List<MethodSpec> buildSetters(List<FieldSpec> fieldSpecList);
}

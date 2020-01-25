package com.oul.mHipster.service.helper;

import com.squareup.javapoet.TypeSpec;

public interface JPoetClassBuilderService {

    TypeSpec buildResourceNotFoundException();

    TypeSpec buildValidationGroup();

}

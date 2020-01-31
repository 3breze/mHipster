package com.oul.mHipster.service.poetic;

import com.squareup.javapoet.TypeSpec;

public interface JPoetClassBuilderService {

    TypeSpec buildResourceNotFoundException();

    TypeSpec buildValidationGroup();

}

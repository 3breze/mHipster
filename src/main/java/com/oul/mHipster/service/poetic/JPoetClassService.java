package com.oul.mHipster.service.poetic;

import com.squareup.javapoet.TypeSpec;

public interface JPoetClassService {

    TypeSpec buildResourceNotFoundException();

    TypeSpec buildValidationGroup();

}

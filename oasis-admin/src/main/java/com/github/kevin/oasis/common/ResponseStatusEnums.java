package com.github.kevin.oasis.common;

import lombok.Getter;

@Getter
public enum ResponseStatusEnums {

    SUCCESS("0000","success"),
    FAIL("1000", "fail"),
    NEED_LOGIN("1001", "need login"),
    INVALID_TOKEN("1002", "invalid token"),
    PARAM_ERROR("1003", "param error"),
    SYSTEM_ERROR("1004", "system error");

    private final String code;
    private final String msg;

    ResponseStatusEnums(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}

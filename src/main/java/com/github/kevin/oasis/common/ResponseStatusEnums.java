package com.github.kevin.oasis.common;

import lombok.Getter;

@Getter
public enum ResponseStatusEnums {

    SUCCESS("0000","success"),
    FAIL("1000", "fail"),
    NEED_LOGIN("1001", "need login"),
    INVALID_TOKEN("1002", "invalid token");

    private String code;
    private String msg;

    ResponseStatusEnums(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}

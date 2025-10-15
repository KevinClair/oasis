package com.github.kevin.oasis.models.vo.oauth;

import lombok.Data;

@Data
public class LoginRequest {

    private String userName;

    private String password;

    private Boolean rememberMe;
}

package com.github.kevin.oasis;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;


public class OasisApplicationTests {

    @Test
    public void contextLoads() throws UnsupportedEncodingException {
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        secretKey.getEncoded();
        System.out.println("Generated SECRET_KEY: " + java.util.Base64.getEncoder().encodeToString(secretKey.getEncoded()));
    }

}

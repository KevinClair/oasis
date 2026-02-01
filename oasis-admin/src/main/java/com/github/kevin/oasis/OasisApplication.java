package com.github.kevin.oasis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.github.kevin.oasis.dao")
public class OasisApplication {

    public static void main(String[] args) {
        SpringApplication.run(OasisApplication.class, args);
    }

}

package com.github.kevin.oasis.global.oauth;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Permission {

    /**
     * 权限标识(通常为spEl表达式)
     *
     * @return 权限标识
     */
    String value() default "";
}

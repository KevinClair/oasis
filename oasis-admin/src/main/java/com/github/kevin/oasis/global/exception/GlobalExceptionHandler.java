package com.github.kevin.oasis.global.exception;

import com.github.kevin.oasis.common.BusinessException;
import com.github.kevin.oasis.common.ResponseStatusEnums;
import com.github.kevin.oasis.models.base.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Response<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return Response.<Void>builder()
                .code(e.getCode())
                .msg(e.getMessage())
                .build();
    }

    /**
     * 处理@Valid参数校验异常（@RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败：{}", errorMessage);
        return Response.<Void>builder()
                .code(ResponseStatusEnums.PARAM_ERROR.getCode())
                .msg(errorMessage)
                .build();
    }

    /**
     * 处理@Valid参数校验异常（表单参数）
     */
    @ExceptionHandler(BindException.class)
    public Response<Void> handleBindException(BindException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败：{}", errorMessage);
        return Response.<Void>builder()
                .code(ResponseStatusEnums.PARAM_ERROR.getCode())
                .msg(errorMessage)
                .build();
    }

    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public Response<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Response.<Void>builder()
                .code(ResponseStatusEnums.SYSTEM_ERROR.getCode())
                .msg("系统异常，请稍后重试")
                .build();
    }
}


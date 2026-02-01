package com.github.kevin.oasis.models.base;

import com.github.kevin.oasis.common.ResponseStatusEnums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Response<T> {

    /**
     * 返回信息
     */
    private String msg;

    /**
     * 返回码
     */
    private String code;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 成功
     *
     * @param data 数据
     * @param <T>  泛型
     * @return Response
     */
    public static <T> Response<T> success(T data) {
        return new Response<T>(ResponseStatusEnums.SUCCESS.getMsg(), ResponseStatusEnums.SUCCESS.getCode(), data);
    }
}

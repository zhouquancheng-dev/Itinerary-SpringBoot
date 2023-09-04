package com.zqc.itineraryweb.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务端统一返回json
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    // 返回code，200 成功  0 失败
    private Integer code;
    // 返回消息
    private String msg;
    // 返回数据
    private T data;

    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 0;

    /**
     * 请求成功
     *
     * @param data Object
     * @return data
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "success", data);
    }

    /**
     * 请求成功
     *
     * @return null data
     */
    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, "success", null);
    }

    /**
     * 请求失败
     *
     * @param msg 失败消息
     * @return null data
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(ERROR_CODE, msg, null);
    }
}

package com.example.aiseckill.common.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一API响应类
 *
 * @param <T> 响应数据类型
 */
@Data
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 响应状态码 */
    private Integer code;
    /** 响应消息 */
    private String message;
    /** 响应数据 */
    private T data;
    /** 时间戳 */
    private Long timestamp;

    /** 成功 */
    public static final int SUCCESS = 200;
    /** 请求参数错误 */
    public static final int BAD_REQUEST = 400;
    /** 服务器内部错误 */
    public static final int INTERNAL_ERROR = 500;
    /** AI服务异常 */
    public static final int AI_SERVICE_ERROR = 600;

    private ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    private ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = SUCCESS;
        response.message = "success";
        return response;
    }

    /**
     * 成功响应（有数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = SUCCESS;
        response.message = "success";
        response.data = data;
        return response;
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = SUCCESS;
        response.message = message;
        response.data = data;
        return response;
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = code;
        response.message = message;
        return response;
    }

    /**
     * 失败响应（默认500）
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = INTERNAL_ERROR;
        response.message = message;
        return response;
    }

    /**
     * 判断是否为成功响应
     */
    public boolean isSuccess() {
        return SUCCESS == this.code;
    }
}

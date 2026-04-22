package com.example.aiseckill.common.exception;

import lombok.Getter;

/**
 * 业务异常
 * <p>用于封装业务逻辑错误，区别于系统异常</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码，默认500 */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}

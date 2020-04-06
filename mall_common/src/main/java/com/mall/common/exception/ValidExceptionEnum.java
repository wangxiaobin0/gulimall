package com.mall.common.exception;

import lombok.Getter;

/**
 * @author
 * @date 2020/4/6
 */
public enum ValidExceptionEnum {
    /**
     * 未知异常
     */
    UNKNOWN_EXCEPTION(500, "未知错误"),
    /**
     * 参数校验异常
     */
    VALID_EXCEPTION(400, "参数校验异常");

    /**
     * 错误码
     */
    @Getter
    private int code;
    /**
     * 错误信息
     */
    @Getter
    private String msg;

    ValidExceptionEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}

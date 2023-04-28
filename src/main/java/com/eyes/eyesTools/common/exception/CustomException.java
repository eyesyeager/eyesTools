package com.eyes.eyesTools.common.exception;

import com.eyes.eyesTools.common.result.DefaultResultCode;
import com.eyes.eyesTools.common.result.ResultCodeInterface;
import lombok.ToString;

/**
 * 自定义异常类
 * @author eyes
 */
@ToString
public class CustomException extends Exception {
    protected final Integer errorCode;

    protected final String errorMsg;

    public CustomException() {
        this.errorCode = DefaultResultCode.FAILURE.getCode();
        this.errorMsg = DefaultResultCode.FAILURE.getMessage();
    }

    public CustomException(String errorMsg) {
        this.errorCode = DefaultResultCode.FAILURE.getCode();
        this.errorMsg = errorMsg;
    }

    public CustomException(Integer errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public CustomException(ResultCodeInterface resultCode) {
        this.errorCode = resultCode.getCode();
        this.errorMsg = resultCode.getMessage();
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}

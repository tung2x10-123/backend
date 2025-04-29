package com.project.cloths.Exception;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {
    private final String errorCode;
    private final String errorDesc;

    public GlobalException(String errorCode, String errorDesc) {
        super(errorDesc);
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }

}

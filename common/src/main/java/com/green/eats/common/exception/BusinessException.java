package com.green.eats.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException { // 내가 일부러 터트리는 예외 처리
    private final ErrorCode errorCode; //모든 Error 코드는 이 인터페이스를 상속 받음

    public BusinessException(ErrorCode errorCode) { // 생성자
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public static BusinessException of(ErrorCode errorCode) {
        return new BusinessException(errorCode);
    }
}
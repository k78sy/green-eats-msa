package com.green.eats.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@ConditionalOnProperty(name = "constants.exception.common-handler.enabled", havingValue = "true") // 게이트웨이에서는 안만들어지게 (중복 사용 안되게). true인거에서만 빈등록
public class ServiceExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * 주소는 맞지만 컨트롤러 매핑이 안 된 경우 (404)
     * ResponseEntityExceptionHandler의 handleNoHandlerFoundException를 오버라이드
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(CommonErrorCode.NOT_FOUND_PATH);
    }

    /**
     * 정적 리소스나 잘못된 경로 요청 시 (404)
     * ResponseEntityExceptionHandler의 handleNoResourceFoundException를 오버라이드
     */
    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(CommonErrorCode.NOT_FOUND_PATH);
    }

    // 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<Object> handleBusinessException(BusinessException e) {
        return handleExceptionInternal(e.getErrorCode());
    }

    //Validation 예외가 발생되었을 경우 캐치
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex
            , HttpHeaders headers
            , HttpStatusCode statusCode
            , WebRequest request) {
        return handleExceptionInternal(CommonErrorCode.INVALID_INPUT_VALUE, ex);
    }

    // 시스템 예외 처리 (예: DB 연결 실패 등)
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleException(Exception e) {
        MyErrorResponse response = new MyErrorResponse(
                CommonErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode) {
        return handleExceptionInternal(errorCode, null);
    }

    private static ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode, BindException bindException) {
        List<MyErrorResponse.FieldError> fieldErrorList = null;
        if(bindException != null) {
            fieldErrorList = getValidationError(bindException);
        }
        MyErrorResponse myErrorResponse = new MyErrorResponse(errorCode.getCode(), errorCode.getMessage(), fieldErrorList);
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(myErrorResponse);
    }

    private static List<MyErrorResponse.FieldError> getValidationError(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        List<MyErrorResponse.FieldError> errors = new ArrayList<>(fieldErrors.size());
        for(FieldError fieldError : fieldErrors) {
            errors.add(new MyErrorResponse.FieldError(fieldError.getField(), fieldError.getRejectedValue(), fieldError.getDefaultMessage()));
        }
        return errors;
    }
}
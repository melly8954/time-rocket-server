package com.melly.timerocketserver.global.exception;

import com.melly.timerocketserver.global.common.ResponseController;
import com.melly.timerocketserver.global.common.ResponseDto;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice   // 컨트롤러 실행 중 발생하는 예외를 잡음, 모든 요청에서 가로챔
public class GlobalExceptionHandler implements ResponseController {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDto> handleIllegalArgument(IllegalArgumentException e) {
        log.error("400 Error : " + e.getMessage());
        return makeResponseEntity(HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }

    // DTO 필드 유효성 검사 실패 시 발생하는 예외를 처리하는 핸들러
    // (@Validated 또는 @Valid 사용 시, @RequestBody DTO 내부 필드 제약조건 위반 시 발생)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> handleValidationExceptions(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("잘못된 요청입니다.");
        log.error("400 Error : " + e.getMessage());
        return makeResponseEntity(HttpStatus.BAD_REQUEST, errorMessage, null);
    }

    // @PathVariable 또는 @RequestParam 의 유효성 검사 실패 시 발생하는 예외를 처리하는 핸들러
    // (@Validated 를 클래스나 메서드에 선언하고, 메서드 파라미터에 제약 조건이 걸린 경우 발생)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseDto> handleConstraintViolation(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("잘못된 요청입니다.");
        log.error("400 Error : " + errorMessage);
        return makeResponseEntity(HttpStatus.BAD_REQUEST, errorMessage, null);
    }

    // 사용자 정의 예외
    @ExceptionHandler(DuplicateNicknameException.class)
    public ResponseEntity<ResponseDto> handleDuplicateNickname(DuplicateNicknameException e) {
        log.error("400 Error : " + e.getMessage());
        return makeResponseEntity(HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }

    // 사용자 정의 예외
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResponseDto> handleUserNotFound(UserNotFoundException e) {
        log.error("404 Error : " + e.getMessage());
        return makeResponseEntity(HttpStatus.NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto> handleException(Exception e) {
        log.error("500 Error : " + e.getMessage());
        return makeResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", null);
    }
}

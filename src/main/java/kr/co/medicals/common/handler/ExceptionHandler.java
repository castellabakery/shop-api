package kr.co.medicals.common.handler;

import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class ExceptionHandler extends ResponseEntityExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(ApiRequestFailExceptionMsg.class)
    protected ResponseEntity<ApiResponse> apiRequestFailException(ApiRequestFailExceptionMsg e) {
        return ResponseEntity.ok(ApiResponse.fail(e.getCode(), e.getReason(), e.getData()));
    }

}

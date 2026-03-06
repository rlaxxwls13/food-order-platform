package nbcamp.food_order_platform.global.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ErrorCode에서 정의한 오류 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(
            BusinessException ex,
            HttpServletRequest req
    ) {
        ErrorCode code = ex.getErrorCode();

        log.warn("BusinessException: code={}, path={}",
                code.name(),
                req.getRequestURI());

        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), code));
    }

    //@Valid 요청 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest req
    ) {
        log.warn("Validation failed: path={}, errors={}",
                req.getRequestURI(),
                ex.getBindingResult().getFieldErrors().stream()
                        .map(e -> e.getField() + ":" + e.getDefaultMessage())
                        .toList()
        );

        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), ErrorCode.VALIDATION_FAILED));
    }

    //JSON 파싱 실패 OR 타입 불일치
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest req
    ) {
        log.warn("HttpMessageNotReadable: path={}", req.getRequestURI(), ex);

        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), ErrorCode.VALIDATION_FAILED));
    }

    //위에서 처리하지 못한 예외는 500으로 응답
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest req
    ) {
        log.error("UnexpectedException path={}", req.getRequestURI(), ex);

        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ErrorResponse.of(req.getRequestURI(), ErrorCode.INTERNAL_ERROR));
    }
}
package nbcamp.food_order_platform.global.error;

import java.time.Instant;

public record ErrorResponse(
        String timestamp,
        String path,
        Integer code,
        String message
) {
    public record FieldError(String field, String reason) {}

    public static ErrorResponse of(String path, ErrorCode errorCode) {
        return new ErrorResponse(
                Instant.now().toString(),
                path,
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }
}
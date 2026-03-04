package nbcamp.food_order_platform.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //400
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, 4001, "Validation failed."), //요청실패

    // 401
    SIGN_IN_FAIL(HttpStatus.UNAUTHORIZED, 4012, "Login information mismatch."), //로그인실패
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 4013,"Invalid or expired token."), //토큰 만료
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, 4014, "Invalid password."), //잘못된 패스워드
    AUTHORIZATION(HttpStatus.UNAUTHORIZED, 4015,"Authorization Failed."), //인증실패

    // 403
    NO_PERMISSION(HttpStatus.FORBIDDEN, 4031,"Do not have permission."), //권한없음

    // 404
    NOT_EXISTED_USER(HttpStatus.NOT_FOUND, 4041,"This user does not exist."), //유저없음
    NOT_EXISTED_STORE(HttpStatus.NOT_FOUND, 4042, "This store does not exist."), //가게없음
    NOT_EXISTED_PRODUCT(HttpStatus.NOT_FOUND, 4043, "This product does not exist."), //상품없음
    NOT_EXISTED_ORDER(HttpStatus.NOT_FOUND, 4044, "This order does not exist."), //주문없음
    NOT_EXISTED_REVIEW(HttpStatus.NOT_FOUND, 4045, "Review not found."), //리뷰없음
    NOT_EXISTED_AI_LOG(HttpStatus.NOT_FOUND, 4046, "AI Description log not found."), //ai로 생성한설명 없음

    //409
    DUPLICATED_USER_ID(HttpStatus.CONFLICT, 4091, "User ID already exists."), //유저ID 중복
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, 4092, "Insufficient stock."), //재고부족
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, 4093, "Review already exists for this order."), //이미 리뷰를 작성한 주문

    // 500
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5001, "Database error."), //DB처리 오류
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5002, "Server error."), //예외 오류

    //502
    AI_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, 5021, "AI API request  failed."); //ai api 호출 실패

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, Integer code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public Integer getCode() { return code; }
    public String getMessage() { return message; }
}
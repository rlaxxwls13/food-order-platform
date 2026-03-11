package nbcamp.food_order_platform.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    //400
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, 4001, "Validation failed."), //요청실패
    INVALID_ROLE(HttpStatus.BAD_REQUEST, 4002, "Invalid role"), //잘못된 role요청
    ALREADY_DELETED_USER(HttpStatus.BAD_REQUEST, 4003, "User already deleted"), //이미 탈퇴한 사용자

    REVIEW_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, 4004, "Review creation or modification period has expired"), // 리뷰 수정기간이 지남
    ORDER_NOT_COMPLETED(HttpStatus.BAD_REQUEST, 4005, "Reviews can only be written for completed orders."), // 오더의 상태가 completed만 리뷰 작성 가능
    INVALID_PASSWORD_LENGTH(HttpStatus.BAD_REQUEST, 4006, "Password length must be between 8 and 20 characters."),//패스워드는 8~20자 사이여야 합니다.

    INVALID_PASSWORD_PATTERN(HttpStatus.BAD_REQUEST, 4007,
            "Password must include uppercase, lowercase, number, and special character"), //패스워드는 알파벳 대소문자(a~z, A~Z), 숫자(0~9), 특수문자를 포함해야 합니다.
    INVALID_USER_ID_LENGTH(HttpStatus.BAD_REQUEST, 4008,"Username length must be between 4 and 10 characters."), //username은 4자 이상, 10자 이하여야 합니다.
    INVALID_USER_ID_PATTERN(HttpStatus.BAD_REQUEST,4009,"Username must contain only lowercase letters and numbers."), //username은 알파벳 소문자(a~z), 숫자(0~9)로 구성되어야 합니다.
    // 401
    SIGN_IN_FAIL(HttpStatus.UNAUTHORIZED, 4012, "Login information mismatch."), //로그인실패
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 4013,"Invalid or expired token."), //토큰 서명오류/형식 오류
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, 4014, "Invalid password."), //잘못된 패스워드
    AUTHORIZATION(HttpStatus.UNAUTHORIZED, 4015,"Authorization Failed."), //인증실패
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, 4016, "expired token"), //토큰 말료
    ALREADY_DELETED_ADDRESS(HttpStatus.BAD_REQUEST, 4017, "Address already deleted."), //이미 삭제된 주소
    ADDRESS_REQUIRED(HttpStatus.BAD_REQUEST, 4018, "At least one address must remain."), //적어도 하나의 주소는 남아 있어야 합니다

    // 403
    NO_PERMISSION(HttpStatus.FORBIDDEN, 4031,"Do not have permission."), //권한없음

    // 404
    NOT_EXISTED_USER(HttpStatus.NOT_FOUND, 4041,"This user does not exist."), //유저없음
    NOT_EXISTED_STORE(HttpStatus.NOT_FOUND, 4042, "This store does not exist."), //가게없음
    NOT_EXISTED_PRODUCT(HttpStatus.NOT_FOUND, 4043, "This product does not exist."), //상품없음
    NOT_EXISTED_ORDER(HttpStatus.NOT_FOUND, 4044, "This order does not exist."), //주문없음
    NOT_EXISTED_REVIEW(HttpStatus.NOT_FOUND, 4045, "Review not found."), //리뷰없음
    NOT_EXISTED_AI_LOG(HttpStatus.NOT_FOUND, 4046, "AI Description log not found."), //ai로 생성한설명 없음
    NOT_EXISTED_STORE_REGION(HttpStatus.NOT_FOUND, 4047, "This store region does not exist"), //가게 지역 없음
    NOT_EXISTED_STORE_CATEGORY(HttpStatus.NOT_FOUND, 4048, "This store category does not exist"), //가게 카테고리 없음
    NOT_EXISTED_ADDRESS(HttpStatus.NOT_FOUND, 4049, "This address does not exist."), //존재하지 않는 주소


    //409
    DUPLICATED_USER_ID(HttpStatus.CONFLICT, 4091, "User ID already exists."), //유저ID 중복
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, 4092, "Insufficient stock."), //재고부족
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, 4093, "Review already exists for this order."), //이미 리뷰를 작성한 주문
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, 4094, "User email already exists"), // email 중복

    // 500
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5001, "Database error."), //DB처리 오류
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5002, "Server error."), //예외 오류

    //502
    AI_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, 5021, "AI API request  failed."); //ai api 호출 실패

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

}
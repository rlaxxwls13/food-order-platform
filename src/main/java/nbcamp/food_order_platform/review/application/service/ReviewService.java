package nbcamp.food_order_platform.review.application.service;


import lombok.RequiredArgsConstructor;
import nbcamp.food_order_platform.review.domain.repository.ReviewRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    // 리뷰 작성
    public void createReview() {}

    // 리뷰 수정
    public void updateReview() {}

    // 리뷰 삭제
    public void deleteReview() {}

    // 리뷰 조회
    public void getReviews() {}



}

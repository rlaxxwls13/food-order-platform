package nbcamp.food_order_platform.ai.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbcamp.food_order_platform.ai.client.request.GeminiReqDto;
import nbcamp.food_order_platform.ai.client.response.GeminiResDto;
import nbcamp.food_order_platform.global.error.ErrorCode;
import nbcamp.food_order_platform.global.error.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiDescriptionService {
    private final RestTemplate restTemplate;

    @Value("${ai.gemini.api-key}")
    private String apiKey;

    @Value("${ai.gemini.base-url}")
    private String baseUrl;

    @Value("${ai.gemini.model}")
    private String model;

    public String generateAiDescription(String requestText) {

        String requestUrl = baseUrl + "/" + model + ":generateContent?key=" + apiKey;
        String prompt = createPrompt(requestText);

        GeminiReqDto request = new GeminiReqDto();
        request.createGeminiReqDto(prompt);

        GeminiResDto response;
        try {
            response = restTemplate.postForObject(requestUrl, request, GeminiResDto.class);
        } catch (RestClientResponseException e) {
            log.error("Gemini API error. status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BusinessException(ErrorCode.AI_GENERATION_FAILED, "Gemini API Error");
        }

        return getGeneratedText(response);
    }

    private String createPrompt(String description) {
        return """
                너는 쇼핑몰 상품 설명을 작성하는 도우미야.
                아래 초안 설명을 바탕으로 자연스러운 상품 설명으로 바꿔줘.
                
                조건:
                - 한국어로 작성
                - 1~2 문장
                - 과장되지 않게
                - 상품 상세페이지에 바로 넣을 수 있게 작성
                - 불필요한 이모지, 특수문자 금지
                - 50자 이내로
                
                원본 설명:
                %s
                """.formatted(description);
    }

    //api 응답에서 생성된 설명만 꺼내기
    private String getGeneratedText(GeminiResDto response) {

        if (response == null ||
                response.getCandidates() == null ||
                response.getCandidates().isEmpty() ||
                response.getCandidates().get(0).getContent() == null ||
                response.getCandidates().get(0).getContent().getParts() == null ||
                response.getCandidates().get(0).getContent().getParts().isEmpty()) {
            throw new BusinessException(ErrorCode.AI_GENERATION_FAILED, "response is null or empty.");
        }

        String text = response.getCandidates().get(0).getContent().getParts().get(0).getText();

        if (text == null || text.isBlank()) {
            throw new BusinessException(ErrorCode.AI_GENERATION_FAILED, "generatedText is null or empty.");
        }

        return text;
    }
}


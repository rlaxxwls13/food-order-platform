package nbcamp.food_order_platform.ai.client.response;

import lombok.Getter;

import java.util.List;

@Getter
public class GeminiResDto {

    private List<Candidate> candidates;

    @Getter
    public static class Candidate {
        private Content content;
        private String finishReason;
    }

    @Getter
    public static class Content {
        private List<Part> parts;
        private String role;
    }

    @Getter
    public static class Part {
        private String text;
    }

    public String getGeneratedText() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        Candidate candidate = candidates.get(0);
        if (candidate.getContent() == null ||
                candidate.getContent().getParts() == null ||
                candidate.getContent().getParts().isEmpty()) {
            return null;
        }

        return candidate.getContent().getParts().get(0).getText();
    }
}
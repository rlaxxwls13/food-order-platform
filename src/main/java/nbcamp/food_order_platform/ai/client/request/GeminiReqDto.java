package nbcamp.food_order_platform.ai.client.request;

import lombok.Getter;

import java.util.List;

@Getter
public class GeminiReqDto {

    private final List<Content> contents;

    public GeminiReqDto(String text) {
        this.contents = List.of(new Content(text));
    }

    @Getter
    static class Content {
        private final List<Part> parts;

        Content(String text) {
            this.parts = List.of(new Part(text));
        }
    }

    @Getter
    static class Part {
        private final String text;

        Part(String text) {
            this.text = text;
        }
    }
}
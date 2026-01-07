package com.jamjam.service.dto.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class GeminiDto {

    @Builder
    @Getter
    public static class GeminiRequest {
        private List<Content> contents;
        @JsonProperty("generation_config")
        private GenerationConfig generationConfig;
    }

    @Builder
    @Getter
    public static class Content {
        private List<Part> parts;
    }

    @Builder
    @Getter
    public static class Part {
        private String text;
    }

    @Builder
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GenerationConfig {
        @JsonProperty("response_modalities")
        private List<String> responseModalities;

        @JsonProperty("response_mime_type")
        private String responseMimeType;
    }
}

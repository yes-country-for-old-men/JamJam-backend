package com.jamjam.service.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface AIClient {
    JsonNode generateTextContent(String prompt);
    JsonNode generateImageContent(String prompt);
}

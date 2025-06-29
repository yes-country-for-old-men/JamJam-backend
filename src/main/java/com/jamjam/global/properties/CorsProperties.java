package com.jamjam.global.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "jamjam.cors")
public class CorsProperties {
    private List<String> allowedOrigins = List.of("*");
}

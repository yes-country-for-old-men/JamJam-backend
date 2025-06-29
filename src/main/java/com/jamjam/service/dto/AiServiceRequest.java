package com.jamjam.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AiServiceRequest {
    private String description;
    private String skills;
    private String career;
}

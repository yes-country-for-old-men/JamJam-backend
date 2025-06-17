package com.jamjam.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AiServiceResponse {
    private String serviceName;
    private String category;
    private String thumbnailInfo;
    private String Description;
    private String salary;
    private String skills;
    private String career;
    private String education;
}

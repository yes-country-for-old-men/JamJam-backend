package com.jamjam.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServiceListResponse {
    private String thumbnailUrl;
    private String serviceName;
    private String providerName;
    private int salary;
}

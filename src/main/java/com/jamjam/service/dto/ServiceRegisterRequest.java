package com.jamjam.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServiceRegisterRequest {
    private String serviceName;
    private String description;
    private int categoryId;
    private int salary;
}

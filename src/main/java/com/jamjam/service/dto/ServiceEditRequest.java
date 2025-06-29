package com.jamjam.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ServiceEditRequest {
    private String serviceName;
    private String description;
    private Integer salary;
    private Integer categoryId;

    private List<String> deleteImages;
}


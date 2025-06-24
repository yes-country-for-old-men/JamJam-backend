package com.jamjam.service.service;

import com.jamjam.global.exception.ApiException;
import com.jamjam.global.exception.CommonErrorCode;
import com.jamjam.service.dto.ServiceRegisterRequest;
import com.jamjam.service.entity.ServiceEntity;
import com.jamjam.service.repository.ServiceRepository;
import com.jamjam.service.util.OpenAiClient;
import com.jamjam.service.util.S3Uploader;
import com.jamjam.user.User;
import com.jamjam.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ServiceService {
    private final OpenAiClient openAiClient;
    private final ServiceRepository serviceRepository;
    private final S3Uploader s3Uploader;
    private final UserRepository userRepository;

    public ServiceService(OpenAiClient openAiClient, ServiceRepository serviceRepository, S3Uploader s3Uploader, UserRepository userRepository) {
        this.openAiClient = openAiClient;
        this.serviceRepository = serviceRepository;
        this.s3Uploader = s3Uploader;
        this.userRepository = userRepository;
    }
    /*서비스 상세 설명은 Gpt로 마크다운 문법 적용
    * 썸네일, 포트폴리오 이미지들은 S3에 저장
    * 그 후 서비스 DB에 저장*/
    public void registerService(ServiceRegisterRequest request, MultipartFile thumbnail, List<MultipartFile> infoImages) {
        UUID testID = UUID.fromString("bd8c8e97-fe40-49fe-b162-d4d095bfbf7b");
        User user = userRepository.findById(testID)
                .orElseThrow(() -> new ApiException(CommonErrorCode.USER_NOT_FOUND));
        String description = openAiClient.applyMarkdown(request.getDescription());
        System.out.println("마크다운 적용");
        try {
            String thumbnailUrl = s3Uploader.upload(thumbnail, "thumbnails");
            System.out.println("썸네일 저장 완료: " + thumbnailUrl);
            List<String> infoImageUrls = new ArrayList<>();
            if (infoImages != null) {
                for (MultipartFile image : infoImages) {
                    String imageUrl = s3Uploader.upload(image, "info-images");
                    infoImageUrls.add(imageUrl);
                }
                System.out.println("포트폴리오 이미지 저장 완료");
            }
            ServiceEntity service = ServiceEntity.builder()
                    .title(request.getServiceName())
                    .description(description)
                    .category(request.getCategory())
                    .salary(request.getSalary())
                    .thumbnail(thumbnailUrl)
                    .infoImages(infoImageUrls)
                    .user(user)
                    .build();

            serviceRepository.save(service);
        } catch(IOException e) {
            throw new ApiException(CommonErrorCode.IMAGE_UPLOAD_ERROR);
        }
    }
}

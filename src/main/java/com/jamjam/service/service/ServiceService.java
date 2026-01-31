package com.jamjam.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamjam.chat.application.ChatService;
import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.chat.domain.entity.MessageType;
import com.jamjam.chat.util.EventBroadcaster;
import com.jamjam.global.exception.ApiException;
import com.jamjam.order.exception.OrderError;
import com.jamjam.service.domain.entity.ServiceInfoImageEntity;
import com.jamjam.service.domain.repository.ServiceInfoImageRepository;
import com.jamjam.service.dto.*;
import com.jamjam.service.exception.ServiceError;
import com.jamjam.service.domain.entity.ServiceEntity;
import com.jamjam.service.domain.repository.ServiceRepository;
import com.jamjam.service.util.S3Uploader;
import com.jamjam.user.application.dto.CustomUserDetails;
import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.entity.UserRole;
import com.jamjam.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceService {
    private final ServiceRepository serviceRepository;
    private final S3Uploader s3Uploader;
    private final UserRepository userRepository;
    private final ServiceInfoImageRepository serviceInfoImageRepository;
    private final ChatService chatService;
    private final EventBroadcaster eventBroadcaster;
    private final ObjectMapper objectMapper;

    /*서비스 등록
    * 썸네일, 포트폴리오 이미지들은 S3에 저장
    * 그 후 서비스 DB에 저장*/
    @Transactional
    public void registerService(ServiceRegisterRequest request, Long userId, MultipartFile thumbnail, List<MultipartFile> portfolioImages) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ServiceError.USER_NOT_FOUND));
        if (user.getRole() != UserRole.PROVIDER) throw new ApiException(ServiceError.NO_AUTH_WRITE);

        try {
            String thumbnailUrl = s3Uploader.upload(thumbnail, "thumbnails");
            log.info("썸네일 저장 완료: " + thumbnailUrl);

            String description = request.getDescription();
            String descriptionPlainText = Jsoup.parse(description).text();

            ServiceEntity service = ServiceEntity.builder()
                    .serviceName(request.getServiceName())
                    .description(description)
                    .descriptionPlainText(descriptionPlainText)
                    .categoryId(request.getCategoryId())
                    .salary(request.getSalary())
                    .thumbnail(thumbnailUrl)
                    .user(user)
                    .build();

            serviceRepository.save(service);
            log.info("서비스 등록 완료");

            if (portfolioImages != null) {
                for (MultipartFile image : portfolioImages) {
                    if (!image.isEmpty()) {
                        String imageUrl = s3Uploader.upload(image, "portfolio-images");
                        ServiceInfoImageEntity imageInfo = ServiceInfoImageEntity.builder()
                                .imageUrl(imageUrl)
                                .service(service)
                                .build();
                        serviceInfoImageRepository.save(imageInfo);
                    }
                }
                log.info("포트폴리오 이미지 저장 완료");
        }

        } catch(IOException e) {
            throw new ApiException(ServiceError.IMAGE_UPLOAD_ERROR);
        }
    }
    /*분류 별 서비스 리스트 반환 (카테고리, 제공자)*/
    @Transactional
    public ServiceListResponse getFilteredServices(Integer categoryId, Long providerId, Pageable pageable) {
        Page<ServiceEntity> entities;

        if (categoryId != null) {
            entities = serviceRepository.findByCategoryId(categoryId, pageable);
        } else if (providerId != null) {
            entities = serviceRepository.findByUserId(providerId, pageable);
        } else {
            entities = serviceRepository.findAll(pageable);
        }
        List<ServiceSummaryDTO> dtoList = entities.stream()
                .map(ServiceSummaryDTO::from)
                .toList();

        return ServiceListResponse.builder()
                .services(dtoList)
                .currentPage(entities.getNumber())
                .totalPages(entities.getTotalPages())
                .hasNext(entities.hasNext())
                .build();
    }
    /*서비스 상세 내용 조회*/
    @Transactional
    public ServiceInfoDTO getServiceDetail(Long serviceId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ApiException(ServiceError.SERVICE_NOT_FOUND));
        return ServiceInfoDTO.from(service);
    }
    /*서비스 삭제*/
    @Transactional
    public void deleteService(CustomUserDetails customUserDetails, Long serviceId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ApiException(ServiceError.SERVICE_NOT_FOUND));
        Long servicePublisherId = service.getUser().getId();
        Long currentUserId = customUserDetails.getUserId();

        if (!servicePublisherId.equals(currentUserId)) {
            throw new ApiException(ServiceError.FORBIDDEN_DELETE);
        }
        log.info("삭제 권한 확인 완료");
        /*썸네일 S3에서 삭제*/
        s3Uploader.delete(service.getThumbnail());
        log.info("썸네일 삭제 완료");
        /*포트폴리오 이미지 S3에서 삭제*/
        if (service.getPortfolioImages() != null) {
            for (ServiceInfoImageEntity image : service.getPortfolioImages()) {
                s3Uploader.delete(image.getImageUrl());
            }
            log.info("포트폴리오 이미지 삭제 완료");

        }
        serviceRepository.deleteById(serviceId);
        log.info("서비스 삭제 완료");
    }
    /*서비스 정보 수정
    * 수정 가능 필드: 서비스 명, 서비스 썸네일(ai생성 제외),
    * 포트폴리오 이미지, 상세 설명, 카테고리, 급여, 경력*/
    @Transactional
    public void editService(CustomUserDetails customUserDetails, Long serviceId,
                            ServiceEditRequest request, MultipartFile thumbnail,
                            List<MultipartFile> portfolioImages) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ApiException(ServiceError.SERVICE_NOT_FOUND));
        Long servicePublisherId = service.getUser().getId();
        Long currentUserId = customUserDetails.getUserId();

        if (!servicePublisherId.equals(currentUserId)) {
            throw new ApiException(ServiceError.FORBIDDEN_MODIFY);
        }
        log.info("수정 권한 확인 완료");

        List<ServiceInfoImageEntity> currentImages = service.getPortfolioImages();
        /*텍스트 필드 수정*/
        if (request != null) {
            if (request.getServiceName() != null) {
                service.setServiceName(request.getServiceName());
            }
            if (request.getDescription() != null) {
                service.setDescription(request.getDescription());
            }
            if (request.getSalary() != null) {
                service.setSalary(request.getSalary());
            }
            if (request.getCategoryId() != null) {
                service.setCategoryId(request.getCategoryId());
            }

            if (request.getDeleteImageIds() != null) {
                List<ServiceInfoImageEntity> toDelete = new ArrayList<>();
                for (Long deleteImageId : request.getDeleteImageIds()) {
                    for (ServiceInfoImageEntity image : currentImages) {
                        if (image.getId().equals(deleteImageId)) {
                            s3Uploader.delete(image.getImageUrl());
                            toDelete.add(image);
                            break;
                        }
                    }
                }
                currentImages.removeAll(toDelete);
                serviceInfoImageRepository.deleteAll(toDelete);
                log.info("선택 포트폴리오 이미지 삭제 완료");
            }
        }
        try {
            if (portfolioImages != null) {
                for (MultipartFile newFile : portfolioImages) {
                    if (!newFile.isEmpty()) {
                        String uploadUrl = s3Uploader.upload(newFile, "portfolio-images");
                        ServiceInfoImageEntity imageEntity = ServiceInfoImageEntity.builder()
                                .imageUrl(uploadUrl)
                                .service(service)
                                .build();
                        serviceInfoImageRepository.save(imageEntity);
                        currentImages.add(imageEntity);
                    }
                }
                log.info("포트폴리오 이미지 수정 완료");
            }
            if (thumbnail != null && !thumbnail.isEmpty()) {
                s3Uploader.delete(service.getThumbnail());
                String thumbnailUrl = s3Uploader.upload(thumbnail, "thumbnails");
                service.setThumbnail(thumbnailUrl);
                log.info("썸네일 이미지 수정 완료");
            }
        } catch (IOException e) {
            throw new ApiException(ServiceError.IMAGE_UPLOAD_ERROR);
        }

        serviceRepository.save(service);
    }

    public void makeServiceInquiry(Long userId, Long serviceId) {
        ServiceEntity service = serviceRepository.findByIdOrThrow(serviceId, ServiceError.SERVICE_NOT_FOUND);

        String content;
        try {
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("serviceId", service.getId());
            contentMap.put("serviceName", service.getServiceName());

            content = objectMapper.writeValueAsString(contentMap);
        } catch (JsonProcessingException e) {
            log.error("[SERVICE] 메시지 포맷 변환 실패", e);
            throw new ApiException(ServiceError.JSON_PROCESSING_ERROR);
        }

        sendMessage(userId, service.getUser().getId(), MessageType.SERVICE_INQUIRY, content);
    }

    public void sendMessage(Long senderId, Long receiverId, MessageType type, String content) {
        Long chatRoomId = chatService.getChatRoomId(senderId, receiverId);

        ChatMessageEntity savedMsg = chatService
                .sendMessage(chatRoomId, String.valueOf(senderId), content, type, null);
        eventBroadcaster.broadcastNewMessage(savedMsg, String.valueOf(senderId));
    }
}

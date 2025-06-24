package com.jamjam.service.util;

import io.awspring.cloud.s3.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Component
public class S3Uploader {
    private final S3Client s3Client;
    @Value("${aws.s3.bucket}")
    private String bucket;

    public S3Uploader(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /*file S3에 저장 후 URL 반환*/
    public String upload(MultipartFile file, String dirName) throws IOException {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        /*이미지 저장 위치 URL 반환*/
        return "https://" + bucket + ".s3.amazonaws.com/" + fileName;
    }
}

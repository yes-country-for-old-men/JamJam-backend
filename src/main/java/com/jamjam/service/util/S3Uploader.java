package com.jamjam.service.util;

import com.jamjam.global.exception.ApiException;
import com.jamjam.service.exception.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
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
    /*S3에서 이미지 삭제*/
    public void delete(String url) {
        try {
            URL safeUrl = new URL(url);
            URI uri = new URI(
                    safeUrl.getProtocol(),
                    safeUrl.getHost(),
                    safeUrl.getPath(),
                    null
            );
            String decodedPath = URLDecoder.decode(uri.getPath(), StandardCharsets.UTF_8);
            String key = decodedPath.substring(1);
            log.info("삭제할 S3 객체 Key: {}", key);

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.deleteObject(request);
        } catch (Exception e) {
            throw new ApiException(CommonErrorCode.S3_IMAGE_NOT_FOUND);
        }
    }
}

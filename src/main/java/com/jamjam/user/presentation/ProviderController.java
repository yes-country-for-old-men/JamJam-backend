package com.jamjam.user.presentation;

import com.jamjam.global.annotation.CurrentUser;
import com.jamjam.global.dto.ResponseDto;
import com.jamjam.global.dto.SuccessMessage;
import com.jamjam.user.application.ProviderService;
import com.jamjam.user.application.dto.CustomUserDetails;
import com.jamjam.user.domain.entity.ProviderEntity;
import com.jamjam.user.presentation.dto.request.ProviderRequest;
import com.jamjam.user.presentation.dto.response.ProviderResponse;
import com.jamjam.user.presentation.dto.response.ProviderPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
@Tag(name = "Provider", description = "Provider 프로필 관리 API")
public class ProviderController {
    private final ProviderService providerService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Provider 프로필 생성",
        description = "Provider 프로필을 생성합니다. 기술, 경력, 학력, 자격증 정보와 증빙자료를 함께 업로드할 수 있습니다. ",
        responses = {
            @ApiResponse(responseCode = "200", description = "프로필 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
        }
    )
    public ResponseEntity<ResponseDto<Void>> createProvider(
            @CurrentUser CustomUserDetails user,
            @Parameter(description = "Provider 정보", required = true)
            @RequestPart("request") ProviderRequest request,
            @Parameter(description = "기술 증빙자료 파일 목록 (PDF, 이미지 등)")
            @RequestPart(value = "skillFiles", required = false) List<MultipartFile> skillFiles,
            @Parameter(description = "경력 증빙자료 파일 목록 (재직증명서 등)")
            @RequestPart(value = "careerFiles", required = false) List<MultipartFile> careerFiles,
            @Parameter(description = "학력 증빙자료 파일 목록 (졸업증명서 등)")
            @RequestPart(value = "educationFiles", required = false) List<MultipartFile> educationFiles,
            @Parameter(description = "자격증 증빙자료 파일 목록 (자격증 사본 등)")
            @RequestPart(value = "licenseFiles", required = false) List<MultipartFile> licenseFiles
    ) throws IOException {
        providerService.createProvider(user.getUserId(), request, skillFiles, careerFiles, educationFiles, licenseFiles);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }

    @GetMapping
    @Operation(
        summary = "내 Provider 정보 조회",
        description = "현재 로그인한 사용자의 Provider 프로필을 조회합니다. 증빙자료 URL이 포함됩니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "Provider 프로필 없음")
        }
    )
    public ResponseEntity<ResponseDto<ProviderResponse>> getProvider(
            @CurrentUser CustomUserDetails user
    ) {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                providerService.getProvider(user.getUserId())));
    }

    @GetMapping("/page/{userId}")
    @Operation(
        summary = "Provider 페이지 조회 (공개)",
        description = "특정 Provider의 공개 프로필 페이지를 조회합니다. 프로필 정보, 증빙자료, 제공 서비스 목록을 포함합니다. " +
                     "인증 없이 접근 가능합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        }
    )
    public ResponseEntity<ResponseDto<ProviderPageResponse>> getProviderPage(
            @Parameter(description = "Provider의 사용자 ID", example = "1", required = true)
            @PathVariable Long userId
    ) {
        ProviderPageResponse response = providerService.getProviderPage(userId);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }

    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Provider 프로필 수정",
        description = "Provider 프로필을 수정합니다. 기존 증빙자료를 유지하거나 새로운 파일로 교체할 수 있습니다. " +
                     "새 파일을 업로드하면 기존 S3 파일은 유지되고 새 URL로 업데이트됩니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "Provider 프로필 없음")
        }
    )
    public ResponseEntity<ResponseDto<ProviderResponse>> updateProvider(
            @CurrentUser CustomUserDetails user,
            @Parameter(description = "수정할 Provider 정보", required = true)
            @RequestPart("request") ProviderRequest request,
            @Parameter(description = "기술 증빙자료 파일 목록 (선택)")
            @RequestPart(value = "skillFiles", required = false) List<MultipartFile> skillFiles,
            @Parameter(description = "경력 증빙자료 파일 목록 (선택)")
            @RequestPart(value = "careerFiles", required = false) List<MultipartFile> careerFiles,
            @Parameter(description = "학력 증빙자료 파일 목록 (선택)")
            @RequestPart(value = "educationFiles", required = false) List<MultipartFile> educationFiles,
            @Parameter(description = "자격증 증빙자료 파일 목록 (선택)")
            @RequestPart(value = "licenseFiles", required = false) List<MultipartFile> licenseFiles
    ) throws IOException {
        ProviderResponse response = providerService.updateProvider(user.getUserId(), request, skillFiles, careerFiles, educationFiles, licenseFiles);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }

    @DeleteMapping
    @Operation(
        summary = "Provider 프로필 삭제",
        description = "Provider 프로필을 삭제합니다. 관련된 모든 증빙자료와 정보가 함께 삭제됩니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "Provider 프로필 없음")
        }
    )
    public ResponseEntity<ResponseDto<Void>> deleteProvider(
            @CurrentUser CustomUserDetails user
    ) {
        providerService.deleteProvider(user.getUserId());
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }
}
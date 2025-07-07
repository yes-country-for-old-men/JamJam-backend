package com.jamjam.user.presentation;

import com.jamjam.global.annotation.CurrentUser;
import com.jamjam.global.dto.ResponseDto;
import com.jamjam.global.dto.SuccessMessage;
import com.jamjam.user.application.ProviderService;
import com.jamjam.user.application.dto.CustomUserDetails;
import com.jamjam.user.domain.entity.ProviderEntity;
import com.jamjam.user.presentation.dto.request.ProviderRequest;
import com.jamjam.user.presentation.dto.response.ProviderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {
    private final ProviderService providerService;

    @PostMapping
    public ResponseEntity<ResponseDto<Void>> createProvider(
            @CurrentUser CustomUserDetails user,
            @RequestBody ProviderRequest request
    ) {
        providerService.createProvider(user.getUserId(), request);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }

    @GetMapping
    public ResponseEntity<ProviderResponse> getProvider(
            @CurrentUser CustomUserDetails user
    ) {
        return providerService.getProvider(user.getUserId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping
    public ResponseEntity<ProviderResponse> updateProvider(
            @CurrentUser CustomUserDetails user,
            @RequestBody ProviderRequest request
    ) {
        var entity = providerService.updateProvider(user.getUserId(), request);
        var response = providerService.getProvider(entity.getId()).orElse(null);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProvider(
            @CurrentUser CustomUserDetails user
    ) {
        providerService.deleteProvider(user.getUserId());
        return ResponseEntity.noContent().build();
    }
} 
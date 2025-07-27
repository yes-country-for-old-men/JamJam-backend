package com.jamjam.notify.dto;

import lombok.Getter;

@Getter
public class FcmTokenRequest {
    private String device;
    private String token;
}

package com.jamjam.chat.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamjam.chat.application.ChatService;
import com.jamjam.chat.domain.entity.MessageType;
import com.jamjam.chat.presentation.dto.res.ChatFileUploadRes;
import com.jamjam.infra.jwt.dto.JwtUserDto;
import com.jamjam.user.application.dto.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    @MockBean
    private com.jamjam.chat.util.EventBroadcaster eventBroadcaster;

    private CustomUserDetails mockUser;

    @BeforeEach
    void setUp() {
        JwtUserDto jwtUser = JwtUserDto.builder()
                .userId(1L)
                .loginId("testuser")
                .password("password")
                .role("CLIENT")
                .build();

        mockUser = new CustomUserDetails(jwtUser);
    }

    @Test
    @DisplayName("채팅 파일 업로드 API - 성공 (이미지)")
    @WithMockUser
    void uploadChatFile_Image_Success() throws Exception {
        // given
        Long roomId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        ChatFileUploadRes response = new ChatFileUploadRes(
                "https://jamjam2025.s3.amazonaws.com/chat-files/test-image.jpg",
                "test-image.jpg",
                file.getSize(),
                MessageType.IMAGE
        );

        given(chatService.uploadChatFile(eq(roomId), anyString(), any())).willReturn(response);

        // when & then
        mockMvc.perform(multipart("/api/chat/upload-file")
                        .file(file)
                        .param("roomId", roomId.toString())
                        .with(user(mockUser))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileUrl").value("https://jamjam2025.s3.amazonaws.com/chat-files/test-image.jpg"))
                .andExpect(jsonPath("$.data.fileName").value("test-image.jpg"))
                .andExpect(jsonPath("$.data.messageType").value("IMAGE"))
                .andExpect(jsonPath("$.data.fileSize").value(file.getSize()));

        verify(chatService, times(1)).uploadChatFile(eq(roomId), anyString(), any());
    }

    @Test
    @DisplayName("채팅 파일 업로드 API - 성공 (PDF)")
    @WithMockUser
    void uploadChatFile_PDF_Success() throws Exception {
        // given
        Long roomId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "test pdf content".getBytes()
        );

        ChatFileUploadRes response = new ChatFileUploadRes(
                "https://jamjam2025.s3.amazonaws.com/chat-files/document.pdf",
                "document.pdf",
                file.getSize(),
                MessageType.FILE
        );

        given(chatService.uploadChatFile(eq(roomId), anyString(), any())).willReturn(response);

        // when & then
        mockMvc.perform(multipart("/api/chat/upload-file")
                        .file(file)
                        .param("roomId", roomId.toString())
                        .with(user(mockUser))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileUrl").value("https://jamjam2025.s3.amazonaws.com/chat-files/document.pdf"))
                .andExpect(jsonPath("$.data.fileName").value("document.pdf"))
                .andExpect(jsonPath("$.data.messageType").value("FILE"));

        verify(chatService, times(1)).uploadChatFile(eq(roomId), anyString(), any());
    }

    @Test
    @DisplayName("채팅 파일 업로드 API - roomId 파라미터 누락")
    @WithMockUser
    void uploadChatFile_MissingRoomId() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/chat/upload-file")
                        .file(file)
                        .with(user(mockUser))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("채팅 파일 업로드 API - 파일 누락")
    @WithMockUser
    void uploadChatFile_MissingFile() throws Exception {
        // given
        Long roomId = 1L;

        // when & then
        mockMvc.perform(multipart("/api/chat/upload-file")
                        .param("roomId", roomId.toString())
                        .with(user(mockUser))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("채팅 파일 업로드 API - 인증 없이 요청")
    void uploadChatFile_Unauthorized() throws Exception {
        // given
        Long roomId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/chat/upload-file")
                        .file(file)
                        .param("roomId", roomId.toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("채팅 파일 업로드 API - PNG 파일")
    @WithMockUser
    void uploadChatFile_PNG_Success() throws Exception {
        // given
        Long roomId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "screenshot.png",
                "image/png",
                "png content".getBytes()
        );

        ChatFileUploadRes response = new ChatFileUploadRes(
                "https://jamjam2025.s3.amazonaws.com/chat-files/screenshot.png",
                "screenshot.png",
                file.getSize(),
                MessageType.IMAGE
        );

        given(chatService.uploadChatFile(eq(roomId), anyString(), any())).willReturn(response);

        // when & then
        mockMvc.perform(multipart("/api/chat/upload-file")
                        .file(file)
                        .param("roomId", roomId.toString())
                        .with(user(mockUser))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageType").value("IMAGE"));
    }

    @Test
    @DisplayName("채팅 파일 업로드 API - DOCX 파일")
    @WithMockUser
    void uploadChatFile_DOCX_Success() throws Exception {
        // given
        Long roomId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "docx content".getBytes()
        );

        ChatFileUploadRes response = new ChatFileUploadRes(
                "https://jamjam2025.s3.amazonaws.com/chat-files/report.docx",
                "report.docx",
                file.getSize(),
                MessageType.FILE
        );

        given(chatService.uploadChatFile(eq(roomId), anyString(), any())).willReturn(response);

        // when & then
        mockMvc.perform(multipart("/api/chat/upload-file")
                        .file(file)
                        .param("roomId", roomId.toString())
                        .with(user(mockUser))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageType").value("FILE"));
    }

    @Test
    @DisplayName("채팅 파일 업로드 API - ZIP 파일")
    @WithMockUser
    void uploadChatFile_ZIP_Success() throws Exception {
        // given
        Long roomId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "archive.zip",
                "application/zip",
                "zip content".getBytes()
        );

        ChatFileUploadRes response = new ChatFileUploadRes(
                "https://jamjam2025.s3.amazonaws.com/chat-files/archive.zip",
                "archive.zip",
                file.getSize(),
                MessageType.FILE
        );

        given(chatService.uploadChatFile(eq(roomId), anyString(), any())).willReturn(response);

        // when & then
        mockMvc.perform(multipart("/api/chat/upload-file")
                        .file(file)
                        .param("roomId", roomId.toString())
                        .with(user(mockUser))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageType").value("FILE"));
    }

    @Test
    @DisplayName("채팅 파일 업로드 API - 한글 파일명")
    @WithMockUser
    void uploadChatFile_KoreanFileName_Success() throws Exception {
        // given
        Long roomId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "채팅_이미지.jpg",
                "image/jpeg",
                "image content".getBytes()
        );

        ChatFileUploadRes response = new ChatFileUploadRes(
                "https://jamjam2025.s3.amazonaws.com/chat-files/채팅_이미지.jpg",
                "채팅_이미지.jpg",
                file.getSize(),
                MessageType.IMAGE
        );

        given(chatService.uploadChatFile(eq(roomId), anyString(), any())).willReturn(response);

        // when & then
        mockMvc.perform(multipart("/api/chat/upload-file")
                        .file(file)
                        .param("roomId", roomId.toString())
                        .with(user(mockUser))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value("채팅_이미지.jpg"));
    }
}

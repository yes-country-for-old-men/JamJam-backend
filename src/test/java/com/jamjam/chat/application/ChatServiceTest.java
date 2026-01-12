package com.jamjam.chat.application;

import com.jamjam.chat.domain.entity.*;
import com.jamjam.chat.domain.repository.ChatMessageRepository;
import com.jamjam.chat.domain.repository.ChatRoomParticipantRepository;
import com.jamjam.chat.domain.repository.ChatRoomRepository;
import com.jamjam.chat.domain.repository.ChatRoomReadStatusRepository;
import com.jamjam.chat.presentation.dto.res.ChatFileUploadRes;
import com.jamjam.global.exception.ApiException;
import com.jamjam.service.util.S3Uploader;
import com.jamjam.user.domain.entity.Gender;
import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.entity.UserRole;
import com.jamjam.user.domain.repository.UserRepository;
import com.jamjam.util.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRoomRepository roomRepo;

    @Mock
    private ChatRoomParticipantRepository partRepo;

    @Mock
    private ChatMessageRepository msgRepo;

    @Mock
    private ChatRoomReadStatusRepository readStatusRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private NotificationSender notificationSender;

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3Uploader s3Uploader;

    @InjectMocks
    private ChatService chatService;

    private ChatRoomEntity chatRoom;
    private UserEntity user;
    private ChatRoomParticipantEntity participant;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .name("테스트유저")
                .loginId("testuser")
                .password("password")
                .phoneNumber("01012345678")
                .isPhoneVerified(true)
                .gender(Gender.MALE)
                .birth(LocalDate.of(1990, 1, 1))
                .createAt(LocalDate.now())
                .role(UserRole.CLIENT)
                .nickname("testUser")
                .build();

        participant = ChatRoomParticipantEntity.builder()
                .userId("1")
                .build();

        chatRoom = ChatRoomEntity.builder()
                .groupChat(false)
                .createdAt(LocalDateTime.now())
                .participants(java.util.List.of(participant))
                .build();
    }

    @Test
    @DisplayName("이미지 파일 업로드 성공")
    void uploadChatFile_Image_Success() throws IOException {
        // given
        Long roomId = 1L;
        String userId = "1";
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        String expectedUrl = "https://jamjam2025.s3.amazonaws.com/chat-files/test-image.jpg";

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));
        given(s3Uploader.upload(imageFile, "chat-files")).willReturn(expectedUrl);

        // when
        ChatFileUploadRes result = chatService.uploadChatFile(roomId, userId, imageFile);

        // then
        assertThat(result).isNotNull();
        assertThat(result.fileUrl()).isEqualTo(expectedUrl);
        assertThat(result.fileName()).isEqualTo("test-image.jpg");
        assertThat(result.messageType()).isEqualTo(MessageType.IMAGE);
        assertThat(result.fileSize()).isEqualTo(imageFile.getSize());

        verify(s3Uploader, times(1)).upload(imageFile, "chat-files");
    }

    @Test
    @DisplayName("문서 파일 업로드 성공 - PDF")
    void uploadChatFile_Document_Success() throws IOException {
        // given
        Long roomId = 1L;
        String userId = "1";
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "test pdf content".getBytes()
        );
        String expectedUrl = "https://jamjam2025.s3.amazonaws.com/chat-files/document.pdf";

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));
        given(s3Uploader.upload(pdfFile, "chat-files")).willReturn(expectedUrl);

        // when
        ChatFileUploadRes result = chatService.uploadChatFile(roomId, userId, pdfFile);

        // then
        assertThat(result).isNotNull();
        assertThat(result.fileUrl()).isEqualTo(expectedUrl);
        assertThat(result.fileName()).isEqualTo("document.pdf");
        assertThat(result.messageType()).isEqualTo(MessageType.FILE);
    }

    @Test
    @DisplayName("파일 업로드 실패 - 채팅방이 존재하지 않음")
    void uploadChatFile_RoomNotFound() {
        // given
        Long roomId = 999L;
        String userId = "1";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        given(roomRepo.findById(roomId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.uploadChatFile(roomId, userId, file))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("채팅방이 존재하지 않습니다");
    }

    @Test
    @DisplayName("파일 업로드 실패 - 참여자가 아님")
    void uploadChatFile_NotParticipant() {
        // given
        Long roomId = 1L;
        String userId = "999";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.uploadChatFile(roomId, userId, file))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("참가자가 아닙니다");
    }

    @Test
    @DisplayName("파일 업로드 실패 - 파일이 비어있음")
    void uploadChatFile_EmptyFile() {
        // given
        Long roomId = 1L;
        String userId = "1";
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));

        // when & then
        assertThatThrownBy(() -> chatService.uploadChatFile(roomId, userId, emptyFile))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("파일이 제공되지 않았습니다");
    }

    @Test
    @DisplayName("파일 업로드 실패 - 파일 크기 초과 (10MB)")
    void uploadChatFile_FileSizeExceeded() {
        // given
        Long roomId = 1L;
        String userId = "1";
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-file.jpg",
                "image/jpeg",
                largeContent
        );

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));

        // when & then
        assertThatThrownBy(() -> chatService.uploadChatFile(roomId, userId, largeFile))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("10MB를 초과할 수 없습니다");
    }

    @Test
    @DisplayName("파일 업로드 실패 - 지원하지 않는 파일 타입")
    void uploadChatFile_InvalidFileType() {
        // given
        Long roomId = 1L;
        String userId = "1";
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "script.exe",
                "application/x-msdownload",
                "test".getBytes()
        );

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));

        // when & then
        assertThatThrownBy(() -> chatService.uploadChatFile(roomId, userId, invalidFile))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("지원하지 않는 파일 형식입니다");
    }

    @Test
    @DisplayName("파일 업로드 실패 - S3 업로드 실패")
    void uploadChatFile_S3UploadFailed() throws IOException {
        // given
        Long roomId = 1L;
        String userId = "1";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));
        given(s3Uploader.upload(file, "chat-files")).willThrow(new IOException("S3 connection failed"));

        // when & then
        assertThatThrownBy(() -> chatService.uploadChatFile(roomId, userId, file))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("파일 업로드에 실패했습니다");
    }

    @Test
    @DisplayName("이미지 타입 검증 - PNG")
    void validateFileType_PNG() throws IOException {
        // given
        Long roomId = 1L;
        String userId = "1";
        MockMultipartFile pngFile = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                "test".getBytes()
        );

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));
        given(s3Uploader.upload(any(), anyString())).willReturn("https://s3.amazonaws.com/test.png");

        // when
        ChatFileUploadRes result = chatService.uploadChatFile(roomId, userId, pngFile);

        // then
        assertThat(result.messageType()).isEqualTo(MessageType.IMAGE);
    }

    @Test
    @DisplayName("이미지 타입 검증 - GIF")
    void validateFileType_GIF() throws IOException {
        // given
        Long roomId = 1L;
        String userId = "1";
        MockMultipartFile gifFile = new MockMultipartFile(
                "file",
                "animation.gif",
                "image/gif",
                "test".getBytes()
        );

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));
        given(s3Uploader.upload(any(), anyString())).willReturn("https://s3.amazonaws.com/test.gif");

        // when
        ChatFileUploadRes result = chatService.uploadChatFile(roomId, userId, gifFile);

        // then
        assertThat(result.messageType()).isEqualTo(MessageType.IMAGE);
    }

    @Test
    @DisplayName("문서 타입 검증 - DOCX")
    void validateFileType_DOCX() throws IOException {
        // given
        Long roomId = 1L;
        String userId = "1";
        MockMultipartFile docxFile = new MockMultipartFile(
                "file",
                "document.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "test".getBytes()
        );

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));
        given(s3Uploader.upload(any(), anyString())).willReturn("https://s3.amazonaws.com/test.docx");

        // when
        ChatFileUploadRes result = chatService.uploadChatFile(roomId, userId, docxFile);

        // then
        assertThat(result.messageType()).isEqualTo(MessageType.FILE);
    }

    @Test
    @DisplayName("문서 타입 검증 - ZIP")
    void validateFileType_ZIP() throws IOException {
        // given
        Long roomId = 1L;
        String userId = "1";
        MockMultipartFile zipFile = new MockMultipartFile(
                "file",
                "archive.zip",
                "application/zip",
                "test".getBytes()
        );

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));
        given(s3Uploader.upload(any(), anyString())).willReturn("https://s3.amazonaws.com/test.zip");

        // when
        ChatFileUploadRes result = chatService.uploadChatFile(roomId, userId, zipFile);

        // then
        assertThat(result.messageType()).isEqualTo(MessageType.FILE);
    }

    @Test
    @DisplayName("여러 파일 업로드 성공 - 2개 파일")
    void uploadChatFiles_Two_Success() throws IOException {
        // given
        Long roomId = 1L;
        String userId = "1";

        MockMultipartFile imageFile = new MockMultipartFile(
                "files",
                "image1.jpg",
                "image/jpeg",
                "image content".getBytes()
        );

        MockMultipartFile pdfFile = new MockMultipartFile(
                "files",
                "document.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );

        List<MultipartFile> files = List.of(imageFile, pdfFile);

        String imageUrl = "https://jamjam2025.s3.amazonaws.com/chat-files/image1.jpg";
        String pdfUrl = "https://jamjam2025.s3.amazonaws.com/chat-files/document.pdf";

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));
        given(s3Uploader.upload(imageFile, "chat-files")).willReturn(imageUrl);
        given(s3Uploader.upload(pdfFile, "chat-files")).willReturn(pdfUrl);

        // when
        List<ChatFileUploadRes> results = chatService.uploadChatFiles(roomId, userId, files);

        // then
        assertThat(results).hasSize(2);

        assertThat(results.get(0).fileUrl()).isEqualTo(imageUrl);
        assertThat(results.get(0).fileName()).isEqualTo("image1.jpg");
        assertThat(results.get(0).messageType()).isEqualTo(MessageType.IMAGE);
        assertThat(results.get(0).fileSize()).isEqualTo(imageFile.getSize());

        assertThat(results.get(1).fileUrl()).isEqualTo(pdfUrl);
        assertThat(results.get(1).fileName()).isEqualTo("document.pdf");
        assertThat(results.get(1).messageType()).isEqualTo(MessageType.FILE);
        assertThat(results.get(1).fileSize()).isEqualTo(pdfFile.getSize());

        verify(s3Uploader, times(1)).upload(imageFile, "chat-files");
        verify(s3Uploader, times(1)).upload(pdfFile, "chat-files");
    }

    @Test
    @DisplayName("여러 파일 업로드 성공 - 3개 파일 (이미지 2개 + 문서 1개)")
    void uploadChatFiles_Three_Success() throws IOException {
        // given
        Long roomId = 1L;
        String userId = "1";

        MockMultipartFile image1 = new MockMultipartFile(
                "files",
                "photo1.jpg",
                "image/jpeg",
                "photo1 content".getBytes()
        );

        MockMultipartFile image2 = new MockMultipartFile(
                "files",
                "photo2.png",
                "image/png",
                "photo2 content".getBytes()
        );

        MockMultipartFile doc = new MockMultipartFile(
                "files",
                "report.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "docx content".getBytes()
        );

        List<MultipartFile> files = List.of(image1, image2, doc);

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));
        given(s3Uploader.upload(any(), eq("chat-files")))
                .willReturn("https://s3.amazonaws.com/file1.jpg")
                .willReturn("https://s3.amazonaws.com/file2.png")
                .willReturn("https://s3.amazonaws.com/file3.docx");

        // when
        List<ChatFileUploadRes> results = chatService.uploadChatFiles(roomId, userId, files);

        // then
        assertThat(results).hasSize(3);
        assertThat(results.get(0).messageType()).isEqualTo(MessageType.IMAGE);
        assertThat(results.get(1).messageType()).isEqualTo(MessageType.IMAGE);
        assertThat(results.get(2).messageType()).isEqualTo(MessageType.FILE);

        verify(s3Uploader, times(3)).upload(any(), eq("chat-files"));
    }

    @Test
    @DisplayName("여러 파일 업로드 실패 - 파일 리스트가 비어있음")
    void uploadChatFiles_EmptyList() {
        // given
        Long roomId = 1L;
        String userId = "1";
        List<MultipartFile> emptyFiles = List.of();

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));

        // when & then
        assertThatThrownBy(() -> chatService.uploadChatFiles(roomId, userId, emptyFiles))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("파일이 제공되지 않았습니다");
    }

    @Test
    @DisplayName("여러 파일 업로드 실패 - 파일 개수 초과 (11개)")
    void uploadChatFiles_TooManyFiles() {
        // given
        Long roomId = 1L;
        String userId = "1";

        List<MultipartFile> tooManyFiles = List.of(
                new MockMultipartFile("files", "file1.jpg", "image/jpeg", "content".getBytes()),
                new MockMultipartFile("files", "file2.jpg", "image/jpeg", "content".getBytes()),
                new MockMultipartFile("files", "file3.jpg", "image/jpeg", "content".getBytes()),
                new MockMultipartFile("files", "file4.jpg", "image/jpeg", "content".getBytes()),
                new MockMultipartFile("files", "file5.jpg", "image/jpeg", "content".getBytes()),
                new MockMultipartFile("files", "file6.jpg", "image/jpeg", "content".getBytes()),
                new MockMultipartFile("files", "file7.jpg", "image/jpeg", "content".getBytes()),
                new MockMultipartFile("files", "file8.jpg", "image/jpeg", "content".getBytes()),
                new MockMultipartFile("files", "file9.jpg", "image/jpeg", "content".getBytes()),
                new MockMultipartFile("files", "file10.jpg", "image/jpeg", "content".getBytes()),
                new MockMultipartFile("files", "file11.jpg", "image/jpeg", "content".getBytes())
        );

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));

        // when & then
        assertThatThrownBy(() -> chatService.uploadChatFiles(roomId, userId, tooManyFiles))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("파일은 최대 10개까지 업로드할 수 있습니다");
    }

    @Test
    @DisplayName("여러 파일 업로드 실패 - 파일 중 하나가 비어있음")
    void uploadChatFiles_OneFileEmpty() {
        // given
        Long roomId = 1L;
        String userId = "1";

        MockMultipartFile validFile = new MockMultipartFile(
                "files",
                "valid.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        MockMultipartFile emptyFile = new MockMultipartFile(
                "files",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        List<MultipartFile> files = List.of(validFile, emptyFile);

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));

        // when & then
        assertThatThrownBy(() -> chatService.uploadChatFiles(roomId, userId, files))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("파일이 제공되지 않았습니다");
    }

    @Test
    @DisplayName("여러 파일 업로드 실패 - 파일 중 하나가 크기 초과")
    void uploadChatFiles_OneFileTooLarge() {
        // given
        Long roomId = 1L;
        String userId = "1";

        MockMultipartFile validFile = new MockMultipartFile(
                "files",
                "small.jpg",
                "image/jpeg",
                "small content".getBytes()
        );

        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "files",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        List<MultipartFile> files = List.of(validFile, largeFile);

        given(roomRepo.findById(roomId)).willReturn(Optional.of(chatRoom));
        given(partRepo.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(participant));

        // when & then
        assertThatThrownBy(() -> chatService.uploadChatFiles(roomId, userId, files))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("10MB를 초과할 수 없습니다");
    }
}

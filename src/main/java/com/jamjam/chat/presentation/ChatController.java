    package com.jamjam.chat.presentation;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.jamjam.chat.domain.entity.ChatMessageEntity;
    import com.jamjam.chat.domain.entity.MessageType;
    import com.jamjam.chat.presentation.dto.SocketEvent;
    import com.jamjam.chat.presentation.dto.req.MarkAsReadReq;
    import com.jamjam.chat.presentation.dto.req.MessageReadReq;
    import com.jamjam.chat.presentation.dto.req.SendMessageReq;
    import com.jamjam.chat.presentation.dto.res.ChatFileUploadRes;
    import com.jamjam.chat.presentation.dto.res.ChatHistoryRes;
    import com.jamjam.chat.presentation.dto.res.ChatRoomListRes;
    import com.jamjam.chat.presentation.dto.res.CreateRoomRes;
    import com.jamjam.chat.application.ChatService;
    import com.jamjam.global.annotation.CurrentUser;
    import com.jamjam.global.dto.ResponseDto;
    import com.jamjam.global.dto.SuccessMessage;
    import com.jamjam.user.application.dto.CustomUserDetails;
    import com.jamjam.chat.util.EventBroadcaster;
    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.Parameter;
    import io.swagger.v3.oas.annotations.media.Content;
    import io.swagger.v3.oas.annotations.media.ExampleObject;
    import io.swagger.v3.oas.annotations.media.Schema;
    import io.swagger.v3.oas.annotations.responses.ApiResponse;
    import io.swagger.v3.oas.annotations.responses.ApiResponses;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.web.PageableDefault;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.messaging.handler.annotation.MessageMapping;
    import org.springframework.messaging.handler.annotation.Payload;
    import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;

    import java.util.ArrayList;
    import java.util.List;

    @Slf4j
    @RequiredArgsConstructor
    @RestController
    @RequestMapping("/api/chat")
    public class ChatController {
    
        private final ChatService chatService;
        private final EventBroadcaster eventBroadcaster;
        private final ObjectMapper objectMapper;

        @MessageMapping("/chat")
        public void handleChatEvent(@Payload SocketEvent<?> event, SimpMessageHeaderAccessor accessor) {
            String userId = (String) accessor.getSessionAttributes().get("userId");
            log.info("convertAndSend userId: {}", userId);

            switch (event.type()) {
                case SEND_MESSAGE -> {
                    SendMessageReq req = convert(event.content(), SendMessageReq.class);
                    ChatMessageEntity savedMsg = chatService.sendMessage(
                            req.roomId(),
                            userId,
                            req.message(),
                            req.messageType() != null ? req.messageType() : MessageType.TEXT,
                            req.files()
                    );
                    eventBroadcaster.broadcastNewMessage(savedMsg, userId);
                }
                case MESSAGE_READ -> {
                    MessageReadReq req = convert(event.content(), MessageReadReq.class);
                    chatService.markRoomAsRead(req.roomId(), userId, req.lastReadMessageId());
                    eventBroadcaster.broadcastMessageRead(req.roomId(), req.lastReadMessageId());
                }
            }
        }

        @PostMapping(value = "/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(
                summary = "채팅 파일 업로드",
                description = """
                        채팅에서 사용할 파일을 S3에 업로드하고 URL을 반환합니다.

                        **지원 파일 타입:**
                        - 이미지: jpg, jpeg, png, gif
                        - 문서: pdf, doc, docx, xls, xlsx, zip, txt

                        **파일 크기 제한:** 각 파일 최대 10MB

                        **파일 개수:** 한 번에 최대 10개까지 업로드 가능

                        **사용 방법:**
                        1. 이 API로 파일을 업로드하여 fileUrl, fileName, fileSize, messageType을 받습니다
                        2. WebSocket(/app/chat)으로 메시지를 전송할 때 받은 정보를 함께 전달합니다
                        
                        **메세지 타입:**
                        - TEXT, IMAGE, FILE

                        **WebSocket 메시지 예시:**
                        ```json
                        {
                          "type": "SEND_MESSAGE",
                          "content": {
                            "roomId": 1,
                            "message": "사진 보냅니다",
                            "messageType": "IMAGE",
                            "files": [
                              {
                                "fileUrl": "https://jamjam2025.s3.amazonaws.com/chat-files/xxx.jpg",
                                "fileName": "photo.jpg",
                                "fileSize": 102400,
                                "fileType": "IMAGE"
                              },
                              {
                                "fileUrl": "https://jamjam2025.s3.amazonaws.com/chat-files/yyy.png",
                                "fileName": "photo2.png",
                                "fileSize": 204800,
                                "fileType": "IMAGE"
                              }
                            ]
                          }
                        }
                        ```
                        """
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "파일 업로드 성공",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ChatFileUploadRes.class),
                                examples = @ExampleObject(
                                        name = "이미지 업로드 성공",
                                        value = """
                                                {
                                                  "success": true,
                                                  "message": "작업이 성공적으로 완료되었습니다.",
                                                  "data": [
                                                    {
                                                      "fileUrl": "https://jamjam2025.s3.amazonaws.com/chat-files/550e8400-e29b-41d4-a716-446655440000_photo.jpg",
                                                      "fileName": "photo.jpg",
                                                      "fileSize": 102400,
                                                      "messageType": "IMAGE"
                                                    }
                                                  ]
                                                }
                                                """
                                )
                        )
                )
        })
        public ResponseEntity<ResponseDto<List<ChatFileUploadRes>>> uploadChatFile(
                @Parameter(hidden = true) @CurrentUser CustomUserDetails user,
                @Parameter(
                        description = "업로드할 파일들 (이미지: jpg/jpeg/png/gif, 문서: pdf/doc/docx/xls/xlsx/zip/txt)",
                        required = true,
                        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
                )
                @RequestPart("files") List<MultipartFile> files,
                @Parameter(description = "채팅방 ID", required = true, example = "1")
                @RequestParam Long roomId
        ) {
            List<ChatFileUploadRes> results = chatService.uploadChatFiles(roomId, String.valueOf(user.getUserId()), files);
            return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, results));
        }

        @PostMapping("/room")
        public ResponseEntity<ResponseDto<CreateRoomRes>> makeRoom(
                @CurrentUser CustomUserDetails user,
                @RequestParam Long otherId
        ){
            List<String> userIds = new ArrayList<>();
            userIds.add(Long.toString(otherId));
            userIds.add(String.valueOf(user.getUserId()));
    
            return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                    chatService.createRoom(false, userIds)));
        }

        @GetMapping("/rooms/{chatRoomId}/messages")
        public ResponseEntity<ResponseDto<ChatHistoryRes>> getMessages(
                @CurrentUser CustomUserDetails user,
                @PathVariable Long chatRoomId,
                @PageableDefault Pageable pageable
        ) {
            ChatHistoryRes slice = chatService.getHistory(chatRoomId, pageable, String.valueOf(user.getUserId()));
            return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, slice));
        }
        @PutMapping("/rooms/{chatRoomId}/read")
        public ResponseEntity<ResponseDto<Void>> markRoomAsRead(
                @CurrentUser CustomUserDetails user,
                @PathVariable Long chatRoomId,
                @RequestBody MarkAsReadReq request
        ) {
            chatService.markRoomAsRead(chatRoomId, String.valueOf(user.getUserId()), request.lastReadMessageId());
            eventBroadcaster.broadcastMessageRead(chatRoomId, request.lastReadMessageId());
            return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
        }

        @GetMapping("/rooms")
        public ResponseEntity<ResponseDto<ChatRoomListRes>> getChatRooms(
                @CurrentUser CustomUserDetails user,
                @PageableDefault Pageable pageable
        ) {
            ChatRoomListRes res = chatService.getChatRooms(String.valueOf(user.getUserId()), pageable);
            return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, res));
        }

        @DeleteMapping("/rooms/{chatRoomId}")
        public ResponseEntity<ResponseDto<Void>> leaveRoom(
                @CurrentUser CustomUserDetails user,
                @PathVariable Long chatRoomId
        ) {
            chatService.leaveRoom(chatRoomId, String.valueOf(user.getUserId()));
            return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
        }

        private <T> T convert(Object content, Class<T> clazz) {
            return objectMapper.convertValue(content, clazz);
        }
    }

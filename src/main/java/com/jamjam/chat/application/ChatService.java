package com.jamjam.chat.application;

import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.chat.domain.entity.ChatRoomEntity;
import com.jamjam.chat.domain.entity.ChatRoomParticipantEntity;
import com.jamjam.chat.domain.entity.ChatRoomReadStatusEntity;
import com.jamjam.chat.domain.entity.MessageType;
import com.jamjam.chat.domain.repository.ChatMessageRepository;
import com.jamjam.chat.domain.repository.ChatRoomParticipantRepository;
import com.jamjam.chat.domain.repository.ChatRoomRepository;
import com.jamjam.chat.domain.repository.ChatRoomReadStatusRepository;
import com.jamjam.chat.exception.ChatError;
import com.jamjam.chat.presentation.dto.res.ChatFileUploadRes;
import com.jamjam.chat.presentation.dto.res.ChatHistoryRes;
import com.jamjam.chat.presentation.dto.res.ChatRoomListRes;
import com.jamjam.chat.presentation.dto.res.CreateRoomRes;
import com.jamjam.chat.domain.entity.ChatFileInfo;
import com.jamjam.chat.presentation.dto.req.SendMessageReq.FileInfo;
import com.jamjam.global.dto.SliceInfo;
import com.jamjam.global.exception.ApiException;
import com.jamjam.notify.domain.entity.NotificationType;
import com.jamjam.service.util.S3Uploader;
import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.repository.UserRepository;
import com.jamjam.user.exception.UserError;
import com.jamjam.util.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository roomRepo;
    private final ChatRoomParticipantRepository partRepo;
    private final ChatMessageRepository msgRepo;
    private final ChatRoomReadStatusRepository readStatusRepo;
    private final UserRepository userRepo;
    private final NotificationSender notificationSender;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public ChatMessageEntity sendMessage(Long roomId, String senderId, String content) {
        return sendMessage(roomId, senderId, content, MessageType.TEXT, null);
    }

    @Transactional
    public ChatMessageEntity sendMessage(Long roomId, String senderId, String content,
                                         MessageType messageType,
                                         List<FileInfo> fileInfos) {
        ChatRoomEntity room = roomRepo.findById(roomId)
                .orElseThrow(() -> new ApiException(ChatError.ROOM_NOT_FOUND));

        UserEntity sender = userRepository.findById(Long.valueOf(senderId))
                .orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

        List<ChatFileInfo> files = new ArrayList<>();
        if (fileInfos != null && !fileInfos.isEmpty()) {
            files = fileInfos.stream()
                    .map(f -> ChatFileInfo.builder()
                            .fileUrl(f.fileUrl())
                            .fileName(f.fileName())
                            .fileSize(f.fileSize())
                            .fileType(f.fileType())
                            .build())
                    .collect(Collectors.toList());
        }

        ChatMessageEntity msg = ChatMessageEntity.builder()
                .room(room)
                .senderId(senderId)
                .senderName(sender.getNickname())
                .content(content)
                .sentAt(LocalDateTime.now())
                .messageType(messageType != null ? messageType : MessageType.TEXT)
                .files(files)
                .build();

        /*채팅방 참여자에게 푸시 알림 web 제외*/
        for (ChatRoomParticipantEntity participant : room.getParticipants()) {
            UserEntity receiver = userRepository.findById(Long.valueOf(participant.getUserId()))
                    .orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

            String notificationContent;
            if (messageType == MessageType.TEXT) {
                notificationContent = content;
            } else if (!files.isEmpty()) {
                notificationContent = "[" + messageType.name() + "] " + files.get(0).getFileName();
                if (files.size() > 1) {
                    notificationContent += " 외 " + (files.size() - 1) + "개";
                }
            } else {
                notificationContent = "[" + messageType.name() + "] 파일";
            }

            notificationSender.sendToUser(
                    receiver,
                    senderId,
                    notificationContent,
                    NotificationType.CHAT
            );
        }

        return msgRepo.save(msg);
    }

    @Transactional(readOnly = true)
    public ChatHistoryRes getHistory(Long roomId, Pageable pageable, String userId) {
        Slice<ChatMessageEntity> chatSlice = msgRepo.findByRoomIdOrderBySentAtDesc(roomId, pageable);
        List<ChatMessageEntity> chats = chatSlice.getContent();

        Map<String, String> userIdToNickname = userRepo.findAllById(
                chats.stream()
                        .map(ChatMessageEntity::getSenderId)
                        .map(Long::valueOf)
                        .collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(
                u -> u.getId().toString(),
                UserEntity::getNickname
        ));

        SliceInfo sliceInfo = SliceInfo.of(chatSlice.hasNext());
        return ChatHistoryRes.of(chats, sliceInfo, userId, userIdToNickname);
    }

    @Transactional
    public CreateRoomRes createRoom(boolean groupChat, List<String> userIds) {

        if (userIds.size() == 2) {
            Optional<ChatRoomEntity> existingRoom = roomRepo.findExistingDirectChat(
                    userIds.get(0), userIds.get(1));
            if (existingRoom.isPresent()) {
                return new CreateRoomRes(existingRoom.get().getId());
            }
        }

        ChatRoomEntity room = roomRepo.save(ChatRoomEntity.builder()
                .groupChat(groupChat)
                .createdAt(LocalDateTime.now())
                .build());

        userIds.forEach(uid ->
                partRepo.save(ChatRoomParticipantEntity.builder()
                        .room(room)
                        .userId(uid)
                        .build()));

        return new CreateRoomRes(room.getId());
    }

    @Transactional(readOnly = true)
    public ChatRoomListRes getChatRooms(String userId, Pageable pageable) {
        Page<ChatRoomParticipantEntity> page = partRepo.findByUserId(userId, pageable);

        List<ChatRoomListRes.ChatRoomSummary> roomSummaries = page.getContent().stream()
                .map(participant -> getChatRoomSummary(participant.getRoom().getId(), userId))
                .toList();

        return ChatRoomListRes.builder()
                .rooms(roomSummaries)
                .currentPage(pageable.getPageNumber() + 1)
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .build();
    }

    @Transactional
    public void markRoomAsRead(Long roomId, String userId, Long lastReadMessageId) {
        ChatRoomEntity room = roomRepo.findById(roomId)
                .orElseThrow(() -> new ApiException(ChatError.ROOM_NOT_FOUND));
        ChatRoomReadStatusEntity status = readStatusRepo.findByChatRoomAndUserId(room, userId)
                .orElse(ChatRoomReadStatusEntity.builder()
                        .chatRoom(room)
                        .userId(userId)
                        .lastReadMessageId(lastReadMessageId)
                        .updatedAt(LocalDateTime.now())
                        .build());
        status.updateLastRead(lastReadMessageId);
        readStatusRepo.save(status);
    }

    @Transactional(readOnly = true)
    public ChatRoomListRes.ChatRoomSummary getChatRoomSummary(Long roomId, String userId) {
        ChatRoomEntity room = roomRepo.findById(roomId)
                .orElseThrow(() -> new ApiException(ChatError.ROOM_NOT_FOUND));

        String opponentId = room.getParticipants().stream()
                .map(ChatRoomParticipantEntity::getUserId)
                .filter(id -> !id.equals(userId))
                .findFirst()
                .orElse(null);

        UserEntity opponent = opponentId != null
                ? userRepo.findById(Long.valueOf(opponentId)).orElse(null)
                : null;

        ChatMessageEntity lastMessage = msgRepo.findTopByRoomOrderBySentAtDesc(room);

        ChatRoomReadStatusEntity readStatus = readStatusRepo.findByChatRoomAndUserId(room, userId)
                .orElse(null);

        Long lastReadMessageId = readStatus != null ? readStatus.getLastReadMessageId() : 0L;

        int unreadCount = msgRepo.countByRoomIdAndIdGreaterThanAndSenderIdNot(
                room.getId(), lastReadMessageId, userId);

        return ChatRoomListRes.ChatRoomSummary.builder()
                .id(room.getId())
                .nickname(opponent != null ? opponent.getNickname() : null)
                .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                .lastMessageTime(lastMessage != null ? lastMessage.getSentAt() : null)
                .unreadCount(unreadCount)
                .profileUrl(opponent != null ? opponent.getProfileUrl() : null)
                .build();
    }

    @Transactional
    public void leaveRoom(Long roomId, String userId) {
        ChatRoomEntity room = roomRepo.findById(roomId)
                .orElseThrow(() -> new ApiException(ChatError.ROOM_NOT_FOUND));

        ChatRoomParticipantEntity participant = partRepo.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ApiException(ChatError.NOT_PARTICIPANT));
        partRepo.delete(participant);

        readStatusRepo.findByChatRoomAndUserId(room, userId)
                .ifPresent(readStatusRepo::delete);
    }

    @Transactional
    public ChatFileUploadRes uploadChatFile(Long roomId, String userId, MultipartFile file) {
        ChatRoomEntity room = roomRepo.findById(roomId)
                .orElseThrow(() -> new ApiException(ChatError.ROOM_NOT_FOUND));

        partRepo.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ApiException(ChatError.NOT_PARTICIPANT));

        if (file == null || file.isEmpty()) {
            throw new ApiException(ChatError.FILE_NOT_PROVIDED);
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new ApiException(ChatError.FILE_SIZE_EXCEEDED);
        }

        MessageType messageType = validateAndGetMessageType(file);

        try {
            String fileUrl = s3Uploader.upload(file, "chat-files");
            return new ChatFileUploadRes(
                    fileUrl,
                    file.getOriginalFilename(),
                    file.getSize(),
                    messageType
            );
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", e.getMessage());
            throw new ApiException(ChatError.FILE_UPLOAD_FAILED);
        }
    }

    @Transactional
    public List<ChatFileUploadRes> uploadChatFiles(Long roomId, String userId, List<MultipartFile> files) {
        ChatRoomEntity room = roomRepo.findById(roomId)
                .orElseThrow(() -> new ApiException(ChatError.ROOM_NOT_FOUND));

        partRepo.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ApiException(ChatError.NOT_PARTICIPANT));

        if (files == null || files.isEmpty()) {
            throw new ApiException(ChatError.FILE_NOT_PROVIDED);
        }

        if (files.size() > 10) {
            throw new ApiException(ChatError.TOO_MANY_FILES);
        }

        return files.stream()
                .map(file -> {
                    if (file.isEmpty()) {
                        throw new ApiException(ChatError.FILE_NOT_PROVIDED);
                    }

                    if (file.getSize() > 10 * 1024 * 1024) {
                        throw new ApiException(ChatError.FILE_SIZE_EXCEEDED);
                    }

                    MessageType messageType = validateAndGetMessageType(file);

                    try {
                        String fileUrl = s3Uploader.upload(file, "chat-files");
                        return new ChatFileUploadRes(
                                fileUrl,
                                file.getOriginalFilename(),
                                file.getSize(),
                                messageType
                        );
                    } catch (IOException e) {
                        log.error("파일 업로드 실패: {}", e.getMessage());
                        throw new ApiException(ChatError.FILE_UPLOAD_FAILED);
                    }
                })
                .collect(Collectors.toList());
    }

    private MessageType validateAndGetMessageType(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        if (contentType == null || fileName == null) {
            throw new ApiException(ChatError.INVALID_FILE_TYPE);
        }

        String extension = getFileExtension(fileName).toLowerCase();

        if (isImageType(contentType, extension)) {
            return MessageType.IMAGE;
        }

        if (isDocumentType(extension)) {
            return MessageType.FILE;
        }

        throw new ApiException(ChatError.INVALID_FILE_TYPE);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private boolean isImageType(String contentType, String extension) {
        List<String> imageExtensions = List.of("jpg", "jpeg", "png", "gif");
        return contentType.startsWith("image/") && imageExtensions.contains(extension);
    }

    private boolean isDocumentType(String extension) {
        List<String> documentExtensions = List.of("pdf", "doc", "docx", "xls", "xlsx", "zip", "txt");
        return documentExtensions.contains(extension);
    }
}
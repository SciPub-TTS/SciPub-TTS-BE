package com.swp.backend.service;

import com.swp.backend.exception.BusinessException;
import com.swp.backend.exception.ErrorCode;
import com.swp.backend.repository.MessageDAO;
import com.swp.backend.dto.chat.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageDAO messageDAO;

    public List<MessageResponse> takeAllMessageOfRequest(UUID requestId){

        if (requestId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_ID);
        }

        List<Object[]> rows = messageDAO.findAllMessageRaw(requestId.toString());

        return rows.stream()
                .map(r -> new MessageResponse(
                        toUuid(r[0]),                        // id
                        toUuid(r[1]),                        // senderId
                        (String) r[2],                       // senderName
                        (String) r[3],                       // senderRole
                        (String) r[4],                       // content
                        toLocalDateTime(r[5])                // sendAt
                ))
                .toList();
    }

    private UUID toUuid(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(value.toString());
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        throw new BusinessException(ErrorCode.UNSUPPORTED_DATE_TYPE, value.getClass().getName());
    }
}

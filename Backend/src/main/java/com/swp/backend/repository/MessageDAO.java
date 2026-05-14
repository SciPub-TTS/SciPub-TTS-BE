package com.swp.backend.repository;

import com.swp.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MessageDAO  extends JpaRepository<Message, UUID> {
    List<Message> findByRequestIdOrderBySendAtAsc(UUID chatId);

    @Query(value = """
        SELECT 
            m.id,
            m.sender_id,
            COALESCE(c.name, s.name) AS sender_name,
            m.sender_role,
            m.content,
            m.send_at
        FROM Message m
        LEFT JOIN Citizen c 
            ON m.sender_id = c.id AND m.sender_role = 'người dân'
        LEFT JOIN Staff s 
            ON m.sender_id = s.id 
            AND m.sender_role IN ('điều phối viên', 'cứu hộ')
        WHERE m.request_id = :requestId
        ORDER BY m.send_at
    """, nativeQuery = true)
    List<Object[]> findAllMessageRaw(String requestId);
}

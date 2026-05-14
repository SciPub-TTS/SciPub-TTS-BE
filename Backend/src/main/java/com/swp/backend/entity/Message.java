package com.swp.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Message")
@Getter
@Setter
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Column(name = "sender_id")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID senderId;

    @Column(name = "sender_role", nullable = false, length = 20)
    private String senderRole; // user, coordinator, rescue team

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "send_at")
    private LocalDateTime sendAt = LocalDateTime.now();
}

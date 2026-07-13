package com.brotherhood.scipubtts.admin.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.UUID;

@SuppressWarnings("unused")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private UUID id;

    private String email;

    private String firstName;

    private String lastName;

    private String role;

    private boolean emailVerified;

    private boolean googleLinked;

    private boolean banned;

    private OffsetDateTime createdAt;

    private long topicCount;

    private long authorCount;

    public UUID id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public String role() {
        return role;
    }

    public boolean banned() {
        return banned;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }

    public long topicCount() {
        return topicCount;
    }

    public long authorCount() {
        return authorCount;
    }
}


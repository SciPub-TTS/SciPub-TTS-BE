package com.brotherhood.scipubtts.follow.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.brotherhood.scipubtts.follow.entity.FollowTargetType;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FollowResponse {
    private UUID id;

    private FollowTargetType targetType;

    private String targetOpenAlexId;

    private String displayName;

    private OffsetDateTime createdAt;

    public UUID id() {
        return id;
    }

    public FollowTargetType targetType() {
        return targetType;
    }

    public String targetOpenAlexId() {
        return targetOpenAlexId;
    }

    public String displayName() {
        return displayName;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }
}


package com.brotherhood.scipubtts.follow.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.brotherhood.scipubtts.follow.entity.FollowTargetType;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FollowStatusResponse {
    private boolean followed;

    private UUID followId;

    private FollowTargetType targetType;

    private String targetOpenAlexId;

    public boolean followed() {
        return followed;
    }

    public UUID followId() {
        return followId;
    }

    public FollowTargetType targetType() {
        return targetType;
    }

    public String targetOpenAlexId() {
        return targetOpenAlexId;
    }
}


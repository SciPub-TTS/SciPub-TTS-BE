package com.brotherhood.scipubtts.socialhub.dto.response;

public record LikeToggleResponse(
        boolean liked,
        int likeCount
) {
}

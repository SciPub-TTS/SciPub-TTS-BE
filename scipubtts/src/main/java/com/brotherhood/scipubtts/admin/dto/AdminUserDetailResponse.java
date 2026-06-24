package com.brotherhood.scipubtts.admin.dto;

public record AdminUserDetailResponse(
        AdminUserDetailUserResponse user,
        AdminUserProfileResponse profile,
        AdminUserActivityResponse activity
) {
}

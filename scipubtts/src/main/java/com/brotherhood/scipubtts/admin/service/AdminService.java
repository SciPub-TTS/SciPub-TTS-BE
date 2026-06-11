package com.brotherhood.scipubtts.admin.service;

import com.brotherhood.scipubtts.admin.dto.AdminUserResponse;

import java.util.UUID;

public interface AdminService {

    AdminUserResponse banUser(UUID adminId, UUID userId);

    AdminUserResponse unbanUser(UUID adminId, UUID userId);
}

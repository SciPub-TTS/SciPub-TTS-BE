package com.brotherhood.scipubtts.user.service;

import com.brotherhood.scipubtts.user.dto.response.DashboardResponse;

import java.util.UUID;

public interface DashboardService {

    DashboardResponse getSummary(UUID userId);

}

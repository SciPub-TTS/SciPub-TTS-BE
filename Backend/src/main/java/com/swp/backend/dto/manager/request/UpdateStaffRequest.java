package com.swp.backend.dto.manager.request;

import java.math.BigDecimal;

public record UpdateStaffRequest(
        String name,
        String phone,
        String password,
        String role,
        String state,
        String teamName,
        Integer teamSize,
        BigDecimal latitude,
        BigDecimal longitude
) {
}

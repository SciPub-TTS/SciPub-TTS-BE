package com.swp.backend.dto.manager.request;

import java.math.BigDecimal;

public record CreateStaffRequest(
    String name,
    String phone,
    String password,
    String role,
    String teamName,
    int teamSize,
    BigDecimal latitude,
    BigDecimal longitude
) {
}

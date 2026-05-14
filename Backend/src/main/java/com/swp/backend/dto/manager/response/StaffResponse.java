package com.swp.backend.dto.manager.response;

import java.math.BigDecimal;
import java.util.UUID;

public record StaffResponse(
    UUID id,
    String name,
    String phone,
    String password,
    String role,
    String teamName,
    Integer teamSize,
    BigDecimal latitude,
    BigDecimal longitude,
    String state
) {
}

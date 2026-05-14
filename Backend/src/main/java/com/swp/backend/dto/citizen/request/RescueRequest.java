package com.swp.backend.dto.citizen.request;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public record RescueRequest (
        String type,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String description,
        String name,
        String phone,
        String additionalLink,
        List<MultipartFile> images
) {
}

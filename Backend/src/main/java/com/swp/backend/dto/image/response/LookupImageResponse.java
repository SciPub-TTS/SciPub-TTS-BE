package com.swp.backend.dto.image.response;

import java.util.UUID;

public record LookupImageResponse(
        UUID id,
        String imageUrl
) {}
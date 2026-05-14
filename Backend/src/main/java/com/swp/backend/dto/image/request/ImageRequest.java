package com.swp.backend.dto.image.request;

import java.util.UUID;

public record ImageRequest (
        UUID id,
        String imageUrl
) {
}

package com.swp.backend.dto.coordinator.response;

import java.util.List;

public record TakePageResponse (
        int totalPage,
        List<TakeListResponse> list
) {
}

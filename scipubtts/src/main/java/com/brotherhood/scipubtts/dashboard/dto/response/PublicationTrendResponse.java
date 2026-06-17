package com.brotherhood.scipubtts.dashboard.dto.response;

import java.util.List;

public record PublicationTrendResponse(
        List<PublicationTrendItem> publicationTrends
) {
    public record PublicationTrendItem(
            Long publications,
            Integer year
    ) {
    }
}

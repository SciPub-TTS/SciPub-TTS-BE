package com.brotherhood.scipubtts.dashboard.dto.response;

import com.brotherhood.scipubtts.dashboard.entity.PublicationTrend;

import java.util.List;

public record PublicationTrendResponse (
        List<PublicationTrend> publicationTrends
) {
}
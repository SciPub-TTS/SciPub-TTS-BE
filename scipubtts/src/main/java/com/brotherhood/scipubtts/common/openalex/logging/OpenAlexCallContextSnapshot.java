package com.brotherhood.scipubtts.common.openalex.logging;

import java.util.UUID;

public record OpenAlexCallContextSnapshot(
        UUID jobId,
        String jobType,
        boolean forceSystem
) {
}

package com.brotherhood.scipubtts.common.openalex.logging;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAlexCallContextTest {

    @Test
    void restoresPreviousContextAfterNestedRun() {
        UUID outerJobId = UUID.randomUUID();
        UUID innerJobId = UUID.randomUUID();
        AtomicReference<OpenAlexCallContextSnapshot> innerContext = new AtomicReference<>();
        AtomicReference<OpenAlexCallContextSnapshot> restoredContext = new AtomicReference<>();

        OpenAlexCallContext.runAsSystemJob(outerJobId, "OUTER", () -> {
            OpenAlexCallContext.runAsSystemJob(innerJobId, "INNER", () ->
                    innerContext.set(OpenAlexCallContext.current())
            );
            restoredContext.set(OpenAlexCallContext.current());
        });

        assertThat(innerContext.get().jobId()).isEqualTo(innerJobId);
        assertThat(innerContext.get().jobType()).isEqualTo("INNER");
        assertThat(restoredContext.get().jobId()).isEqualTo(outerJobId);
        assertThat(restoredContext.get().jobType()).isEqualTo("OUTER");
        assertThat(OpenAlexCallContext.current()).isNull();
    }
}

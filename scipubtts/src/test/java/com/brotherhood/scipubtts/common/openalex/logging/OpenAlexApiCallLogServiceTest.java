package com.brotherhood.scipubtts.common.openalex.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class OpenAlexApiCallLogServiceTest {

    @Test
    void saveBestEffortDoesNotExposePersistenceFailure() {
        OpenAlexApiCallLogWriter writer = mock(OpenAlexApiCallLogWriter.class);
        OpenAlexApiCallLogService service = new OpenAlexApiCallLogService(writer);
        OpenAlexApiCallLog log = new OpenAlexApiCallLog();
        doThrow(new IllegalStateException("database unavailable")).when(writer).save(log);

        assertThatNoException().isThrownBy(() -> service.saveBestEffort(log));
    }
}

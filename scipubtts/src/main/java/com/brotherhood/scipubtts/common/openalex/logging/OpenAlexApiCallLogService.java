package com.brotherhood.scipubtts.common.openalex.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAlexApiCallLogService {

    private final OpenAlexApiCallLogWriter writer;

    public void saveBestEffort(OpenAlexApiCallLog apiCallLog) {
        try {
            writer.save(apiCallLog);
        } catch (Exception exception) {
            log.warn("Failed to persist OpenAlex API call log", exception);
        }
    }
}

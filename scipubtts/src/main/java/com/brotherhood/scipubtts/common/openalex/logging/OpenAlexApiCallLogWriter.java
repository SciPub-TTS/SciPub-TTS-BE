package com.brotherhood.scipubtts.common.openalex.logging;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OpenAlexApiCallLogWriter {

    private final OpenAlexApiCallLogRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(OpenAlexApiCallLog log) {
        repository.save(log);
    }
}

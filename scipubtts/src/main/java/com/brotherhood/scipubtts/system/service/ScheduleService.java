package com.brotherhood.scipubtts.system.service;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.system.dto.CronConfigResponse;
import com.brotherhood.scipubtts.system.dto.UpdateCronConfigRequest;
import com.brotherhood.scipubtts.system.entity.SystemValue;
import com.brotherhood.scipubtts.system.repository.SystemValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final SystemValueRepository systemValueRepository;
    private static final String CONFIG_FEED_DAILY = "cron.schedule.research_feed.sync_daily";
    private static final String CONFIG_DASHBOARD_WEEKLY = "cron.schedule.statistic_weekly.run_job";

    public List<CronConfigResponse> getAllSchedules() {
        return systemValueRepository.findAll().stream()
                .map(this::toCronConfigResponse)
                .toList();
    }

    public CronConfigResponse getDailySyncSchedule() {
        var systemValue = systemValueRepository.findByConfigKey(CONFIG_FEED_DAILY)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFIGURATION_KEY_NOT_FOUND));

        return toCronConfigResponse(systemValue);
    }


    public CronConfigResponse getWeeklyStatisticSchedule() {
        var systemValue = systemValueRepository.findByConfigKey(CONFIG_DASHBOARD_WEEKLY)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFIGURATION_KEY_NOT_FOUND));

        return toCronConfigResponse(systemValue);
    }

    @Transactional
    public CronConfigResponse updateCronConfig(String configKey, UpdateCronConfigRequest request) {
        String cron = buildCronExpression(request);
        if (!CronExpression.isValidExpression(cron)) {
            throw new BusinessException(ErrorCode.INVALID_CRON_EXPRESSION, cron);
        }

        SystemValue systemValue = systemValueRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.SYSTEM_CONFIGURATION_NOT_FOUND));

        systemValue.setConfigValue(cron);
        systemValue.setUpdatedAt(OffsetDateTime.now());

        return toCronConfigResponse(systemValue);
    }

    private CronConfigResponse toCronConfigResponse(SystemValue systemValue) {
        String cron = systemValue.getConfigValue();
        String[] parts = parseCron(cron);
        return CronConfigResponse.builder()
                .configKey(systemValue.getConfigKey())
                .fullCronExpression(cron)
                .second(parts[0])
                .minute(parts[1])
                .hour(parts[2])
                .dayOfMonth(parts[3])
                .month(parts[4])
                .dayOfWeek(parts[5])
                .description(systemValue.getDescription())
                .createdAt(systemValue.getCreatedAt())
                .updateAt(systemValue.getUpdatedAt())
                .build();
    }

    private String[] parseCron(String cron) {
        String[] parts = cron.trim().split("\\s+");
        if (parts.length < 6) {
            String[] safeParts = {"0", "0", "0", "*", "*", "*"};
            System.arraycopy(parts, 0, safeParts, 0, parts.length);
            return safeParts;
        }
        return parts;
    }

    private String buildCronExpression(UpdateCronConfigRequest request) {
        return String.join(" ",
                request.second().trim(),
                request.minute().trim(),
                request.hour().trim(),
                normalizeOptionalCronPart(request.dayOfMonth()),
                normalizeOptionalCronPart(request.month()),
                normalizeOptionalCronPart(request.dayOfWeek())
        );
    }

    private String normalizeOptionalCronPart(String value) {
        if (value == null || value.isBlank()) {
            return "*";
        }

        return value.trim();
    }
}

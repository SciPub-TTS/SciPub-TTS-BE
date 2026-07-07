package com.brotherhood.scipubtts.system.service;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.system.dto.CronConfigResponse;
import com.brotherhood.scipubtts.system.repository.SystemValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final SystemValueRepository systemValueRepository;
    private static final String CONFIG_FEED_DAILY = "cron.schedule.research_feed.sync_daily";
    private static final String CONFIG_DASHBOARD_WEEKLY = "cron.schedule.statistic_weekly.run_job";

    public CronConfigResponse getDailySyncSchedule() {
        var systemValue = systemValueRepository.findByConfigKey(CONFIG_FEED_DAILY)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFIGURATION_KEY_NOT_FOUND));

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
                .build();
    }


    public CronConfigResponse getWeeklyStatisticSchedule() {
        var systemValue = systemValueRepository.findByConfigKey(CONFIG_DASHBOARD_WEEKLY)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFIGURATION_KEY_NOT_FOUND));

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
}
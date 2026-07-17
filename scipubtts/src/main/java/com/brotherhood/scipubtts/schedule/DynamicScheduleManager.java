package com.brotherhood.scipubtts.schedule;

import com.brotherhood.scipubtts.system.entity.SystemValue;
import com.brotherhood.scipubtts.system.repository.SystemValueRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicScheduleManager {

    private static final String DAILY_KEY = "cron.schedule.research_feed.sync_daily";
    private static final String WEEKLY_KEY = "cron.schedule.statistic_weekly.run_job";
    private static final String MONTHLY_TAXONOMY_KEY = "cron.schedule.openalex_field_taxonomy.sync_monthly";

    private static final String DAILY_FALLBACK_CRON = "0 0 2 * * *";
    private static final String WEEKLY_FALLBACK_CRON = "0 30 0 * * MON";
    private static final String MONTHLY_TAXONOMY_FALLBACK_CRON = "0 0 3 1 * *";

    private final SystemValueRepository systemValueRepository;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final ResearchFeedScheduler researchFeedScheduler;
    private final StatisticWeeklyScheduler statisticWeeklyScheduler;
    private final OpenAlexFieldTaxonomyScheduler openAlexFieldTaxonomyScheduler;

    private final Map<String, AtomicBoolean> runningJobs = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void initAllTasks() {
        log.info("Initializing dynamic scheduled tasks");
        scheduleJob(DAILY_KEY, DAILY_FALLBACK_CRON, researchFeedScheduler::syncResearchFeedDaily);
        scheduleJob(WEEKLY_KEY, WEEKLY_FALLBACK_CRON, statisticWeeklyScheduler::runWeeklyStatisticJob);
        scheduleJob(MONTHLY_TAXONOMY_KEY, MONTHLY_TAXONOMY_FALLBACK_CRON, openAlexFieldTaxonomyScheduler::syncFieldTaxonomyMonthly);
    }

    public void reloadJob(String configKey, String cron) {
        Runnable job = resolveJob(configKey);
        if (job == null) {
            log.warn("No scheduled job is mapped with config key {}", configKey);
            return;
        }

        schedule(configKey, cron, job);
    }

    private void scheduleJob(String configKey, String fallbackCron, Runnable job) {
        schedule(configKey, resolveCron(configKey, fallbackCron), job);
    }

    private void schedule(String configKey, String cron, Runnable job) {
        ScheduledFuture<?> existingTask = scheduledTasks.get(configKey);
        if (existingTask != null) {
            existingTask.cancel(false);
            log.info("Cancelled existing schedule for job {}", configKey);
        }

        ScheduledFuture<?> newTask = taskScheduler.schedule(safeJob(configKey, job), new CronTrigger(cron));
        if (newTask == null) {
            log.warn("Cannot schedule job {} with cron {}", configKey, cron);
            return;
        }

        scheduledTasks.put(configKey, newTask);
        log.info("Scheduled job {} with cron [{}]", configKey, cron);
    }

    private Runnable resolveJob(String configKey) {
        return switch (configKey) {
            case DAILY_KEY -> researchFeedScheduler::syncResearchFeedDaily;
            case WEEKLY_KEY -> statisticWeeklyScheduler::runWeeklyStatisticJob;
            case MONTHLY_TAXONOMY_KEY -> openAlexFieldTaxonomyScheduler::syncFieldTaxonomyMonthly;
            default -> null;
        };
    }

    private Runnable safeJob(String jobKey, Runnable job) {
        return () -> {
            AtomicBoolean running = runningJobs.computeIfAbsent(jobKey, key -> new AtomicBoolean(false));
            if (!running.compareAndSet(false, true)) {
                log.warn("Skipping scheduled job {} because a previous run is still in progress", jobKey);
                return;
            }

            log.info("Starting scheduled job {}", jobKey);
            try {
                job.run();
                log.info("Finished scheduled job {}", jobKey);
            } catch (Exception exception) {
                log.error("Scheduled job {} failed", jobKey, exception);
            } finally {
                running.set(false);
            }
        };
    }

    private String resolveCron(String configKey, String fallbackCron) {
        try {
            String cron = systemValueRepository.findByConfigKey(configKey)
                    .map(SystemValue::getConfigValue)
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .orElse(fallbackCron);

            if (CronExpression.isValidExpression(cron)) {
                return cron;
            }

            log.warn("Invalid cron expression '{}' for key {}. Falling back to '{}'", cron, configKey, fallbackCron);
        } catch (Exception exception) {
            log.warn("Cannot resolve cron for key {}. Falling back to '{}'", configKey, fallbackCron, exception);
        }

        return fallbackCron;
    }
}

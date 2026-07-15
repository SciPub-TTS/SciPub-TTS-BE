package com.brotherhood.scipubtts.config;


import com.brotherhood.scipubtts.schedule.ResearchFeedScheduler;
import com.brotherhood.scipubtts.schedule.OpenAlexFieldTaxonomyScheduler;
import com.brotherhood.scipubtts.schedule.StatisticWeeklyScheduler;
import com.brotherhood.scipubtts.system.entity.SystemValue;
import com.brotherhood.scipubtts.system.repository.SystemValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AppScheduleConfig implements SchedulingConfigurer {

    private final SystemValueRepository systemValueRepository;

    // Inject trực tiếp 2 lớp scheduler của bạn vào đây
    private final ResearchFeedScheduler researchFeedScheduler;
    private final StatisticWeeklyScheduler statisticWeeklyScheduler;
    private final OpenAlexFieldTaxonomyScheduler openAlexFieldTaxonomyScheduler;

    private final Map<String, AtomicBoolean> runningJobs = new ConcurrentHashMap<>();

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("owlreka-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        return scheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler());

        // 1. Cấu hình cho Research Feed Daily
        String dailyKey = "cron.schedule.research_feed.sync_daily";
        String dailyFallbackCron = "0 0 2 * * *";

        taskRegistrar.addTriggerTask(
                safeJob(dailyKey, researchFeedScheduler::syncResearchFeedDaily),
                triggerContext -> new CronTrigger(resolveCron(dailyKey, dailyFallbackCron)).nextExecution(triggerContext)
        );

        String weeklyKey = "cron.schedule.statistic_weekly.run_job";
        String weeklyFallbackCron = "0 30 0 * * MON";

        taskRegistrar.addTriggerTask(
                safeJob(weeklyKey, statisticWeeklyScheduler::runWeeklyStatisticJob),
                triggerContext -> new CronTrigger(resolveCron(weeklyKey, weeklyFallbackCron)).nextExecution(triggerContext)
        );

        String monthlyTaxonomyKey = "cron.schedule.openalex_field_taxonomy.sync_monthly";
        String monthlyTaxonomyFallbackCron = "0 0 3 1 * *";

        taskRegistrar.addTriggerTask(
                safeJob(monthlyTaxonomyKey, openAlexFieldTaxonomyScheduler::syncFieldTaxonomyMonthly),
                triggerContext -> new CronTrigger(resolveCron(monthlyTaxonomyKey, monthlyTaxonomyFallbackCron)).nextExecution(triggerContext)
        );
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

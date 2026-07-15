package com.brotherhood.scipubtts.config;


import com.brotherhood.scipubtts.schedule.ResearchFeedScheduler;
import com.brotherhood.scipubtts.schedule.OpenAlexFieldTaxonomyScheduler;
import com.brotherhood.scipubtts.schedule.StatisticWeeklyScheduler;
import com.brotherhood.scipubtts.system.entity.SystemValue;
import com.brotherhood.scipubtts.system.repository.SystemValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AppScheduleConfig implements SchedulingConfigurer {

    private final SystemValueRepository systemValueRepository;

    // Inject trực tiếp 2 lớp scheduler của bạn vào đây
    private final ResearchFeedScheduler researchFeedScheduler;
    private final StatisticWeeklyScheduler statisticWeeklyScheduler;
    private final OpenAlexFieldTaxonomyScheduler openAlexFieldTaxonomyScheduler;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        // 1. Cấu hình cho Research Feed Daily
        String dailyKey = "cron.schedule.research_feed.sync_daily";
        String dailyCron = systemValueRepository.findByConfigKey(dailyKey)
                .map(SystemValue::getConfigValue)
                .orElse("0 0 2 * * *");

        taskRegistrar.addTriggerTask(
                researchFeedScheduler::syncResearchFeedDaily,
                triggerContext -> new CronTrigger(dailyCron).nextExecution(triggerContext)
        );

        String weeklyKey = "cron.schedule.statistic_weekly.run_job";
        String weeklyCron = systemValueRepository.findByConfigKey(weeklyKey)
                .map(SystemValue::getConfigValue)
                .orElse("0 30 0 * * MON");

        taskRegistrar.addTriggerTask(
                statisticWeeklyScheduler::runWeeklyStatisticJob,
                triggerContext -> new CronTrigger(weeklyCron).nextExecution(triggerContext)
        );

        String monthlyTaxonomyKey = "cron.schedule.openalex_field_taxonomy.sync_monthly";
        String monthlyTaxonomyCron = systemValueRepository.findByConfigKey(monthlyTaxonomyKey)
                .map(SystemValue::getConfigValue)
                .orElse("0 0 3 1 * *");

        taskRegistrar.addTriggerTask(
                openAlexFieldTaxonomyScheduler::syncFieldTaxonomyMonthly,
                triggerContext -> new CronTrigger(monthlyTaxonomyCron).nextExecution(triggerContext)
        );
    }
}

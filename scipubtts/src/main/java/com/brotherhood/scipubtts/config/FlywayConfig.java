package com.brotherhood.scipubtts.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Value("${spring.flyway.locations:classpath:db/migration}")
    private String migrationLocations;

    @Value("${spring.flyway.baseline-on-migrate:true}")
    private boolean baselineOnMigrate;

    @Value("${spring.flyway.out-of-order:true}")
    private boolean outOfOrder;

    @Value("${spring.flyway.validate-migration-naming:false}")
    private boolean validateMigrationNaming;

    // 1. Thêm biến này để đọc cấu hình bỏ qua lỗi từ properties
    @Value("${spring.flyway.ignore-migration-patterns:*:missing,*:future}")
    private String[] ignoreMigrationPatterns;

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(migrationLocations)
                .baselineOnMigrate(baselineOnMigrate)
                .baselineVersion("1")
                .baselineDescription("init_schema")
                .outOfOrder(outOfOrder)
                .validateMigrationNaming(validateMigrationNaming)
                .ignoreMigrationPatterns("*:missing", "*:future")
                .cleanDisabled(true)
                .load();
    }
}
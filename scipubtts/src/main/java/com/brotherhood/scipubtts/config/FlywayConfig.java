/*
 Archived on 2026-06-19.

 This custom Flyway configuration is intentionally disabled because it causes
 the application's Hikari DataSource to initialize too early under Spring
 Boot 4, which then seals the pool before Spring finishes binding
 spring.datasource.hikari.* properties.

 The application now relies on Spring Boot's built-in Flyway auto-configuration.

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

     @Value("${spring.flyway.baseline-on-migrate:false}")
     private boolean baselineOnMigrate;

     @Value("${spring.flyway.baseline-version:0}")
     private String baselineVersion;

     @Value("${spring.flyway.baseline-description:init_schema}")
     private String baselineDescription;

     @Value("${spring.flyway.out-of-order:true}")
     private boolean outOfOrder;

     @Value("${spring.flyway.validate-migration-naming:false}")
     private boolean validateMigrationNaming;

     @Value("${spring.flyway.ignore-migration-patterns:*:missing,*:future}")
     private String[] ignoreMigrationPatterns;

     @Bean(initMethod = "migrate")
     public Flyway flyway(DataSource dataSource) {
         return Flyway.configure()
                 .dataSource(dataSource)
                 .locations(migrationLocations)
                 .baselineOnMigrate(baselineOnMigrate)
                 .baselineVersion(baselineVersion)
                 .baselineDescription(baselineDescription)
                 .outOfOrder(outOfOrder)
                 .validateMigrationNaming(validateMigrationNaming)
                 .ignoreMigrationPatterns("*:missing", "*:future")
                 .cleanDisabled(true)
                 .load();
     }
 }
*/

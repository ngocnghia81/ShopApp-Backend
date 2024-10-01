package com.tripleng.shopappserver.configs;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Value("${spring.flyway.locations}")
    private String[] flywayLocations;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUser;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Bean
    public Flyway flyway() {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource())
                .locations(flywayLocations)
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load();

        try {
            flyway.migrate();
            System.out.println("Flyway migration completed successfully");
        } catch (Exception e) {
            System.err.println("Flyway migration failed: " + e.getMessage());
        }

        return flyway;
    }

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(datasourceUrl)
                .username(datasourceUser)
                .password(datasourcePassword)
                .build();
    }

}

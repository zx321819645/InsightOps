package com.insightops.health;

import com.insightops.health.checker.DatabaseMiddlewareChecker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@AutoConfiguration
@ConditionalOnClass(DataSource.class)
public class DatabaseCheckerAutoConfiguration {

    @Bean
    @ConditionalOnBean(DataSource.class)
    public MiddlewareChecker databaseMiddlewareChecker(DataSource dataSource) {
        return new DatabaseMiddlewareChecker(dataSource);
    }
}

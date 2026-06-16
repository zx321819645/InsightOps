package com.insightops.health.checker;

import com.insightops.health.MiddlewareCheckResult;
import com.insightops.health.MiddlewareChecker;

import javax.sql.DataSource;
import java.sql.Connection;

public class DatabaseMiddlewareChecker implements MiddlewareChecker {

    private final DataSource dataSource;

    public DatabaseMiddlewareChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String label() {
        return "MySQL 数据库";
    }

    @Override
    public MiddlewareCheckResult check() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(3)) {
                String catalog = connection.getCatalog();
                return MiddlewareCheckResult.ok(catalog != null ? "database=" + catalog : null);
            }
            return MiddlewareCheckResult.fail("连接无效");
        } catch (Exception ex) {
            return MiddlewareCheckResult.fail(ex.getMessage());
        }
    }
}

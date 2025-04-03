package guru.qa.rococo.data.jpa;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RococoDataSources {

    private RococoDataSources() {}

    private static final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

    @Nonnull
    public static DataSource dataSource(String dbName) {
        return dataSources.computeIfAbsent(dbName, key -> {
            HikariConfig config = new HikariConfig();
            String jdbcUrl ="jdbc:mysql://" +dbName + "?serverTimezone=UTC&createDatabaseIfNotExist=true";

            config.setJdbcUrl(jdbcUrl);
            config.setUsername("root");        // или другой пользователь
            config.setPassword("secret");      // или другой пароль

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(10000);
            config.setIdleTimeout(10000);
            config.setMaxLifetime(30000);
            config.setAutoCommit(true);

            return new HikariDataSource(config);
        });
    }
}
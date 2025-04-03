package guru.qa.rococo.data.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManagers {
    private EntityManagers() {
    }

    private static final Map<String, EntityManagerFactory> emfs = new ConcurrentHashMap<>();

    @Nonnull
    public static EntityManager em(String jdbcUrl) {
        return new ThreadSafeEntityManager(
                emfs.computeIfAbsent(
                        jdbcUrl,
                        key -> {
                            RococoDataSources.dataSource(jdbcUrl);
                            final String persistenceUnitName = StringUtils.substringAfter(jdbcUrl, "5432/");
                            return Persistence.createEntityManagerFactory(persistenceUnitName);
                        }
                ).createEntityManager()
        );
    }

    public static void closeAllEmfs() {
        emfs.values().forEach(EntityManagerFactory::close);
    }
}

package guru.qa.rococo.data.repository;

import guru.qa.rococo.config.Config;
import guru.qa.rococo.data.entity.MuseumEntity;
import guru.qa.rococo.data.jpa.RococoDataSources;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

public class MuseumRepositorySpringJdbc implements MuseumRepository {

    private static final Config CFG = Config.getInstance();
    JdbcTemplate jdbcTemplate = new JdbcTemplate(RococoDataSources.dataSource(CFG.databaseAddress() + "/rococo-museum"));

    @Override
    public void createMuseumForTest(MuseumEntity museum) {
        if (museum.getId() == null) {
            museum.setId(UUID.randomUUID());
        }

        jdbcTemplate.update(
                "INSERT INTO museum (id, title, description, city, geo_id, photo) " +
                        "VALUES (UUID_TO_BIN(?), ?, ?, ?, UUID_TO_BIN(?), ?)",
                museum.getId().toString(),
                museum.getTitle(),
                museum.getDescription(),
                museum.getCity(),
                museum.getGeoId().toString(),
                museum.getPhoto());

    }

    @Override
    public void deleteMuseum(MuseumEntity museum) {
        jdbcTemplate.update("DELETE FROM museum WHERE id = ?", museum.getId());
    }
}
package guru.qa.rococo.data.repository;

import guru.qa.rococo.config.Config;
import guru.qa.rococo.data.entity.PaintingEntity;
import guru.qa.rococo.data.jpa.RococoDataSources;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

public class PaintingRepositorySpringJdbc implements PaintingRepository {
    private static final Config CFG = Config.getInstance();
    JdbcTemplate jdbcTemplate = new JdbcTemplate(RococoDataSources.dataSource(CFG.databaseAddress() + "/rococo-painting"));

    @Override
    public void createPainting(PaintingEntity painting) {
        if (painting.getId() == null) {
            painting.setId(UUID.randomUUID()); // Генерируем ID заранее
        }

        final String sql = """
                INSERT INTO painting (
                    id,
                    title,
                    description,
                    content,
                    museum_id,
                    artist_id
                ) VALUES (UUID_TO_BIN(?), ?, ?, ?, UUID_TO_BIN(?), UUID_TO_BIN(?))
                """;

        jdbcTemplate.update(sql,
                painting.getId().toString(), // Явно передаем ID
                painting.getTitle(),
                painting.getDescription(),
                painting.getContent(),
                painting.getMuseumId().toString(),
                painting.getArtistId().toString()
        );
    }

    @Override
    public void deletePainting(PaintingEntity painting) {
        jdbcTemplate.update("DELETE FROM painting WHERE id = ?", painting.getId());
    }

}

package guru.qa.rococo.data.repository;

import guru.qa.rococo.config.Config;
import guru.qa.rococo.data.entity.ArtistEntity;
import guru.qa.rococo.data.jpa.RococoDataSources;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

public class ArtistRepositorySpringJdbc implements ArtistRepository {
    private static final Config CFG = Config.getInstance();

    JdbcTemplate jdbcTemplate = new JdbcTemplate(RococoDataSources.dataSource(CFG.databaseAddress() + "/rococo-artist"));

    @Override
    public void createArtistForTest(ArtistEntity artist) {

        if (artist.getId() == null) {
            artist.setId(UUID.randomUUID());
        }
        jdbcTemplate.update(
                "INSERT INTO artist (id, name, biography, photo) VALUES (UUID_TO_BIN(?), ?, ?, ?)",
                artist.getId().toString(),
                artist.getName(),
                artist.getBiography(),
                artist.getPhoto()
        );
    }


    @Override
    public void deleteArtist(ArtistEntity artist) {
        jdbcTemplate.update("DELETE FROM artist WHERE id = ?", artist.getId());
    }
}

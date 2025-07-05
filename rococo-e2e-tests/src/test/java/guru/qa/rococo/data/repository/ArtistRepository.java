package guru.qa.rococo.data.repository;

import guru.qa.rococo.data.entity.ArtistEntity;

public interface ArtistRepository {
    void createArtistForTest(ArtistEntity artist);
    void deleteArtist(ArtistEntity artist);
}

package guru.qa.rococo.data.repository;

import guru.qa.rococo.data.entity.MuseumEntity;

public interface MuseumRepository {
    void createMuseumForTest(MuseumEntity museum);
    void deleteMuseum(MuseumEntity museum);
}

package guru.qa.rococo.data.repository;

import guru.qa.rococo.data.entity.PaintingEntity;

public interface PaintingRepository {
    void createPainting(PaintingEntity painting);
    void deletePainting(PaintingEntity painting);
}

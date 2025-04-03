package guru.qa.rococo.jupiter.extension;

import guru.qa.rococo.data.entity.PaintingEntity;
import guru.qa.rococo.data.repository.PaintingRepository;
import guru.qa.rococo.data.repository.PaintingRepositorySpringJdbc;
import guru.qa.rococo.jupiter.annotation.GeneratePainting;
import guru.qa.rococo.model.ArtistJson;
import guru.qa.rococo.model.MuseumJson;
import guru.qa.rococo.model.PaintingJson;
import guru.qa.rococo.utils.ImageUtils;
import guru.qa.rococo.utils.RandomDataUtils;
import org.junit.jupiter.api.extension.*;

import static io.qameta.allure.Allure.step;

public class DBCreatePaintingExtension implements BeforeEachCallback, ParameterResolver {

    public static ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(DBCreatePaintingExtension.class);
    private static final String IMAGE_PATH = "img/artist.jpg";
    static final String PAINTING_KEY = "painting";

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        GeneratePainting annotation = extensionContext.getRequiredTestMethod().getAnnotation(GeneratePainting.class);
        if (annotation != null) {
            step("Create painting (DB)", () -> {
                PaintingEntity painting = createPaintingForTest(extensionContext, annotation);
                extensionContext.getStore(NAMESPACE).put(extensionContext.getUniqueId(), PaintingJson.fromEntity(painting));
            });
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext
                .getParameter()
                .getType()
                .isAssignableFrom(PaintingJson.class);
    }

    @Override
    public PaintingJson resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext
                .getStore(NAMESPACE)
                .get(extensionContext.getUniqueId(), PaintingJson.class);
    }

    private PaintingEntity createPaintingForTest(ExtensionContext extensionContext, GeneratePainting annotation) {
        PaintingRepository paintingRepository = new PaintingRepositorySpringJdbc();

        String title = annotation.title().isEmpty() ? RandomDataUtils.randomSentence(2) : annotation.title();
        String description = annotation.description().isEmpty() ? RandomDataUtils.randomSentence(10) : annotation.description();
        String photoBase64 = ImageUtils.convertImageToBase64(IMAGE_PATH);

        // Добавляем проверку на null
        MuseumJson museumJson = extensionContext
                .getStore(DBCreateMuseumExtension.NAMESPACE)
                .get(extensionContext.getUniqueId() + PAINTING_KEY, MuseumJson.class);

        if (museumJson == null) {
            throw new IllegalStateException("Museum not found in context. Did you forget to add @GenerateMuseum annotation?");
        }

        ArtistJson artistJson = extensionContext
                .getStore(CreateArtistExtension.NAMESPACE)
                .get(extensionContext.getUniqueId() + PAINTING_KEY, ArtistJson.class);

        if (artistJson == null) {
            throw new IllegalStateException("Artist not found in context. Did you forget to add @GenerateArtist annotation?");
        }

        PaintingEntity painting = new PaintingEntity();
        painting.setTitle(title);
        painting.setDescription(description);
        painting.setContent(photoBase64.getBytes());
        painting.setMuseumId(museumJson.id());
        painting.setArtistId(artistJson.id());

        paintingRepository.createPainting(painting);
        return painting;
    }
}

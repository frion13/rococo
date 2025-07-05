package guru.qa.rococo.jupiter.extension;

import guru.qa.rococo.data.entity.MuseumEntity;
import guru.qa.rococo.data.repository.GeoRepository;
import guru.qa.rococo.data.repository.GeoRepositorySpringJdbc;
import guru.qa.rococo.data.repository.MuseumRepository;
import guru.qa.rococo.data.repository.MuseumRepositorySpringJdbc;
import guru.qa.rococo.jupiter.annotation.GenerateMuseum;
import guru.qa.rococo.jupiter.annotation.GeneratePainting;
import guru.qa.rococo.model.CountryJson;
import guru.qa.rococo.model.GeoJson;
import guru.qa.rococo.model.MuseumJson;
import guru.qa.rococo.utils.ImageUtils;
import guru.qa.rococo.utils.RandomDataUtils;
import io.qameta.allure.Step;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.UUID;

import static guru.qa.rococo.jupiter.extension.DBCreatePaintingExtension.PAINTING_KEY;

public class DBCreateMuseumExtension implements BeforeEachCallback, ParameterResolver {

    // Пространство имен для хранения данных в контексте теста
    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(DBCreateMuseumExtension.class);

    // Путь к тестовому изображению для музея
    private static final String IMAGE_PATH = "img/artist.jpg";
    String photoBase64 = ImageUtils.convertImageToBase64(IMAGE_PATH);

    @Override
    public void beforeEach(ExtensionContext context) {
        // Обработка аннотации @GenerateMuseum
        processGenerateMuseumAnnotation(context);

        // Обработка аннотации @GeneratePainting (если нужна)
        processGeneratePaintingAnnotation(context);
    }

    private void processGenerateMuseumAnnotation(ExtensionContext context) {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), GenerateMuseum.class)
                .ifPresent(annotation -> {
                    MuseumEntity museum = createMuseumEntity(annotation);
                    MuseumJson museumJson = convertToJson(museum, annotation.enrichJsonCountryName());
                    saveToContext(context, museumJson, "");
                });
    }

    private void processGeneratePaintingAnnotation(ExtensionContext context) {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), GeneratePainting.class)
                .ifPresent(annotation -> {
                    MuseumEntity museum = createMuseumEntity(annotation.museum());
                    MuseumJson museumJson = convertToJson(museum, annotation.museum().enrichJsonCountryName());
                    saveToContext(context, museumJson, PAINTING_KEY);
                });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().isAssignableFrom(MuseumJson.class);
    }

    @Override
    public MuseumJson resolveParameter(ParameterContext parameterContext,
                                       ExtensionContext extensionContext) {
        return extensionContext.getStore(NAMESPACE)
                .get(extensionContext.getUniqueId() + getKeySuffix(parameterContext), MuseumJson.class);
    }

    private MuseumEntity createMuseumEntity(GenerateMuseum annotation) {
        MuseumRepository repository = new MuseumRepositorySpringJdbc();
        GeoRepository geoRepository = new GeoRepositorySpringJdbc();

        MuseumEntity museum = new MuseumEntity();
        museum.setTitle(getTitle(annotation));
        museum.setDescription(getDescription(annotation));
        museum.setCity(getCity(annotation));
        museum.setGeoId(getCountryId(annotation, geoRepository));
        museum.setPhoto(photoBase64.getBytes());

        repository.createMuseumForTest(museum);
        return museum;
    }

    private String getTitle(GenerateMuseum annotation) {
        return annotation.title().isEmpty()
                ? RandomDataUtils.randomName()
                : annotation.title();
    }

    private String getDescription(GenerateMuseum annotation) {
        return annotation.description().isEmpty()
                ? RandomDataUtils.randomSentence(15)
                : annotation.description();
    }

    private String getCity(GenerateMuseum annotation) {
        return annotation.city().isEmpty()
                ? RandomDataUtils.randomCity()
                : annotation.city();
    }

    private UUID getCountryId(GenerateMuseum annotation, GeoRepository geoRepository) {
        return annotation.country().isEmpty()
                ? RandomDataUtils.randomCountry().id()
                : geoRepository.getCountryByName(annotation.country()).getId();
    }

    private MuseumJson convertToJson(MuseumEntity entity, boolean enrichCountry) {
        MuseumJson museumJson = MuseumJson.fromEntity(entity);
        return enrichCountry ? enrichWithCountryName(museumJson) : museumJson;
    }

    @Step("Добавить название страны к данным музея")
    private MuseumJson enrichWithCountryName(MuseumJson museum) {
        GeoRepository geoRepository = new GeoRepositorySpringJdbc();
        UUID countryId = museum.geo().getCountry().id();
        String countryName = geoRepository.getCountryById(countryId).getName();

        return new MuseumJson(
                museum.id(),
                museum.title(),
                museum.description(),
                museum.photo(),
                new GeoJson(
                        museum.geo().getCity(),
                        new CountryJson(countryId, countryName)
                )
        );
    }

    private void saveToContext(ExtensionContext context, MuseumJson museumJson, String keySuffix) {
        context.getStore(NAMESPACE).put(context.getUniqueId() + keySuffix, museumJson);
    }

    private String getKeySuffix(ParameterContext parameterContext) {
        return parameterContext.findAnnotation(GeneratePainting.class).isPresent()
                ? PAINTING_KEY
                : "";
    }

}
package guru.qa.rococo.test.grpc;

import guru.qa.grpc.rococo.grpc.*;
import guru.qa.rococo.config.Config;
import guru.qa.rococo.jupiter.annotation.GrpcTest;
import guru.qa.rococo.model.CountryJson;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qameta.allure.grpc.AllureGrpc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.google.protobuf.ByteString.copyFromUtf8;
import static guru.qa.rococo.utils.RandomDataUtils.randomCountry;
import static org.junit.jupiter.api.Assertions.*;

@GrpcTest
public class GeoGrpcTest {

    private static final Channel geoChannel;
    private static final Config CFG = Config.getInstance();

    static {
        geoChannel = ManagedChannelBuilder
                .forAddress(CFG.geoGrpcAddress(), CFG.geoGrpcPort())
                .intercept(new AllureGrpc())
                .usePlaintext()
                .build();
    }

    private final RococoGeoServiceGrpc.RococoGeoServiceBlockingStub geoStub = RococoGeoServiceGrpc.newBlockingStub(geoChannel);

    @Test
    @DisplayName("GRPC: Получение информации о стране из rococo-geo по id")
    void shouldReturnCountryDataByIdFromDB() {
        CountryJson country = randomCountry();
        CountryId request = CountryId.newBuilder()
                .setId(copyFromUtf8(country.id().toString()))
                .build();

        final CountryResponse countryResponse = geoStub.getCountry(request);

       assertEquals(country.name(), countryResponse.getName());
     assertEquals(country.id(), UUID.fromString(countryResponse.getId().toStringUtf8()));
    }

    @Test
    @DisplayName("GRPC: Получение информации о стране из rococo-geo по имени страны")
    void shouldReturnCountryDataByNameFromDB() {
        CountryJson country = randomCountry();
        CountryName request = CountryName.newBuilder()
                .setName(country.name())
                .build();

        final CountryResponse countryResponse = geoStub.getCountryByName(request);

        assertEquals(country.name(), countryResponse.getName());
        assertEquals(country.id(), UUID.fromString(countryResponse.getId().toStringUtf8()));
    }


    @Test
    @DisplayName("GRPC: Получение списка стран с пагинацией")
    void getAllCountriesWithPagination_ShouldReturnPageOfCountries() {
        int page = 0;
        int size = 5;
        AllCountryRequest request = AllCountryRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .build();

        AllCountryResponse response = geoStub.getAllCountry(request);
        assertAll(
                () -> assertFalse(response.getCountryList().isEmpty(),
                        "Список стран не должен быть пустым"),
                () -> assertTrue(response.getCountryCount() <= size,
                        "Количество стран не должно превышать запрошенный размер страницы"),
                () -> assertTrue(response.getTotalCount() > 0,
                        "Общее количество стран должно быть больше 0")
        );
    }

    @Test
    @DisplayName("GRPC: Получение стран по списку ID")
    void getCountriesByIds_ShouldReturnCorrectCountries() {
        List<CountryJson> testCountries = IntStream.range(0, 3)
                .mapToObj(i -> randomCountry())
                .toList();

        CountryIdsRequest request = CountryIdsRequest.newBuilder()
                .addAllId(testCountries.stream()
                        .map(c -> copyFromUtf8(c.id().toString()))
                        .toList())
                .build();

        AllCountryByIdsResponse response = geoStub.getCountriesByIds(request);

        assertAll(
                () -> assertEquals(testCountries.size(), response.getCountryCount(),
                        "Должны вернуться все запрошенные страны"),
                () -> assertTrue(testCountries.stream()
                                .allMatch(tc -> response.getCountryList().stream()
                                        .anyMatch(rc ->
                                                tc.id().toString().equals(rc.getId().toStringUtf8()) &&
                                                        tc.name().equals(rc.getName()))),
                        "Все запрошенные страны должны быть в ответе")
        );
    }

    @Test
    @DisplayName("GRPC: Получение несуществующей страны по ID - ошибка NOT_FOUND")
    void getNonExistentCountryById_ShouldReturnNotFoundError() {
        CountryId request = CountryId.newBuilder()
                .setId(copyFromUtf8(UUID.randomUUID().toString()))
                .build();

        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class,
                () -> geoStub.getCountry(request));

        assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
    }

    @Test
    @DisplayName("GRPC: Получение несуществующей страны по имени - ошибка NOT_FOUND")
    void getNonExistentCountryByName_ShouldReturnNotFoundError() {
        CountryName request = CountryName.newBuilder()
                .setName("Несуществующая страна " + System.currentTimeMillis())
                .build();

        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class,
                () -> geoStub.getCountryByName(request));

        assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
    }

    @Test
    @DisplayName("GRPC: Получение списка стран по пустому списку ID - должен вернуть пустой ответ")
    void getCountriesByEmptyIdsList_ShouldReturnEmptyResponse() {
        CountryIdsRequest request = CountryIdsRequest.newBuilder().build();
        AllCountryByIdsResponse response = geoStub.getCountriesByIds(request);

        assertTrue(response.getCountryList().isEmpty());
    }

    @Test
    @DisplayName("GRPC: Получение страницы стран с несуществующим номером страницы - должен вернуть пустой список")
    void getNonExistentPageOfCountries_ShouldReturnEmptyList() {
        int nonExistentPage = 9999;
        AllCountryRequest request = AllCountryRequest.newBuilder()
                .setPage(nonExistentPage)
                .setSize(10)
                .build();

        AllCountryResponse response = geoStub.getAllCountry(request);

        assertTrue(response.getCountryList().isEmpty());
    }
}

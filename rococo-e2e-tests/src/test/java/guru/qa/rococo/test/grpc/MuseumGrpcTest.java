package guru.qa.rococo.test.grpc;

import com.google.protobuf.ByteString;
import guru.qa.grpc.rococo.grpc.*;
import guru.qa.rococo.config.Config;
import guru.qa.rococo.jupiter.annotation.GenerateMuseum;
import guru.qa.rococo.jupiter.annotation.GrpcTest;
import guru.qa.rococo.model.MuseumJson;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.qameta.allure.grpc.AllureGrpc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.google.protobuf.ByteString.copyFromUtf8;
import static guru.qa.rococo.model.MuseumJson.fromGrpcMessage;
import static org.junit.jupiter.api.Assertions.*;

@GrpcTest
public class MuseumGrpcTest {

    private static final Channel museumChannel;
    private static final Config CFG = Config.getInstance();

    static {
        museumChannel = ManagedChannelBuilder
                .forAddress(CFG.museumGrpcAddress(), CFG.museumGrpcPort())
                .intercept(new AllureGrpc())
                .usePlaintext()
                .build();
    }

    private final RococoMuseumServiceGrpc.RococoMuseumServiceBlockingStub museumStub = RococoMuseumServiceGrpc.newBlockingStub(museumChannel);

    @Test
    @DisplayName("GRPC: Получение информации о музее из rococo-museum")
    @GenerateMuseum
    void shouldReturnMuseumDataFromDB(MuseumJson createdMuseum) {
        String createdMuseumId = createdMuseum.id().toString();
        MuseumRequest request = MuseumRequest.newBuilder()
                .setId(copyFromUtf8(createdMuseumId))
                .build();

        final MuseumResponse museumResponse = museumStub.getMuseum(request);

        assertEquals(createdMuseum, fromGrpcMessage(museumResponse));
    }


    @Test
    @DisplayName("GRPC: Добавление нового музея")
    void shouldAddNewMuseum() {
        AddMuseumRequest request = AddMuseumRequest.newBuilder()
                .setTitle("Новый музей")
                .setDescription("Описание нового музея")
                .setGeo(Geo.newBuilder()
                        .setCity("Москва")
                        .setCountry(CountryId.newBuilder()
                                .setId(copyFromUtf8(UUID.randomUUID().toString()))
                                .build())
                        .build())
                .setPhoto(ByteString.copyFrom(new byte[]{1, 2, 3}))
                .build();

        MuseumResponse response = museumStub.addMuseum(request);

        assertNotNull(response.getId());
        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(request.getDescription(), response.getDescription());
        assertEquals(request.getGeo(), response.getGeo());
        assertEquals(request.getPhoto(), response.getPhoto());
    }

    @Test
    @DisplayName("GRPC: Обновление информации о музее")
    @GenerateMuseum
    void shouldUpdateMuseum(MuseumJson createdMuseum) {
        UpdateMuseumRequest request = UpdateMuseumRequest.newBuilder()
                .setId(copyFromUtf8(createdMuseum.id().toString()))
                .setMuseumData(AddMuseumRequest.newBuilder()
                        .setTitle("Обновленное название")
                        .setDescription("Обновленное описание")
                        .setGeo(Geo.newBuilder()
                                .setCity("Санкт-Петербург")
                                .setCountry(CountryId.newBuilder()
                                        .setId(copyFromUtf8(UUID.randomUUID().toString()))
                                        .build())
                                .build())
                        .setPhoto(ByteString.copyFrom(new byte[]{4, 5, 6}))
                        .build())
                .build();

        MuseumResponse response = museumStub.updateMuseum(request);

        assertEquals(request.getId(), response.getId());
        assertEquals(request.getMuseumData().getTitle(), response.getTitle());
        assertEquals(request.getMuseumData().getDescription(), response.getDescription());
        assertEquals(request.getMuseumData().getGeo(), response.getGeo());
        assertEquals(request.getMuseumData().getPhoto(), response.getPhoto());
    }

    @Test
    @DisplayName("GRPC: Получение списка музеев по ids")
    @GenerateMuseum
    void shouldGetMuseumsByIds(MuseumJson createdMuseum) {
        MuseumIdsRequest request = MuseumIdsRequest.newBuilder()
                .addId(copyFromUtf8(createdMuseum.id().toString()))
                .build();

        AllMuseumByIdsResponse response = museumStub.getMuseumByIds(request);

        assertEquals(1, response.getMuseumCount());
        assertEquals(createdMuseum, fromGrpcMessage(response.getMuseum(0)));
    }

    @Test
    @DisplayName("GRPC: Получение списка музеев с пагинацией")
    void shouldGetAllMuseumsWithPagination() {
        AllMuseumRequest request = AllMuseumRequest.newBuilder()
                .setPage(0)
                .setSize(10)
                .build();

        AllMuseumResponse response = museumStub.getAllMuseum(request);

        assertTrue(response.getMuseumCount() >= 0);
        if (response.getMuseumCount() > 0) {
            assertNotNull(response.getMuseum(0).getId());
        }
    }

    @Test
    @DisplayName("GRPC: Поиск музеев по названию")
    void shouldSearchMuseumsByTitle() {
        String searchTitle = "Лувр";
        AllMuseumRequest request = AllMuseumRequest.newBuilder()
                .setTitle(searchTitle)
                .setPage(0)
                .setSize(5)
                .build();

        AllMuseumResponse response = museumStub.getAllMuseum(request);

        assertTrue(response.getMuseumCount() >= 0);
        if (response.getMuseumCount() > 0) {
            assertTrue(response.getMuseum(0).getTitle().contains(searchTitle));
        }
    }
}

package guru.qa.rococo.test.grpc;

import com.google.protobuf.ByteString;
import guru.qa.grpc.rococo.grpc.*;
import guru.qa.rococo.config.Config;
import guru.qa.rococo.jupiter.annotation.GenerateArtist;
import guru.qa.rococo.jupiter.annotation.GrpcTest;
import guru.qa.rococo.model.ArtistJson;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.qameta.allure.grpc.AllureGrpc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.google.protobuf.ByteString.copyFromUtf8;
import static guru.qa.rococo.model.ArtistJson.fromGrpcMessage;
import static org.junit.jupiter.api.Assertions.*;

@GrpcTest
public class ArtistGrpcTest {

    private static final Channel artistChannel;
    private static final Config CFG = Config.getInstance();

    static {
        artistChannel = ManagedChannelBuilder
                .forAddress(CFG.artistGrpcAddress(), CFG.artistGrpcPort())
                .intercept(new AllureGrpc())
                .usePlaintext()
                .build();
    }

    private final RococoArtistServiceGrpc.RococoArtistServiceBlockingStub artistStub = RococoArtistServiceGrpc.newBlockingStub(artistChannel);

    @Test
    @GenerateArtist

    @DisplayName("GRPC: Получение информации о художнике из rococo-artist")
    void shouldReturnArtistDataFromDB(ArtistJson createdArtist) {
        String createdArtistId = createdArtist.id().toString();
        ArtistRequest request = ArtistRequest.newBuilder()
                .setId(copyFromUtf8(createdArtistId))
                .build();

        final ArtistResponse artistResponse = artistStub.getArtist(request);

        assertEquals(createdArtist, fromGrpcMessage(artistResponse));
    }

    @Test
    @GenerateArtist
    @DisplayName("GRPC: Получение художника по ID - должен вернуть корректные данные художника")
    void getArtistShouldReturnArtistById(ArtistJson artist) {
        ArtistRequest request = ArtistRequest.newBuilder()
                .setId(copyFromUtf8(artist.id().toString()))
                .build();

        ArtistResponse response = artistStub.getArtist(request);
        ArtistJson responseArtist = fromGrpcMessage(response);

        assertEquals(artist.id(), responseArtist.id());
        assertEquals(artist.name(), responseArtist.name());
        assertEquals(artist.biography(), responseArtist.biography());
    }

    @Test
    @GenerateArtist
    @DisplayName("GRPC: Обновление данных художника - должен успешно изменить имя, биографию и сохранить фото")
    void updateArtistShouldModifyExistingArtist(ArtistJson originalArtist) {
        ArtistJson updatedArtist = new ArtistJson(
                originalArtist.id(),
                "Updated Name " + UUID.randomUUID(),
                "Updated Biography",
                originalArtist.photo()
        );

        UpdateArtistRequest request = UpdateArtistRequest.newBuilder()
                .setId(copyFromUtf8(updatedArtist.id().toString()))
                .setArtistData(ArtistJson.toGrpcMessage(updatedArtist))
                .build();

        ArtistResponse response = artistStub.updateArtist(request);
        ArtistJson result = fromGrpcMessage(response);

        assertEquals(updatedArtist.id(), result.id());
        assertEquals(updatedArtist.name(), result.name());
        assertEquals(updatedArtist.biography(), result.biography());
    }


    @Test
    @GenerateArtist
    @DisplayName("GRPC: Поиск художника по имени - должен вернуть корректные данные художника")
    void getArtistByNameShouldReturnArtist(ArtistJson artist) {
        GetArtistRequest request = GetArtistRequest.newBuilder()
                .setName(artist.name())
                .build();

        AllArtistResponse response = artistStub.getArtistByName(request);
        List<ArtistJson> artists = response.getArtistsList()
                .stream()
                .map(ArtistJson::fromGrpcMessage)
                .toList();

        assertFalse(artists.isEmpty());
        assertEquals(artist.name(), artists.get(0).name());
    }

    @Test
    @DisplayName("GRPC: Создание нового художника")
    void createArtistShouldPersistInDatabase() {
        ArtistJson newArtist = new ArtistJson(
                null, // ID будет сгенерирован сервером
                "Vincent van Gogh",
                "Dutch post-impressionist painter",
                new byte[]{0x01, 0x02, 0x03}
        );

        AddArtistRequest request = AddArtistRequest.newBuilder()
                .setName(newArtist.name())
                .setBiography(newArtist.biography())
                .setPhoto(ByteString.copyFrom(newArtist.photo())).build();

        ArtistResponse response = artistStub.addArtist(request);
        ArtistJson createdArtist = fromGrpcMessage(response);

        assertNotNull(createdArtist.id());
        assertEquals(newArtist.name(), createdArtist.name());
        assertEquals(newArtist.biography(), createdArtist.biography());
        assertArrayEquals(newArtist.photo(), createdArtist.photo());
    }

    @Test
    @DisplayName("GRPC: Получение несуществующего художника")
    void getNonExistentArtistShouldReturnNotFound() {
        String nonExistentId = UUID.randomUUID().toString();
        ArtistRequest request = ArtistRequest.newBuilder()
                .setId(copyFromUtf8(nonExistentId))
                .build();

        assertThrows(StatusRuntimeException.class, () -> {
            artistStub.getArtist(request);
        });
    }


    @Test
    @DisplayName("GRPC: Обновление несуществующего художника")
    void updateNonExistentArtistShouldFail() {
        ArtistJson nonExistentArtist = new ArtistJson(
                UUID.randomUUID(),
                "Non-existent",
                "Should not exist",
                new byte[0]
        );

        UpdateArtistRequest request = UpdateArtistRequest.newBuilder()
                .setId(copyFromUtf8(nonExistentArtist.id().toString()))
                .setArtistData(ArtistJson.toGrpcMessage(nonExistentArtist))
                .build();

        assertThrows(StatusRuntimeException.class, () -> {
            artistStub.updateArtist(request);
        });
    }

    @Test
    @GenerateArtist
    @DisplayName("GRPC: Поиск художников по частичному имени")
    void searchArtistsByPartialName(ArtistJson artist) {
        String partialName = artist.name().substring(0, 3);

        GetArtistRequest request = GetArtistRequest.newBuilder()
                .setName(partialName)
                .build();

        AllArtistResponse response = artistStub.getArtistByName(request);
        List<ArtistJson> artists = response.getArtistsList()
                .stream()
                .map(ArtistJson::fromGrpcMessage)
                .toList();

        assertFalse(artists.isEmpty());
        assertTrue(artists.stream().anyMatch(a ->
                a.name().contains(partialName)));
    }

}
package guru.qa.rococo.test.grpc;

import com.google.protobuf.ByteString;
import guru.qa.grpc.rococo.grpc.*;
import guru.qa.rococo.config.Config;
import guru.qa.rococo.jupiter.annotation.GenerateArtist;
import guru.qa.rococo.jupiter.annotation.GenerateMuseum;
import guru.qa.rococo.jupiter.annotation.GeneratePainting;
import guru.qa.rococo.jupiter.annotation.GrpcTest;
import guru.qa.rococo.model.ArtistJson;
import guru.qa.rococo.model.MuseumJson;
import guru.qa.rococo.model.PaintingJson;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qameta.allure.grpc.AllureGrpc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.google.protobuf.ByteString.copyFromUtf8;
import static guru.qa.rococo.model.PaintingJson.fromGrpcMessage;
import static org.junit.jupiter.api.Assertions.*;

@GrpcTest
public class PaintingGrpcTest {
    private static final Channel paintingChannel;
    private static final Config CFG = Config.getInstance();

    static {
        paintingChannel = ManagedChannelBuilder
                .forAddress(CFG.paintingGrpcAddress(), CFG.paintingGrpcPort())
                .intercept(new AllureGrpc())
                .usePlaintext()
                .build();
    }

    protected final RococoPaintingServiceGrpc.RococoPaintingServiceBlockingStub paintingStub = RococoPaintingServiceGrpc.newBlockingStub(paintingChannel);

    @Test
    @DisplayName("GRPC: Получение информации о картине из rococo-painting")
    @GenerateMuseum  // Создаёт музей
    @GenerateArtist
    @GeneratePainting
    void shouldReturnArtistDataFromDB(PaintingJson createdPainting) {
        String createdPaintingId = createdPainting.getId().toString();
        PaintingRequest request = PaintingRequest.newBuilder()
                .setId(copyFromUtf8(createdPaintingId))
                .build();

        final PaintingResponse paintingResponse = paintingStub.getPainting(request);

        assertEquals(createdPainting, fromGrpcMessage(paintingResponse));
    }

    @Test
    @DisplayName("GRPC: Получение ошибки при запросе несуществующей картины")
    void shouldReturnNotFoundForNonExistingPainting() {
        PaintingRequest request = PaintingRequest.newBuilder()
                .setId(copyFromUtf8(UUID.randomUUID().toString()))
                .build();

        StatusRuntimeException exception = assertThrows(
                StatusRuntimeException.class,
                () -> paintingStub.getPainting(request)
        );

        assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
    }

    @Test
    @DisplayName("GRPC: Добавление новой картины")
    @GenerateMuseum
    @GenerateArtist
    void shouldAddNewPainting(MuseumJson museum, ArtistJson artist) {
        AddPaintingRequest request = AddPaintingRequest.newBuilder()
                .setTitle("Звездная ночь")
                .setDescription("Знаменитая картина Ван Гога")
                .setContent(ByteString.copyFrom(new byte[]{1, 2, 3}))
                .setMuseumId(MuseumId.newBuilder()
                        .setId(copyFromUtf8(museum.id().toString()))
                        .build())
                .setArtistId(ArtistId.newBuilder()
                        .setId(copyFromUtf8(artist.id().toString()))
                        .build())
                .build();

        PaintingResponse response = paintingStub.addPainting(request);

        assertNotNull(response.getId());
        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(request.getDescription(), response.getDescription());
        assertEquals(request.getContent(), response.getContent());
    }

    @Test
    @DisplayName("GRPC: Обновление информации о картине")
    @GenerateMuseum
    @GenerateArtist
    @GeneratePainting
    void shouldUpdatePainting(PaintingJson createdPainting, MuseumJson museum, ArtistJson artist) {
        UpdatePaintingRequest request = UpdatePaintingRequest.newBuilder()
                .setId(copyFromUtf8(createdPainting.getId().toString()))
                .setPaintingData(AddPaintingRequest.newBuilder()
                        .setTitle("Обновленное название")
                        .setDescription("Новое описание")
                        .setContent(ByteString.copyFrom(new byte[]{4, 5, 6}))
                        .setMuseumId(MuseumId.newBuilder()
                                .setId(copyFromUtf8(museum.id().toString()))
                                .build())
                        .setArtistId(ArtistId.newBuilder()
                                .setId(copyFromUtf8(artist.id().toString()))
                                .build())
                        .build())
                .build();

        PaintingResponse response = paintingStub.updatePainting(request);

        assertEquals(request.getId(), response.getId());
        assertEquals(request.getPaintingData().getTitle(), response.getTitle());
        assertEquals(request.getPaintingData().getDescription(), response.getDescription());
    }

    @Test
    @DisplayName("GRPC: Получение списка картин с пагинацией")
    void shouldGetAllPaintingsWithPagination() {
        AllPaintingRequest request = AllPaintingRequest.newBuilder()
                .setPage(0)
                .setSize(10)
                .build();

        AllPaintingResponse response = paintingStub.getAllPainting(request);

        assertTrue(response.getTotalCount() >= 0);
        if (response.getTotalCount() > 0) {
            assertNotNull(response.getPainting(0).getId());
        }
    }

    @Test
    @DisplayName("GRPC: Поиск картин по названию")
    void shouldSearchPaintingsByTitle() {
        String searchTitle = "Мона Лиза";
        AllPaintingRequest request = AllPaintingRequest.newBuilder()
                .setTitle(searchTitle)
                .setPage(0)
                .setSize(5)
                .build();

        AllPaintingResponse response = paintingStub.getAllPainting(request);

        assertTrue(response.getTotalCount() >= 0);
        if (response.getTotalCount() > 0) {
            assertTrue(response.getPainting(0).getTitle().contains(searchTitle));
        }
    }

    @Test
    @DisplayName("GRPC: Проверка валидации при создании картины")
    void shouldValidateWhenAddingPainting() {
        AddPaintingRequest invalidRequest = AddPaintingRequest.newBuilder()
                .setTitle("")  // Пустое название
                .build();

        StatusRuntimeException exception = assertThrows(
                StatusRuntimeException.class,
                () -> paintingStub.addPainting(invalidRequest)
        );

        assertEquals(Status.UNKNOWN.getCode(), exception.getStatus().getCode());
    }


}

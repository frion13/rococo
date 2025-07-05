package guru.qa.rococo.service;

import com.google.protobuf.ByteString;
import guru.qa.grpc.rococo.grpc.*;
import guru.qa.rococo.data.PaintingEntity;
import guru.qa.rococo.data.repository.PaintingRepository;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GrpcPaintingServiceGetPaintingTest {

    private PaintingRepository paintingRepository;
    private GrpcPaintingService grpcPaintingService;

    @BeforeEach
    void setUp() {
        paintingRepository = mock(PaintingRepository.class);
        grpcPaintingService = new GrpcPaintingService(paintingRepository);
    }

    @Test
    void shouldReturnPaintingIfExists() {
        UUID paintingId = randomUUID();

        PaintingEntity entity = new PaintingEntity();
        entity.setId(paintingId);
        entity.setTitle("Mona Lisa");

        when(paintingRepository.findById(paintingId)).thenReturn(Optional.of(entity));

        PaintingResponse mockResponse = PaintingResponse.newBuilder()
                .setId(ByteString.copyFromUtf8(paintingId.toString()))
                .setTitle("Mona Lisa")
                .build();

        StreamObserver<PaintingResponse> observer = mock(StreamObserver.class);

        try (MockedStatic<PaintingEntity> mockedStatic = mockStatic(PaintingEntity.class)) {
            mockedStatic.when(() -> PaintingEntity.toGrpcMessage(entity)).thenReturn(mockResponse);

            grpcPaintingService.getPainting(
                    PaintingRequest.newBuilder().setId(ByteString.copyFromUtf8(paintingId.toString())).build(),
                    observer
            );

            verify(observer).onNext(mockResponse);
            verify(observer).onCompleted();
            verify(observer, never()).onError(any());
        }
    }

    @Test
    void shouldReturnNotFoundIfPaintingMissing() {
        UUID paintingId = randomUUID();

        when(paintingRepository.findById(paintingId)).thenReturn(Optional.empty());

        StreamObserver<PaintingResponse> observer = mock(StreamObserver.class);

        grpcPaintingService.getPainting(
                PaintingRequest.newBuilder().setId(ByteString.copyFromUtf8(paintingId.toString())).build(),
                observer
        );

        verify(observer).onError(argThat(error -> {
            assertTrue(error instanceof StatusRuntimeException);
            StatusRuntimeException ex = (StatusRuntimeException) error;
            assertEquals(Status.NOT_FOUND.getCode(), ex.getStatus().getCode());
            assertTrue(ex.getStatus().getDescription().contains(paintingId.toString()));
            return true;
        }));

        verify(observer, never()).onNext(any());
        verify(observer, never()).onCompleted();
    }


    @Test
    void shouldReturnPagedPaintings() {
        String title = "sun";
        int page = 0;
        int size = 2;

        AllPaintingRequest request = AllPaintingRequest.newBuilder()
                .setTitle(title)
                .setPage(page)
                .setSize(size)
                .build();

        UUID id1 = randomUUID();
        UUID id2 = randomUUID();

        PaintingEntity e1 = new PaintingEntity();
        e1.setId(id1);
        e1.setTitle("Sunset");

        PaintingEntity e2 = new PaintingEntity();
        e2.setId(id2);
        e2.setTitle("Sunrise");

        List<PaintingEntity> entities = List.of(e1, e2);
        Page<PaintingEntity> paintingPage = new PageImpl<>(entities);

        when(paintingRepository.findAllByTitleContainsIgnoreCase(eq(title), any(PageRequest.class)))
                .thenReturn(paintingPage);

        PaintingResponse grpcResp1 = PaintingResponse.newBuilder()
                .setId(ByteString.copyFromUtf8(id1.toString()))
                .setTitle("Sunset")
                .build();

        PaintingResponse grpcResp2 = PaintingResponse.newBuilder()
                .setId(ByteString.copyFromUtf8(id2.toString()))
                .setTitle("Sunrise")
                .build();

        StreamObserver<AllPaintingResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<AllPaintingResponse> captor = ArgumentCaptor.forClass(AllPaintingResponse.class);

        try (MockedStatic<PaintingEntity> mockStatic = mockStatic(PaintingEntity.class)) {
            mockStatic.when(() -> PaintingEntity.toGrpcMessage(e1)).thenReturn(grpcResp1);
            mockStatic.when(() -> PaintingEntity.toGrpcMessage(e2)).thenReturn(grpcResp2);

            grpcPaintingService.getAllPainting(request, observer);

            verify(observer).onNext(captor.capture());
            verify(observer).onCompleted();
            verify(observer, never()).onError(any());
        }

        AllPaintingResponse result = captor.getValue();
        assertEquals(2, result.getPaintingCount());
        assertEquals(2, result.getTotalCount());
        assertEquals("Sunset", result.getPainting(0).getTitle());
        assertEquals("Sunrise", result.getPainting(1).getTitle());
    }

    @Test
    void shouldAddPaintingSuccessfully() {
        UUID museumId = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        UUID paintingId = UUID.randomUUID();

        AddPaintingRequest request = AddPaintingRequest.newBuilder()
                .setTitle("The Starry Night")
                .setDescription("Famous painting by Van Gogh")
                .setContent(ByteString.copyFromUtf8("binary-content"))
                .setMuseumId(MuseumId.newBuilder().setId(ByteString.copyFromUtf8(museumId.toString())))
                .setArtistId(ArtistId.newBuilder().setId(ByteString.copyFromUtf8(artistId.toString())))
                .build();

        PaintingEntity entityToSave = new PaintingEntity();
        entityToSave.setTitle("The Starry Night");

        PaintingEntity savedEntity = new PaintingEntity();
        savedEntity.setId(paintingId);
        savedEntity.setTitle("The Starry Night");

        PaintingResponse grpcResponse = PaintingResponse.newBuilder()
                .setId(ByteString.copyFromUtf8(paintingId.toString()))
                .setTitle("The Starry Night")
                .build();

        StreamObserver<PaintingResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<PaintingResponse> captor = ArgumentCaptor.forClass(PaintingResponse.class);

        try (MockedStatic<PaintingEntity> mockStatic = mockStatic(PaintingEntity.class)) {
            mockStatic.when(() -> PaintingEntity.fromAddPaintingGrpcMessage(request)).thenReturn(entityToSave);
            mockStatic.when(() -> PaintingEntity.toGrpcMessage(savedEntity)).thenReturn(grpcResponse);

            when(paintingRepository.save(entityToSave)).thenReturn(savedEntity);

            grpcPaintingService.addPainting(request, observer);

            verify(observer).onNext(captor.capture());
            verify(observer).onCompleted();
            verify(observer, never()).onError(any());

            PaintingResponse result = captor.getValue();
            assertEquals("The Starry Night", result.getTitle());
            assertEquals(paintingId.toString(), result.getId().toStringUtf8());
        }
    }

    @Test
    void shouldUpdatePaintingIfExists() {
        UUID paintingId = UUID.randomUUID();

        UpdatePaintingRequest request = UpdatePaintingRequest.newBuilder()
                .setId(ByteString.copyFromUtf8(paintingId.toString()))
                .setPaintingData(AddPaintingRequest.newBuilder()
                        .setTitle("New Title")
                        .setDescription("Updated Description")
                        .build())
                .build();

        PaintingEntity existing = new PaintingEntity();
        existing.setId(paintingId);

        PaintingEntity updated = new PaintingEntity();
        updated.setId(paintingId);
        updated.setTitle("New Title");

        when(paintingRepository.findById(paintingId)).thenReturn(Optional.of(existing));
        when(paintingRepository.save(any())).thenReturn(updated);

        PaintingResponse expected = PaintingResponse.newBuilder()
                .setId(ByteString.copyFromUtf8(paintingId.toString()))
                .setTitle("New Title")
                .build();

        try (MockedStatic<PaintingEntity> mocked = mockStatic(PaintingEntity.class)) {
            mocked.when(() -> PaintingEntity.fromUpdatePaintingGrpcMessage(request)).thenReturn(updated);
            mocked.when(() -> PaintingEntity.toGrpcMessage(updated)).thenReturn(expected);

            StreamObserver<PaintingResponse> observer = mock(StreamObserver.class);
            grpcPaintingService.updatePainting(request, observer);

            verify(observer).onNext(expected);
            verify(observer).onCompleted();
            verify(observer, never()).onError(any());
        }
    }

    @Test
    void shouldReturnNotFoundIfPaintingMissingOnUpdate() {
        UUID paintingId = UUID.randomUUID();

        UpdatePaintingRequest request = UpdatePaintingRequest.newBuilder()
                .setId(ByteString.copyFromUtf8(paintingId.toString()))
                .setPaintingData(AddPaintingRequest.newBuilder().setTitle("X").build())
                .build();

        when(paintingRepository.findById(paintingId)).thenReturn(Optional.empty());

        StreamObserver<PaintingResponse> observer = mock(StreamObserver.class);
        grpcPaintingService.updatePainting(request, observer);

        verify(observer).onError(argThat(error ->
                error instanceof StatusRuntimeException &&
                        ((StatusRuntimeException) error).getStatus().getCode() == Status.NOT_FOUND.getCode() &&
                        ((StatusRuntimeException) error).getStatus().getDescription().contains("Painting not found by id")
        ));

        verify(observer, never()).onNext(any());
        verify(observer, never()).onCompleted();
    }

    @Test
    void shouldCaptureCorrectResponseWhenUpdated() {
        UUID id = UUID.randomUUID();
        UpdatePaintingRequest request = UpdatePaintingRequest.newBuilder()
                .setId(ByteString.copyFromUtf8(id.toString()))
                .setPaintingData(AddPaintingRequest.newBuilder().setTitle("Updated").build())
                .build();

        PaintingEntity entity = new PaintingEntity();
        entity.setId(id);
        entity.setTitle("Updated");

        when(paintingRepository.findById(id)).thenReturn(Optional.of(entity));
        when(paintingRepository.save(any())).thenReturn(entity);

        PaintingResponse response = PaintingResponse.newBuilder()
                .setId(ByteString.copyFromUtf8(id.toString()))
                .setTitle("Updated")
                .build();

        try (MockedStatic<PaintingEntity> mocked = mockStatic(PaintingEntity.class)) {
            mocked.when(() -> PaintingEntity.fromUpdatePaintingGrpcMessage(request)).thenReturn(entity);
            mocked.when(() -> PaintingEntity.toGrpcMessage(entity)).thenReturn(response);

            StreamObserver<PaintingResponse> observer = mock(StreamObserver.class);
            ArgumentCaptor<PaintingResponse> captor = ArgumentCaptor.forClass(PaintingResponse.class);

            grpcPaintingService.updatePainting(request, observer);

            verify(observer).onNext(captor.capture());
            PaintingResponse captured = captor.getValue();

            assertEquals("Updated", captured.getTitle());
            assertEquals(id.toString(), captured.getId().toStringUtf8());
        }
    }

    @Test
    void shouldReturnPaintingsByArtistId() {
        UUID artistId = UUID.randomUUID();
        PaintingEntity painting1 = new PaintingEntity();
        painting1.setId(UUID.randomUUID());
        painting1.setTitle("Painting A");

        PaintingEntity painting2 = new PaintingEntity();
        painting2.setId(UUID.randomUUID());
        painting2.setTitle("Painting B");

        Page<PaintingEntity> page = new PageImpl<>(List.of(painting1, painting2));

        when(paintingRepository.findAllByArtistId(eq(artistId), any(PageRequest.class)))
                .thenReturn(page);

        PaintingResponse grpc1 = PaintingResponse.newBuilder()
                .setId(ByteString.copyFromUtf8(painting1.getId().toString()))
                .setTitle("Painting A")
                .build();

        PaintingResponse grpc2 = PaintingResponse.newBuilder()
                .setId(ByteString.copyFromUtf8(painting2.getId().toString()))
                .setTitle("Painting B")
                .build();

        try (MockedStatic<PaintingEntity> mockedStatic = mockStatic(PaintingEntity.class)) {
            mockedStatic.when(() -> PaintingEntity.toGrpcMessage(painting1)).thenReturn(grpc1);
            mockedStatic.when(() -> PaintingEntity.toGrpcMessage(painting2)).thenReturn(grpc2);

            StreamObserver<AllPaintingResponse> observer = mock(StreamObserver.class);
            ArgumentCaptor<AllPaintingResponse> captor = ArgumentCaptor.forClass(AllPaintingResponse.class);

            grpcPaintingService.getAllPaintingByArtistId(
                    AllPaintingByArtistIdRequest.newBuilder()
                            .setArtistId(ByteString.copyFromUtf8(artistId.toString()))
                            .setPage(0).setSize(10).build(),
                    observer
            );

            verify(observer).onNext(captor.capture());
            verify(observer).onCompleted();

            AllPaintingResponse result = captor.getValue();
            assertEquals(2, result.getPaintingCount());
            assertEquals("Painting A", result.getPainting(0).getTitle());
            assertEquals("Painting B", result.getPainting(1).getTitle());
        }
    }

    @Test
    void shouldReturnEmptyListIfNoPaintingsByArtist() {
        UUID artistId = UUID.randomUUID();
        when(paintingRepository.findAllByArtistId(eq(artistId), any(PageRequest.class)))
                .thenReturn(Page.empty());

        StreamObserver<AllPaintingResponse> observer = mock(StreamObserver.class);
        ArgumentCaptor<AllPaintingResponse> captor = ArgumentCaptor.forClass(AllPaintingResponse.class);

        grpcPaintingService.getAllPaintingByArtistId(
                AllPaintingByArtistIdRequest.newBuilder()
                        .setArtistId(ByteString.copyFromUtf8(artistId.toString()))
                        .setPage(0).setSize(10).build(),
                observer
        );

        verify(observer).onNext(captor.capture());
        verify(observer).onCompleted();

        assertTrue(captor.getValue().getPaintingList().isEmpty());
    }

    @Test
    void shouldThrowExceptionOnInvalidUuid() {
        StreamObserver<AllPaintingResponse> observer = mock(StreamObserver.class);

        assertThrows(IllegalArgumentException.class, () ->
                grpcPaintingService.getAllPaintingByArtistId(
                        AllPaintingByArtistIdRequest.newBuilder()
                                .setArtistId(ByteString.copyFromUtf8("invalid-uuid"))
                                .setPage(0).setSize(10).build(),
                        observer
                )
        );

        verify(observer, never()).onNext(any());
        verify(observer, never()).onCompleted();
    }
}
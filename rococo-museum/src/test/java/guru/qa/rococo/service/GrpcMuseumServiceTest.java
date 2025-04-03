package guru.qa.rococo.service;

import com.google.protobuf.ByteString;
import guru.qa.grpc.rococo.grpc.*;
import guru.qa.rococo.data.MuseumEntity;
import guru.qa.rococo.data.repository.MuseumRepository;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrpcMuseumServiceTest {

    @Mock
    private MuseumRepository museumRepository;
    @Mock
    private StreamObserver<AllMuseumResponse> observer;
    @Mock
    private StreamObserver<MuseumResponse> museumObserver;

    private GrpcMuseumService grpcMuseumService;

    @BeforeEach
    void setUp() {
        grpcMuseumService = new GrpcMuseumService(museumRepository);
    }

    @Nested
    class GetMuseumTests {

        @Test
        void shouldReturnMuseumIfExists() {
            UUID museumId = randomUUID();
            MuseumEntity entity = new MuseumEntity();
            entity.setId(museumId);

            when(museumRepository.findById(museumId)).thenReturn(Optional.of(entity));

            MuseumResponse response = MuseumResponse.newBuilder()
                    .setId(ByteString.copyFromUtf8(museumId.toString()))
                    .setTitle("Mock title")
                    .setDescription("Mock description")
                    .build();

            try (MockedStatic<MuseumEntity> mocked = mockStatic(MuseumEntity.class)) {
                mocked.when(() -> MuseumEntity.toGrpcMessage(entity)).thenReturn(response);

                StreamObserver<MuseumResponse> observer = mock(StreamObserver.class);
                grpcMuseumService.getMuseum(MuseumRequest.newBuilder().setId(ByteString.copyFromUtf8(museumId.toString())).build(), observer);

                verify(observer).onNext(response);
                verify(observer).onCompleted();
                verify(observer, never()).onError(any());
            }
        }

        @Test
        void shouldReturnErrorIfMuseumNotFound() {
            UUID museumId = randomUUID();
            when(museumRepository.findById(museumId)).thenReturn(Optional.empty());

            StreamObserver<MuseumResponse> observer = mock(StreamObserver.class);

            grpcMuseumService.getMuseum(
                    MuseumRequest.newBuilder().setId(ByteString.copyFromUtf8(museumId.toString())).build(),
                    observer
            );

            verify(observer).onError(argThat(error -> {
                assertTrue(error instanceof StatusRuntimeException ex &&
                        ex.getStatus().getCode() == Status.NOT_FOUND.getCode() &&
                        ex.getStatus().getDescription().contains("Museum not found by id"));
                return true;
            }));

            verify(observer, never()).onNext(any());
            verify(observer, never()).onCompleted();
        }
    }

    @Test
    void shouldReturnPagedMuseums() {
        String title = "art";
        int page = 0;
        int size = 2;

        AllMuseumRequest request = AllMuseumRequest.newBuilder().setTitle(title).setPage(page).setSize(size).build();

        UUID id1 = randomUUID(), id2 = randomUUID();
        MuseumEntity e1 = new MuseumEntity();
        e1.setId(id1);
        e1.setTitle("Art 1");
        MuseumEntity e2 = new MuseumEntity();
        e2.setId(id2);
        e2.setTitle("Art 2");

        Page<MuseumEntity> pageResult = new PageImpl<>(List.of(e1, e2));
        when(museumRepository.findAllByTitleContainsIgnoreCase(eq(title), any(PageRequest.class))).thenReturn(pageResult);

        MuseumResponse r1 = MuseumResponse.newBuilder().setId(ByteString.copyFromUtf8(id1.toString())).setTitle("Art 1").build();
        MuseumResponse r2 = MuseumResponse.newBuilder().setId(ByteString.copyFromUtf8(id2.toString())).setTitle("Art 2").build();

        try (MockedStatic<MuseumEntity> mocked = mockStatic(MuseumEntity.class)) {
            mocked.when(() -> MuseumEntity.toGrpcMessage(e1)).thenReturn(r1);
            mocked.when(() -> MuseumEntity.toGrpcMessage(e2)).thenReturn(r2);

            StreamObserver<AllMuseumResponse> observer = mock(StreamObserver.class);
            ArgumentCaptor<AllMuseumResponse> captor = ArgumentCaptor.forClass(AllMuseumResponse.class);

            grpcMuseumService.getAllMuseum(request, observer);

            verify(observer).onNext(captor.capture());
            verify(observer).onCompleted();
            verify(observer, never()).onError(any());

            AllMuseumResponse result = captor.getValue();
            assertEquals(2, result.getMuseumCount());
            assertEquals(2, result.getTotalCount());
            assertEquals("Art 1", result.getMuseum(0).getTitle());
            assertEquals("Art 2", result.getMuseum(1).getTitle());
        }
    }

    @Nested
    class UpdateMuseumTests {

        @Test
        void shouldUpdateMuseumIfExists() {
            UUID museumId = randomUUID();

            UpdateMuseumRequest request = UpdateMuseumRequest.newBuilder()
                    .setId(ByteString.copyFromUtf8(museumId.toString()))
                    .setMuseumData(AddMuseumRequest.newBuilder().setTitle("Updated Museum").setDescription("Updated description").build())
                    .build();

            MuseumEntity existing = new MuseumEntity();
            existing.setId(museumId);
            MuseumEntity updated = new MuseumEntity();
            updated.setId(museumId);
            updated.setTitle("Updated Museum");

            when(museumRepository.findById(museumId)).thenReturn(Optional.of(existing));
            when(museumRepository.save(any())).thenReturn(updated);

            MuseumResponse expected = MuseumResponse.newBuilder()
                    .setId(ByteString.copyFromUtf8(museumId.toString()))
                    .setTitle("Updated Museum").build();

            try (MockedStatic<MuseumEntity> mocked = mockStatic(MuseumEntity.class)) {
                mocked.when(() -> MuseumEntity.fromUpdateMuseumGrpcMessage(request)).thenReturn(updated);
                mocked.when(() -> MuseumEntity.toGrpcMessage(updated)).thenReturn(expected);

                grpcMuseumService.updateMuseum(request, museumObserver);

                ArgumentCaptor<MuseumResponse> captor = ArgumentCaptor.forClass(MuseumResponse.class);
                verify(museumObserver).onNext(captor.capture());
                verify(museumObserver).onCompleted();
                verify(museumObserver, never()).onError(any());

                assertEquals("Updated Museum", captor.getValue().getTitle());
            }
        }

        @Test
        void shouldReturnNotFoundIfMuseumMissing() {
            UUID museumId = randomUUID();

            UpdateMuseumRequest request = UpdateMuseumRequest.newBuilder()
                    .setId(ByteString.copyFromUtf8(museumId.toString()))
                    .setMuseumData(AddMuseumRequest.newBuilder().setTitle("Non-existent Museum").build())
                    .build();

            when(museumRepository.findById(museumId)).thenReturn(Optional.empty());

            grpcMuseumService.updateMuseum(request, museumObserver);

            verify(museumObserver).onError(argThat(error ->
                    error instanceof StatusRuntimeException ex &&
                            ex.getStatus().getCode() == Status.NOT_FOUND.getCode() &&
                            ex.getStatus().getDescription().contains(museumId.toString())
            ));

            verify(museumObserver, never()).onNext(any());
            verify(museumObserver, never()).onCompleted();
        }
    }

    @Test
    void shouldReturnMuseumsByIds() {
        UUID id1 = randomUUID(), id2 = randomUUID();

        MuseumIdsRequest request = MuseumIdsRequest.newBuilder()
                .addId(ByteString.copyFromUtf8(id1.toString()))
                .addId(ByteString.copyFromUtf8(id2.toString()))
                .build();

        MuseumEntity e1 = new MuseumEntity();
        e1.setId(id1);
        e1.setTitle("Museum One");
        MuseumEntity e2 = new MuseumEntity();
        e2.setId(id2);
        e2.setTitle("Museum Two");

        when(museumRepository.findAllByIdIn(Set.of(id1, id2))).thenReturn(List.of(e1, e2));

        MuseumResponse r1 = MuseumResponse.newBuilder().setId(ByteString.copyFromUtf8(id1.toString())).setTitle("Museum One").build();
        MuseumResponse r2 = MuseumResponse.newBuilder().setId(ByteString.copyFromUtf8(id2.toString())).setTitle("Museum Two").build();

        try (MockedStatic<MuseumEntity> mocked = mockStatic(MuseumEntity.class)) {
            mocked.when(() -> MuseumEntity.toGrpcMessage(e1)).thenReturn(r1);
            mocked.when(() -> MuseumEntity.toGrpcMessage(e2)).thenReturn(r2);

            StreamObserver<AllMuseumByIdsResponse> observer = mock(StreamObserver.class);
            ArgumentCaptor<AllMuseumByIdsResponse> captor = ArgumentCaptor.forClass(AllMuseumByIdsResponse.class);

            grpcMuseumService.getMuseumByIds(request, observer);

            verify(observer).onNext(captor.capture());
            verify(observer).onCompleted();
            verify(observer, never()).onError(any());

            List<String> titles = captor.getValue().getMuseumList().stream().map(MuseumResponse::getTitle).toList();
            assertEquals(2, titles.size());
            assertTrue(titles.containsAll(List.of("Museum One", "Museum Two")));
        }
    }
}
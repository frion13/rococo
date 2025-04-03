package guru.qa.rococo.service;


import com.google.protobuf.ByteString;
import guru.qa.grpc.rococo.grpc.*;
import guru.qa.rococo.data.ArtistEntity;
import guru.qa.rococo.data.repository.ArtistRepository;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

class GrpcArtistServiceTest {

    private ArtistRepository artistRepository;
    private GrpcArtistService grpcArtistService;

    @BeforeEach
    void setup() {
        artistRepository = mock(ArtistRepository.class);
        grpcArtistService = new GrpcArtistService(artistRepository);
    }

    @Test
    void shouldReturnArtistById() {
        UUID id = UUID.randomUUID();
        ArtistEntity entity = new ArtistEntity();
        entity.setId(id);
        entity.setName("John");
        entity.setBiography("Famous artist");
        entity.setPhoto(new byte[]{1, 2, 3});

        when(artistRepository.findById(id)).thenReturn(Optional.of(entity));

        StreamObserver<ArtistResponse> observer = mock(StreamObserver.class);
        ArtistRequest request = ArtistRequest.newBuilder()
                .setId(ByteString.copyFromUtf8(id.toString()))
                .build();

        grpcArtistService.getArtist(request, observer);

        verify(observer).onNext(any(ArtistResponse.class));
        verify(observer).onCompleted();
    }

    @Test
    void shouldReturnNotFoundIfArtistNotExist() {
        UUID id = UUID.randomUUID();
        when(artistRepository.findById(id)).thenReturn(Optional.empty());

        StreamObserver<ArtistResponse> observer = mock(StreamObserver.class);
        ArtistRequest request = ArtistRequest.newBuilder()
                .setId(ByteString.copyFromUtf8(id.toString()))
                .build();

        grpcArtistService.getArtist(request, observer);

        verify(observer).onError(any());
    }

    @Test
    void shouldReturnAllArtists() {
        ArtistEntity entity1 = new ArtistEntity();
        entity1.setId(UUID.randomUUID());
        entity1.setName("Artist1");
        entity1.setBiography("Bio1");
        entity1.setPhoto(new byte[]{1});

        ArtistEntity entity2 = new ArtistEntity();
        entity2.setId(UUID.randomUUID());
        entity2.setName("Artist2");
        entity2.setBiography("Bio2");
        entity2.setPhoto(new byte[]{2});

        when(artistRepository.findAllByNameContainsIgnoreCase(eq(""), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(entity1, entity2)));

        StreamObserver<AllArtistResponse> observer = mock(StreamObserver.class);
        AllArtistRequest request = AllArtistRequest.newBuilder()
                .setName("")
                .setPage(0)
                .setSize(10)
                .build();

        grpcArtistService.getAllArtist(request, observer);

        verify(observer).onNext(argThat(resp ->
                resp.getArtistsCount() == 2 &&
                        resp.getTotalCount() == 2
        ));
        verify(observer).onCompleted();
    }

    @Test
    void shouldAddArtist() {
        AddArtistRequest addRequest = AddArtistRequest.newBuilder()
                .setName("New Artist")
                .setBiography("New Bio")
                .setPhoto(ByteString.copyFrom(new byte[]{10}))
                .build();

        ArtistEntity savedEntity = new ArtistEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setName("New Artist");
        savedEntity.setBiography("New Bio");
        savedEntity.setPhoto(new byte[]{10});

        when(artistRepository.save(any())).thenReturn(savedEntity);

        StreamObserver<ArtistResponse> observer = mock(StreamObserver.class);
        grpcArtistService.addArtist(addRequest, observer);

        verify(observer).onNext(any(ArtistResponse.class));
        verify(observer).onCompleted();
    }

    @Test
    void shouldUpdateArtist() {
        UUID id = UUID.randomUUID();

        ArtistEntity existingEntity = new ArtistEntity();
        existingEntity.setId(id);
        existingEntity.setName("Old Name");

        when(artistRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(artistRepository.save(any())).thenReturn(existingEntity);

        AddArtistRequest artistData = AddArtistRequest.newBuilder()
                .setName("Updated Name")
                .setBiography("Updated Bio")
                .setPhoto(ByteString.copyFrom(new byte[]{1}))
                .build();

        UpdateArtistRequest request = UpdateArtistRequest.newBuilder()
                .setId(ByteString.copyFromUtf8(id.toString()))
                .setArtistData(artistData)
                .build();

        StreamObserver<ArtistResponse> observer = mock(StreamObserver.class);
        grpcArtistService.updateArtist(request, observer);

        verify(observer).onNext(any(ArtistResponse.class));
        verify(observer).onCompleted();
    }

    @Test
    void shouldReturnNotFoundOnUpdateIfIdMissing() {
        UUID id = UUID.randomUUID();

        when(artistRepository.findById(id)).thenReturn(Optional.empty());

        UpdateArtistRequest request = UpdateArtistRequest.newBuilder()
                .setId(ByteString.copyFromUtf8(id.toString()))
                .setArtistData(AddArtistRequest.newBuilder()
                        .setName("Updated")
                        .setBiography("Bio")
                        .setPhoto(ByteString.copyFrom(new byte[]{1}))
                        .build())
                .build();

        StreamObserver<ArtistResponse> observer = mock(StreamObserver.class);
        grpcArtistService.updateArtist(request, observer);

        verify(observer).onError(any());
    }

    @Test
    void shouldReturnArtistByIds() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        ArtistEntity a1 = new ArtistEntity();
        a1.setId(id1);
        a1.setName("A1");
        a1.setBiography("Bio1");
        a1.setPhoto(new byte[]{1});

        ArtistEntity a2 = new ArtistEntity();
        a2.setId(id2);
        a2.setName("A2");
        a2.setBiography("Bio2");
        a2.setPhoto(new byte[]{2});

        when(artistRepository.findAllByIdIn(Set.of(id1, id2))).thenReturn(List.of(a1, a2));

        ArtistIdsRequest request = ArtistIdsRequest.newBuilder()
                .addId(ByteString.copyFromUtf8(id1.toString()))
                .addId(ByteString.copyFromUtf8(id2.toString()))
                .build();

        StreamObserver<AllArtistByIdsResponse> observer = mock(StreamObserver.class);
        grpcArtistService.getArtistByIds(request, observer);

        verify(observer).onNext(argThat(resp -> resp.getArtistCount() == 2));
        verify(observer).onCompleted();
    }
}
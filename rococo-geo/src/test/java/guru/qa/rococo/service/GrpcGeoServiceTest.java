package guru.qa.rococo.service;

import com.google.protobuf.ByteString;
import guru.qa.grpc.rococo.grpc.*;
import guru.qa.rococo.data.CountryEntity;
import guru.qa.rococo.data.repository.CountryRepository;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GrpcGeoServiceTest {

    private CountryRepository countryRepository;
    private GrpcGeoService grpcGeoService;

    @BeforeEach
    void setUp() {
        countryRepository = mock(CountryRepository.class);
        grpcGeoService = new GrpcGeoService(countryRepository);
    }

    @Test
    void shouldReturnCountryById() {
        UUID id = UUID.randomUUID();
        CountryEntity entity = new CountryEntity();
        entity.setId(id);
        entity.setName("France");

        when(countryRepository.findById(id)).thenReturn(Optional.of(entity));

        StreamObserver<CountryResponse> observer = mock(StreamObserver.class);
        grpcGeoService.getCountry(
                CountryId.newBuilder().setId(ByteString.copyFromUtf8(id.toString())).build(),
                observer
        );

        verify(observer).onNext(any(CountryResponse.class));
        verify(observer).onCompleted();
    }

    @Test
    void shouldReturnNotFoundCountryById() {
        UUID id = UUID.randomUUID();

        when(countryRepository.findById(id)).thenReturn(Optional.empty());

        StreamObserver<CountryResponse> observer = mock(StreamObserver.class);
        grpcGeoService.getCountry(
                CountryId.newBuilder().setId(ByteString.copyFromUtf8(id.toString())).build(),
                observer
        );

        verify(observer).onError(any());
    }

    @Test
    void shouldReturnCountryByName() {
        CountryEntity entity = new CountryEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Germany");

        when(countryRepository.findByName("Germany")).thenReturn(Optional.of(entity));

        StreamObserver<CountryResponse> observer = mock(StreamObserver.class);
        grpcGeoService.getCountryByName(
                CountryName.newBuilder().setName("Germany").build(),
                observer
        );

        verify(observer).onNext(any(CountryResponse.class));
        verify(observer).onCompleted();
    }

    @Test
    void shouldReturnNotFoundCountryByName() {
        when(countryRepository.findByName("Atlantis")).thenReturn(Optional.empty());

        StreamObserver<CountryResponse> observer = mock(StreamObserver.class);
        grpcGeoService.getCountryByName(
                CountryName.newBuilder().setName("Atlantis").build(),
                observer
        );

        verify(observer).onError(any());
    }

    @Test
    void shouldReturnAllCountries() {
        CountryEntity e1 = new CountryEntity();
        e1.setId(UUID.randomUUID());
        e1.setName("Italy");

        CountryEntity e2 = new CountryEntity();
        e2.setId(UUID.randomUUID());
        e2.setName("Spain");

        when(countryRepository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(e1, e2)));

        StreamObserver<AllCountryResponse> observer = mock(StreamObserver.class);
        grpcGeoService.getAllCountry(
                AllCountryRequest.newBuilder().setPage(0).setSize(10).build(),
                observer
        );

        verify(observer).onNext(argThat(response ->
                response.getCountryCount() == 2 &&
                        response.getTotalCount() == 2
        ));
        verify(observer).onCompleted();
    }

    @Test
    void shouldReturnCountriesByIds() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        CountryEntity e1 = new CountryEntity();
        e1.setId(id1);
        e1.setName("Japan");

        CountryEntity e2 = new CountryEntity();
        e2.setId(id2);
        e2.setName("Brazil");

        when(countryRepository.findAllByIdIn(Set.of(id1, id2)))
                .thenReturn(List.of(e1, e2));

        CountryIdsRequest request = CountryIdsRequest.newBuilder()
                .addId(ByteString.copyFromUtf8(id1.toString()))
                .addId(ByteString.copyFromUtf8(id2.toString()))
                .build();

        StreamObserver<AllCountryByIdsResponse> observer = mock(StreamObserver.class);
        grpcGeoService.getCountriesByIds(request, observer);

        verify(observer).onNext(argThat(response -> response.getCountryCount() == 2));
        verify(observer).onCompleted();
    }
}
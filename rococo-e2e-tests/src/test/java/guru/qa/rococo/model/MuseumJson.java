package guru.qa.rococo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.ByteString;
import guru.qa.grpc.rococo.grpc.MuseumResponse;
import guru.qa.rococo.data.entity.MuseumEntity;

import java.util.UUID;

public record MuseumJson(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("title")
        String title,
        @JsonProperty("description")
        String description,
        @JsonProperty("photo")
        String photo,
        @JsonProperty("geo")
        GeoJson geo
) {

    public static MuseumJson fromGrpcMessage(MuseumResponse museumResponse) {
        GeoJson geoJson = new GeoJson(
                museumResponse.getGeo().getCity(),
                new CountryJson(UUID.fromString(museumResponse.getGeo().getCountry().getId().toStringUtf8()), null)
        );
        return new MuseumJson(
                UUID.fromString(museumResponse.getId().toStringUtf8()),
                museumResponse.getTitle(),
                museumResponse.getDescription(),
                museumResponse.getPhoto().toStringUtf8(),
                geoJson);
    }

    public static MuseumJson fromEntity(MuseumEntity entity) {
        return new MuseumJson(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                ByteString.copyFrom(entity.getPhoto()).toStringUtf8(),
                new GeoJson(entity.getCity(), new CountryJson(entity.getGeoId(), null))
        );
    }
}
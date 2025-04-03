package guru.qa.rococo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.ByteString;
import guru.qa.grpc.rococo.grpc.AddArtistRequest;
import guru.qa.grpc.rococo.grpc.ArtistResponse;
import guru.qa.rococo.data.entity.ArtistEntity;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import static java.util.UUID.fromString;


public record ArtistJson(
        @JsonProperty("id") UUID id,
        @JsonProperty("name") String name,
        @JsonProperty("biography") String biography,
        @JsonProperty("photo") byte[] photo
) {

    public static ArtistJson fromGrpcMessage(ArtistResponse response) {
       return new ArtistJson(
               fromString(response.getId().toStringUtf8()),
        response.getName(),
        response.getBiography(),
               response.getPhoto().toByteArray());
    }

    public static AddArtistRequest toGrpcMessage(ArtistJson artist) {
        return AddArtistRequest.newBuilder()
                .setName(artist.name())
                .setBiography(artist.biography())
                .setPhoto(ByteString.copyFrom(artist.photo()))
                .build();
    }

    public static ArtistJson fromEntity(ArtistEntity entity) {
        ArtistJson artistJson = new ArtistJson(
                entity.getId(),
                entity.getName(),
                entity.getBiography(),
                entity.getPhoto());
        return artistJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtistJson that = (ArtistJson) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(biography, that.biography) && Arrays.equals(photo, that.photo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, biography, photo);
    }
}
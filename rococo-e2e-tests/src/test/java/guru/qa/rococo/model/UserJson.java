package guru.qa.rococo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import guru.qa.grpc.rococo.grpc.UserResponse;
import guru.qa.rococo.data.entity.UserEntity;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserJson(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("username")
        String username,
        @JsonProperty("firstname")
        String firstname,
        @JsonProperty("surname")
        String surname,
        @JsonProperty("avatar")
        byte[] avatar) {

    public static UserJson fromEntity(UserEntity entity) {
        return new UserJson(
                entity.getId(),
                entity.getUsername(),
                entity.getFirstname(),
                entity.getLastname(),
                entity.getAvatar()
        );
    }

    @NonNull
    public static UserJson fromGrpcResponse(@Nullable UserResponse response) {

        return new UserJson(
                response != null ? UUID.fromString(response.getId().toString()) : UUID.randomUUID(),
                response != null ? response.getUsername() : "",
                response != null ? response.getFirstname() : "",
                response != null ? response.getLastname() : "",
                response != null ? response.getAvatar().toByteArray() : "".getBytes()
        );
    }
}

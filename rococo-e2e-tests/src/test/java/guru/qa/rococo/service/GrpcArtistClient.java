package guru.qa.rococo.service;

import guru.qa.grpc.rococo.grpc.*;
import guru.qa.rococo.model.ArtistJson;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;

public class GrpcArtistClient {
    private final RococoArtistServiceGrpc.RococoArtistServiceBlockingStub artistStub;

    public GrpcArtistClient(Channel channel) {
        this.artistStub = RococoArtistServiceGrpc.newBlockingStub(channel);
    }

    public ArtistJson searchArtistsByName(String name) {
        try {
            GetArtistRequest request = GetArtistRequest.newBuilder()
                    .setName(name)
                    .build();

            AllArtistResponse response = artistStub.getArtistByName(request);
            return response.getArtistsList().stream()
                    .findFirst()
                    .map(ArtistJson::fromGrpcMessage)
                    .orElse(null);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to search artists by name: " + name, e);
        }
    }

    public ArtistJson createArtist(ArtistJson artist) {
        try {
            AddArtistRequest request = ArtistJson.toGrpcMessage(artist);
            ArtistResponse response = artistStub.addArtist(request);
            return ArtistJson.fromGrpcMessage(response);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to create artist", e);
        }
    }
}
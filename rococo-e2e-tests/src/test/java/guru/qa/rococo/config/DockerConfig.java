package guru.qa.rococo.config;

import javax.annotation.Nonnull;

public class DockerConfig implements Config {
    static final DockerConfig INSTANCE = new DockerConfig();

    private DockerConfig() {
    }

    @Nonnull
    @Override
    public String frontUrl() {
        return "http://client.rococo.dc";
    }

    @Nonnull
    @Override
    public String authUrl() {
        return "http://auth.niffler.dc:9000/";
    }

    @Nonnull
    @Override
    public String authJdbcUrl() {
        return "jdbc:postgresql://niffler-all-db:5432/niffler-auth";
    }


    @Nonnull
    @Override
    public String userdataJdbcUrl() {
        return "jdbc:postgresql://niffler-all-db:5432/niffler-userdata";
    }

    @Override
    public String userdataGrpcAddress() {
        return "";
    }

    @Override
    public String artistGrpcAddress() {
        return "artist.rococo.dc";
    }

    @Override
    public String databaseAddress() {
        return "rococo-all-db:3306";
    }

    @Override
    public String museumGrpcAddress() {
        return "museum.rococo.dc";
    }

    @Override
    public String geoGrpcAddress() {
        return "geo.rococo.dc";
    }

    @Override
    public String paintingGrpcAddress() {
        return "painting.rococo.dc";
    }
}

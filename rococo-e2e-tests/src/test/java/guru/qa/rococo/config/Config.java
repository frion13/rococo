package guru.qa.rococo.config;

import javax.annotation.Nonnull;

public interface Config {

    static Config getInstance() {
        return "docker".equals(System.getProperty("test.env"))
                ? DockerConfig.INSTANCE
                : LocalConfig.INSTANCE;
    }

    @Nonnull
    String frontUrl();

    @Nonnull
    String authUrl();

    @Nonnull
    String authJdbcUrl();

    @Nonnull
    String userdataJdbcUrl();

    String userdataGrpcAddress();

    default int userdataGrpcPort() {
        return 8091;
    }

    String artistGrpcAddress();

    default int artistGrpcPort() {
        return 8092;
    }

    String databaseAddress();

    String museumGrpcAddress();

    default int museumGrpcPort() {
        return 8093;
    }

    String geoGrpcAddress();

    default int geoGrpcPort() {
        return 8094;
    }

    String paintingGrpcAddress();

    default int paintingGrpcPort() {
        return 8095;
    }
}


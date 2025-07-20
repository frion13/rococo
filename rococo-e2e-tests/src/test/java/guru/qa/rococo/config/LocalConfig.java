package guru.qa.rococo.config;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class LocalConfig implements Config {
    static final LocalConfig INSTANCE = new LocalConfig();

    private LocalConfig() {
    }

    @Nonnull
    @Override
    public String frontUrl() {
        return "http://127.0.0.1:3000";
    }

    @Nonnull
    @Override
    public String authUrl() {
        return "http://127.0.0.1:9000/";
    }

    @Nonnull
    @Override
    public String authJdbcUrl() {
        return "localhost:3306/rococo-auth";
    }

    @Nonnull
    @Override
    public String userdataJdbcUrl() {
        return "127.0.0.1:3306/rococo-userdata";
    }

    @NotNull
    @Override
    public String userdataGrpcAddress() {
        return "localhost";
    }

    @Override
    public String artistGrpcAddress() {
        return "localhost";
    }

    @Override
    public String museumGrpcAddress() {
        return "localhost";
    }

    @Override
    public String geoGrpcAddress() {
        return "localhost";
    }

    @Override
    public String paintingGrpcAddress() {
        return "localhost";
    }

    @Override
    public String databaseAddress() {
        return "localhost:3306";
    }
}


package guru.qa.rococo.data.dao;

import guru.qa.rococo.config.Config;
import guru.qa.rococo.data.entity.auth.AuthUserEntity;
import guru.qa.rococo.data.jpa.RococoDataSources;
import guru.qa.rococo.data.mapper.AuthUserEntityRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class AuthUserDaoSpringJdbc implements AuthUserDao {

    private static final Config CFG = Config.getInstance();

    @Override
    @Nonnull
    public AuthUserEntity createUser(AuthUserEntity authUser) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(RococoDataSources.dataSource(CFG.authJdbcUrl()));

        UUID userId = UUID.randomUUID(); // 👉 создаем UUID вручную
        authUser.setId(userId);

        jdbcTemplate.update(
                "INSERT INTO `user` " +
                        "(id, username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                uuidToBytes(userId),
                authUser.getUsername(),
                authUser.getPassword(),
                authUser.getEnabled(),
                authUser.getAccountNonExpired(),
                authUser.getAccountNonLocked(),
                authUser.getCredentialsNonExpired()
        );
        return authUser;

    }

    @Override
    @Nonnull
    public AuthUserEntity update(AuthUserEntity user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(RococoDataSources.dataSource(CFG.authJdbcUrl()));
        jdbcTemplate.update("UPDATE `user` " +
                        "SET username = ?, password = ?, enabled = ?, account_non_expired = ?, account_non_locked = ?, credentials_non_expired = ? " +
                        "WHERE id = ?",

                user.getUsername(),
                user.getPassword(),
                user.getEnabled(),
                user.getAccountNonExpired(),
                user.getAccountNonLocked(),
                user.getCredentialsNonExpired()

        );
        return user;
    }

    @Override
    @Nonnull
    public Optional<AuthUserEntity> findById(UUID id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(RococoDataSources.dataSource(CFG.authJdbcUrl()));
        return Optional.ofNullable(
                jdbcTemplate.queryForObject(
                        "SELECT * FROM `user` WHERE id = ? ",
                        AuthUserEntityRowMapper.instance,
                        id
                )
        );
    }

    @Override
    @Nonnull
    public Optional<AuthUserEntity> findByUserName(String username) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(RococoDataSources.dataSource(CFG.authJdbcUrl()));
        return Optional.ofNullable(
                jdbcTemplate.queryForObject(
                        "SELECT * FROM `user` WHERE username = ? ",
                        AuthUserEntityRowMapper.instance,
                        username
                )
        );
    }

    @Override
    @Nonnull
    public List<AuthUserEntity> findAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(RococoDataSources.dataSource(CFG.authJdbcUrl()));
        return jdbcTemplate.query(
                "SELECT * FROM `user`",
                AuthUserEntityRowMapper.instance);
    }

    @Override
    public void remove(AuthUserEntity authUser) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(RococoDataSources.dataSource(CFG.authJdbcUrl()));
        jdbcTemplate.update(
                "DELETE FROM `user` WHERE id = ?", authUser.getId()
        );
    }

    private static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
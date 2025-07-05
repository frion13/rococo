package guru.qa.rococo.data.impl;


import guru.qa.rococo.config.Config;
import guru.qa.rococo.data.dao.UserdataUserDao;
import guru.qa.rococo.data.entity.UserEntity;
import guru.qa.rococo.data.jpa.RococoDataSources;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class UserdataUserDaoSpringJdbc implements UserdataUserDao {

    private static final Config CFG = Config.getInstance();
    private final String url = CFG.userdataJdbcUrl();

    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(RococoDataSources.dataSource(url));

    @Override
    @Nonnull
    public UserEntity create(UserEntity user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                            INSERT INTO `user` (username, firstname, lastname, avatar)
                            VALUES (?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getFirstname());
            ps.setString(3, user.getLastname());
            ps.setBytes(4, user.getAvatar());
            return ps;
        }, keyHolder);

        return user;
    }
}

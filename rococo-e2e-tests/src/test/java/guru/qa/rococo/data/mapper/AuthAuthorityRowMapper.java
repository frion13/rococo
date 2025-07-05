package guru.qa.rococo.data.mapper;

import guru.qa.rococo.data.entity.auth.Authority;
import guru.qa.rococo.data.entity.auth.AuthorityEntity;
import org.springframework.jdbc.core.RowMapper;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AuthAuthorityRowMapper implements RowMapper<AuthorityEntity> {

    public static final AuthAuthorityRowMapper instance = new AuthAuthorityRowMapper();

    private AuthAuthorityRowMapper() {
    }

    @Override
    @Nonnull
    public AuthorityEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuthorityEntity authority = new AuthorityEntity();
        authority.setId(rs.getObject("id", UUID.class));
        authority.setAuthority(Authority.valueOf(rs.getString("authority")));

        return authority;
    }
}
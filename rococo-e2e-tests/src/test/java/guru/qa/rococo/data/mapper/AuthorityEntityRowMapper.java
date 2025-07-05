package guru.qa.rococo.data.mapper;

import guru.qa.rococo.data.entity.auth.AuthUserEntity;
import guru.qa.rococo.data.entity.auth.Authority;
import guru.qa.rococo.data.entity.auth.AuthorityEntity;
import org.springframework.jdbc.core.RowMapper;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AuthorityEntityRowMapper implements RowMapper<AuthorityEntity> {

  public static final AuthorityEntityRowMapper instance = new AuthorityEntityRowMapper();

  private AuthorityEntityRowMapper() {
  }

  @Override
  @Nonnull
  public AuthorityEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
    AuthorityEntity ae = new AuthorityEntity();
    ae.setId(rs.getObject("id", UUID.class));

    AuthUserEntity user = new AuthUserEntity();
    user.setId(rs.getObject("user_id", UUID.class));
    ae.setUser(user);

    ae.setAuthority(Authority.valueOf(rs.getString("authority")));
    return ae;
  }
}

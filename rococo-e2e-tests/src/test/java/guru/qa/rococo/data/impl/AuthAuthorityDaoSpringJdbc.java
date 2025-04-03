package guru.qa.rococo.data.impl;

import guru.qa.rococo.config.Config;
import guru.qa.rococo.data.dao.AuthAuthorityDao;
import guru.qa.rococo.data.entity.auth.AuthorityEntity;
import guru.qa.rococo.data.jpa.RococoDataSources;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class AuthAuthorityDaoSpringJdbc implements AuthAuthorityDao {

    private static final Config CFG = Config.getInstance();
    private final String url = CFG.authJdbcUrl();

    @Override
    public void createAuthority(AuthorityEntity... authority) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(RococoDataSources.dataSource(url));
        jdbcTemplate.batchUpdate(
                "INSERT INTO authority (id, user_id, authority) VALUES (?, ? , ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setBytes(1, uuidToBin(UUID.randomUUID())); // id
                        ps.setBytes(2, uuidToBin(authority[i].getUser().getId())); // user_id
                        ps.setString(3, authority[i].getAuthority().name()); // authority

                    }//                    }

                    @Override
                    public int getBatchSize() {
                        return authority.length;
                    }
                }
        );
    }

    private byte[] uuidToBin(UUID uuid) {
        // UUID_TO_BIN(uuid, true): меняет порядок байт (time-first layout)
        // порядок байтов: time_low, time_mid, time_hi, clock_seq_hi, clock_seq_low, node
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] buffer = new byte[16];

        // Reorder bytes to match UUID_TO_BIN(uuid, true)
        buffer[0] = (byte) (msb >>> 56);
        buffer[1] = (byte) (msb >>> 48);
        buffer[2] = (byte) (msb >>> 40);
        buffer[3] = (byte) (msb >>> 32);
        buffer[4] = (byte) (msb >>> 24);
        buffer[5] = (byte) (msb >>> 16);
        buffer[6] = (byte) (msb >>> 8);
        buffer[7] = (byte) (msb);

        buffer[8] = (byte) (lsb >>> 56);
        buffer[9] = (byte) (lsb >>> 48);
        buffer[10] = (byte) (lsb >>> 40);
        buffer[11] = (byte) (lsb >>> 32);
        buffer[12] = (byte) (lsb >>> 24);
        buffer[13] = (byte) (lsb >>> 16);
        buffer[14] = (byte) (lsb >>> 8);
        buffer[15] = (byte) (lsb);

        return buffer;
    }
}
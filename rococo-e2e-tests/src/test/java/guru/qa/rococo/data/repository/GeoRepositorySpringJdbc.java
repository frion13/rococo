package guru.qa.rococo.data.repository;


import guru.qa.rococo.config.Config;
import guru.qa.rococo.data.entity.CountryEntity;
import guru.qa.rococo.data.jpa.RococoDataSources;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class GeoRepositorySpringJdbc implements GeoRepository {

    private static final Config CFG = Config.getInstance();
    JdbcTemplate jdbcTemplate = new JdbcTemplate(RococoDataSources.dataSource(CFG.databaseAddress() + "/rococo-geo"));


    private static final String COUNTRY_TABLE = "country";
    private static final String ID_COLUMN = "id";
    private static final String NAME_COLUMN = "name";


    private final RowMapper<CountryEntity> countryRowMapper = new RowMapper<>() {
        @Override
        public CountryEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            CountryEntity country = new CountryEntity();
            country.setId(bytesToUuid(rs.getBytes(ID_COLUMN)));             country.setName(rs.getString(NAME_COLUMN));
            return country;
        }
    };

    private UUID bytesToUuid(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return new UUID(bb.getLong(), bb.getLong());
    }

    @Override
    public CountryEntity getCountryById(UUID countryId) {
        String sql = String.format("SELECT * FROM %s WHERE %s = ?", COUNTRY_TABLE, ID_COLUMN);
        return jdbcTemplate.queryForObject(sql, countryRowMapper, countryId);
    }

    @Override
    public CountryEntity getCountryByName(String countryName) {
        String sql = String.format("SELECT * FROM %s WHERE %s = ?", COUNTRY_TABLE, NAME_COLUMN);
        return jdbcTemplate.queryForObject(sql, countryRowMapper, countryName);
    }

    @Override
    public List<CountryEntity> getAllCountries() {
        String sql = String.format("SELECT * FROM %s", COUNTRY_TABLE);
        return jdbcTemplate.query(sql, countryRowMapper);
    }
}

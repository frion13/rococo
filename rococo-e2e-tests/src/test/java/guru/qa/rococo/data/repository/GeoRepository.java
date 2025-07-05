package guru.qa.rococo.data.repository;

import guru.qa.rococo.data.entity.CountryEntity;

import java.util.List;
import java.util.UUID;

public interface GeoRepository {
    CountryEntity getCountryById(UUID countryId);

    CountryEntity getCountryByName(String countryName);
    List<CountryEntity> getAllCountries();
}

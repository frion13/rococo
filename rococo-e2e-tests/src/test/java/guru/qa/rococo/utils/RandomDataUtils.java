package guru.qa.rococo.utils;

import com.github.javafaker.Faker;
import guru.qa.rococo.data.repository.GeoRepository;
import guru.qa.rococo.data.repository.GeoRepositorySpringJdbc;
import guru.qa.rococo.model.CountryJson;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class RandomDataUtils {

    private static final Faker faker = new Faker();

    public static String randomUsername() {
        return faker.name().username();
    }

    public static String randomName() {
        return faker.name().firstName();
    }

    public static String randomSurname() {
        return faker.name().lastName();
    }

    public static String randomSentence(int wordsCount) {
        return faker.lorem().sentence(wordsCount);
    }

    @Nonnull
    @Step("Получение случайного названия страны из сервиса rococo-geo")
    public static synchronized CountryJson randomCountry() {
        GeoRepository geoRepository = new GeoRepositorySpringJdbc();

        List<CountryJson> countryName = geoRepository.getAllCountries().stream().map(CountryJson::fromEntity).toList();
        Random random = new Random();
        int randomIndex = random.nextInt(countryName.size());
        return countryName.get(randomIndex);
    }

    public static String randomCity (){
        return  faker.address().city();
    }
}

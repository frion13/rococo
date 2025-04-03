package guru.qa.rococo.test.web;

import guru.qa.rococo.jupiter.annotation.ApiLogin;
import guru.qa.rococo.jupiter.annotation.User;
import guru.qa.rococo.jupiter.annotation.WebTest;
import guru.qa.rococo.page.ArtistPage;
import guru.qa.rococo.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.codeborne.selenide.Selenide.open;


@WebTest
public class ArtistTest {
    @Test
    @User
    @ApiLogin
    @DisplayName("WEB: Пользователь может добавить нового художника")
    void shouldAddNewArtist() throws IOException {
        String name = RandomDataUtils.randomName() + " " + RandomDataUtils.randomSurname();
        String biography = RandomDataUtils.randomSentence(15);

        open(ArtistPage.URL, ArtistPage.class)
                .clickAddArtistBtn()
                .setArtistName(name)
                .uploadPhoto("img/artist.jpg")
                .setBiography(biography)
                .checkArtistExistsInArtistsList(name);
    }

    @Test
    @Tag("WEB")
    @User
    @ApiLogin
    @DisplayName("WEB: Ошибка при добавлении художника с именем менее 3 символов")
    void shouldShowErrorWhenArtistNameLessThan3Chars() throws IOException {
        String biography = RandomDataUtils.randomSentence(15);
        open(ArtistPage.URL, ArtistPage.class)
                .clickAddArtistBtn()
                .setArtistName("sf")
                .uploadPhoto("img/artist.jpg")
                .setBiography(biography)
                .checkErrorMsg("Имя не может быть короче 3 символов");
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("WEB: Ошибка при добавлении художника с биографией менее 9 символов")
    void shouldShowErrorWhenArtistBiographyLessThan9Chars() throws IOException {
        String biography = "bio";
        String name = RandomDataUtils.randomName() + " " + RandomDataUtils.randomSurname();

        open(ArtistPage.URL, ArtistPage.class)
                .clickAddArtistBtn()
                .setArtistName(name)
                .uploadPhoto("img/artist.jpg")
                .setBiography(biography)
                .checkErrorMsg("Биография не может быть короче 10 символов");
    }
}

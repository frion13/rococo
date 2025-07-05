package guru.qa.rococo.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class ArtistPage extends BasePage<ArtistPage> {
    public static final String URL = CFG.frontUrl() + "/artist";

    private final SelenideElement
            addArtistBtn = $("button.btn"),
            artistNameInput = $("input[type='text']"),
            photoInput = $("input[name='photo']"),
            biographyInput = $("textarea[name='biography']");

    ElementsCollection artistsName = $$("ul li span"),
            errorMsg = $$(".text-error-400");


    @Override
    public ArtistPage checkThatPageLoaded() {
        return null;
    }

    @Step("Нажимаем кнопку 'Добавить художника'")
    public ArtistPage clickAddArtistBtn() {
        addArtistBtn.shouldBe(clickable).click();
        return this;
    }

    @Step("Вводим имя '{0}'")
    public ArtistPage setArtistName(String name) {
        artistNameInput.setValue(name);
        return this;
    }

    @Step("Загружаем фото художника: {filePath}")
    public ArtistPage uploadPhoto(String filePath) {
        photoInput.uploadFromClasspath(filePath);//        photoInput.uploadFromClasspath(Arrays.toString(filePath));
        return this;
    }

    @Step("Вводим биографию художника")
    public ArtistPage setBiography(String biography) {
        biographyInput.setValue(biography).submit();
        return this;
    }

    @Step("Проверка, что {0} присутствует в списке художниев")
    public void checkArtistExistsInArtistsList(String artistName) {
        artistsName.findBy(text(artistName)).shouldBe(visible);
    }

    @Step("Проверка, что присутствует ошибка {0}")
    public void checkErrorMsg(String msg) {
        errorMsg.findBy(text(msg)).shouldBe(visible);
    }
}

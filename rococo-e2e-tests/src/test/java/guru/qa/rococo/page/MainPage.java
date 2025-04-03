package guru.qa.rococo.page;

import guru.qa.rococo.page.component.Header;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

@ParametersAreNonnullByDefault
public class MainPage extends BasePage<MainPage> {
    public static final String URL = CFG.frontUrl();
    protected final Header header = new Header();


    @Override
    public MainPage checkThatPageLoaded() {
        return null;
    }

    @Nonnull
    public Header getHeader() {
        return header;
    }

    @Step("Проверяем, что пользователь авторизовался")
    @Nonnull
    public MainPage checkUserIsLoggedIn() {
        $("[data-testid]").shouldHave(visible);
        return this;
    }
}
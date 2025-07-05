package guru.qa.rococo.page.component;

import com.codeborne.selenide.SelenideElement;
import guru.qa.rococo.page.LoginPage;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class Header extends BaseComponent<Header> {

    private final SelenideElement loginButton = $$("button").findBy(text("Войти"));

    public Header() {
        super($("#shell-header"));
    }


    @Step("Click login button")
    public LoginPage login() {
        loginButton.click();
        return new LoginPage();
    }
}

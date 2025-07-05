package guru.qa.rococo.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.interactable;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

@ParametersAreNonnullByDefault
public class RegisterPage extends BasePage<RegisterPage> {
    private final SelenideElement
            userNameInput = $("#username"),
            passwordInput = $("#password"),
            passwordSubmitInput = $("#passwordSubmit"),
            successRegisterText = $(".form__subheader"),
            formSubmit = $("a[href]"),
            errorRegisterText = $(".form__error");

    @Override
    public RegisterPage checkThatPageLoaded() {
        return null;
    }

    @Step("Заполняем логин и пароль")
    @Nonnull
    public RegisterPage fillRegisterForm(String userName, String password, String submitPassword) {
        setUserName(userName);
        setPassword(password);
        setPasswordSubmit(submitPassword);
        return this;
    }

    @Nonnull
    public RegisterPage setUserName(String userName) {
        userNameInput.shouldBe(interactable).setValue(userName);
        return this;
    }

    @Nonnull
    public RegisterPage setPassword(String password) {
        passwordInput.setValue(password);
        return this;
    }

    @Nonnull
    public RegisterPage setPasswordSubmit(String password) {
        passwordSubmitInput.setValue(password).pressEnter();
        return this;
    }

    @Step("Проверка успешной регистрации")
    @Nonnull
    public RegisterPage checkSuccsessRegister() {
        successRegisterText.shouldHave(text("Добро пожаловать в Ro"));
        return this;
    }

    public LoginPage clickLoginButton() {
        formSubmit.click();
        return new LoginPage();
    }

    @Step("Проверка ошибки при регистрации")
    @Nonnull
    public RegisterPage checkErrorRegisterMessage(String text) {
        errorRegisterText.shouldHave(text(text));
        return this;
    }
}

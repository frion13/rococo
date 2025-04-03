package guru.qa.rococo.page;


import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;


@ParametersAreNonnullByDefault
public class LoginPage extends BasePage<LoginPage> {

    private final SelenideElement
            userNameInput,
            passwordInput,
            formRegisterButton,
            errorForm;

    public LoginPage() {
        this.userNameInput = $("input[name='username']");
        this.passwordInput = $("input[name='password']");
        this.formRegisterButton = $("a[href = '/register']");
        this.errorForm = $(".form__error");
    }

    @Override
    @Nonnull
    public LoginPage checkThatPageLoaded() {
        userNameInput.should(visible);
        passwordInput.should(visible);
        return this;
    }


    @Step("Вводим логин '{0}'")
    @Nonnull
    public LoginPage setUserName(String userName) {
        userNameInput.shouldBe(interactable).setValue(userName);
        return this;
    }

    @Step("Вводим пароль '{0}'")
    @Nonnull
    public LoginPage setPassword(String userName) {
        passwordInput.setValue(userName).pressEnter();
        return this;
    }

    @Step("Проверка ошибки при авторизации")
    @Nonnull
    public LoginPage checkBadCredentialError() {
        errorForm.shouldHave(text("Неверные учетные данные пользователя"));
        return this;
    }

    @Step("Нажимаем на кнопку 'Регистрация'")
    public RegisterPage clickOnRegisterButton() {
        formRegisterButton.click();
        return new RegisterPage();
    }
}
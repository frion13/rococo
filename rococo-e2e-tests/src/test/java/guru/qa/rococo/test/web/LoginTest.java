package guru.qa.rococo.test.web;

import guru.qa.rococo.config.Config;
import guru.qa.rococo.jupiter.annotation.User;
import guru.qa.rococo.jupiter.annotation.WebTest;
import guru.qa.rococo.model.UserJson;
import guru.qa.rococo.page.MainPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.open;
import static guru.qa.rococo.utils.RandomDataUtils.randomUsername;


@WebTest
public class LoginTest {
    private static final Config CFG = Config.getInstance();

    @Test
    @DisplayName("Ошибка во время ввода невалидных данных")
    void shouldBeErrorWithBadCredential() {
        open(CFG.frontUrl(), MainPage.class)
                .getHeader()
                .login()
                .setUserName(randomUsername())
                .setPassword("1234")
                .checkBadCredentialError();
    }

    @User
    @Test
    @DisplayName("Успешный вход с валидными логином и паролем")
    void shouldLoginSuccessfully(UserJson user) {
        open(MainPage.URL, MainPage.class)
                .getHeader()
                .login()
                .setUserName(user.username())
                .setPassword("123");
        new MainPage().checkUserIsLoggedIn();
    }

    @Test
    @DisplayName("UI: Регистрация нового пользователя - пользователь должен успешно зарегистрироваться")
    void shouldBeRegisterNewUser() {
        String userName = randomUsername();
        String password = "123";
        open(CFG.frontUrl(), MainPage.class)
                .getHeader()
                .login()
                .clickOnRegisterButton()
                .fillRegisterForm(userName, password, password)
                .checkSuccsessRegister();
    }

    @Test
    void shouldShowErrorIfPasswordAndConfirmPasswordAreNotEqual() {
        String userName = randomUsername();
        String password = "123";
        String submitPassword = "1234";
        open(CFG.frontUrl(), MainPage.class)
                .getHeader()
                .login()
                .clickOnRegisterButton()
                .fillRegisterForm(userName, password, submitPassword)
                .checkErrorRegisterMessage("Passwords should be equal");
    }

    @User
    @Test
    void shouldShowErrorIfRegistrationExistUser(UserJson user) {
        String password = "123";
        open(CFG.frontUrl(), MainPage.class)
                .getHeader()
                .login()
                .clickOnRegisterButton()
                .fillRegisterForm(user.username(), password, password)
                .checkErrorRegisterMessage(String.format("Username `%s` already exists", user.username()));
    }
}

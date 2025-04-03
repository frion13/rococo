package guru.qa.rococo.page;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideDriver;
import com.codeborne.selenide.SelenideElement;
import guru.qa.rococo.config.Config;

public abstract class BasePage<T extends BasePage<?>> {
    protected static final Config CFG = Config.getInstance();

    private final SelenideElement alert;

    protected BasePage(SelenideDriver driver) {
        this.alert = driver.$(".MuiSnackbar-root");
    }

    public BasePage() {
        this.alert = Selenide.$(".MuiSnackbar-root");
    }

    public abstract T checkThatPageLoaded();

}

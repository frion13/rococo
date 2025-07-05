package guru.qa.rococo.jupiter.annotation;

import guru.qa.rococo.jupiter.extension.ApiLoginExtension;
import guru.qa.rococo.jupiter.extension.BrowserExtension;
import guru.qa.rococo.jupiter.extension.UserExtension;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith({
        BrowserExtension.class,
        UserExtension.class,
        ApiLoginExtension.class,
        AllureJunit5.class
})
public @interface WebTest {
}

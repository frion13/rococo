package guru.qa.rococo.jupiter.extension;

import guru.qa.rococo.data.dao.AuthUserDaoSpringJdbc;
import guru.qa.rococo.data.entity.UserEntity;
import guru.qa.rococo.data.entity.auth.AuthUserEntity;
import guru.qa.rococo.data.entity.auth.Authority;
import guru.qa.rococo.data.entity.auth.AuthorityEntity;
import guru.qa.rococo.data.impl.AuthAuthorityDaoSpringJdbc;
import guru.qa.rococo.data.impl.UserdataUserDaoSpringJdbc;
import guru.qa.rococo.jupiter.annotation.User;
import guru.qa.rococo.model.UserJson;
import guru.qa.rococo.utils.RandomDataUtils;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.ArrayList;
import java.util.Arrays;

public class UserExtension implements BeforeEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(UserExtension.class);
    private static final String DEFAULT_PASSWORD = "123";

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), User.class)
                .ifPresent(userAnno -> {
                    String username = userAnno.username().isEmpty()
                            ? RandomDataUtils.randomUsername()
                            : userAnno.username();

                    String password = userAnno.password().isEmpty()
                            ? DEFAULT_PASSWORD
                            : userAnno.password();

                    AuthUserEntity authUser = fillAuthUserEntity(username, "{noop}" + password);
                    AuthUserDaoSpringJdbc authUserDao = new AuthUserDaoSpringJdbc();
                    AuthAuthorityDaoSpringJdbc authorityDao = new AuthAuthorityDaoSpringJdbc();

                    AuthUserEntity createdUser = authUserDao.createUser(authUser);
                    authUser.getAuthorities().forEach(a -> a.setUser(createdUser));
                    authorityDao.createAuthority(authUser.getAuthorities().toArray(new AuthorityEntity[0]));

                    UserEntity userProfile = new UserEntity();
                    userProfile.setUsername(username);
                    userProfile.setFirstname("QA");
                    userProfile.setLastname("Guru");

                    UserdataUserDaoSpringJdbc userdataDao = new UserdataUserDaoSpringJdbc();
                    UserEntity createdProfile = userdataDao.create(userProfile);

                    context.getStore(NAMESPACE).put("user", UserJson.fromEntity(createdProfile));

                    setUser(UserJson.fromEntity(createdProfile));
                });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(UserJson.class);
    }


    @Override
    public UserJson resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return createdUser();
    }

    private AuthUserEntity fillAuthUserEntity(String username, String password) {
        AuthUserEntity user = new AuthUserEntity();
        user.setUsername(username);
        user.setPassword(password);
        user.setEncodedPassword(password);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setAuthorities(new ArrayList<>(Arrays.stream(Authority.values())
                .map(a -> {
                    AuthorityEntity ae = new AuthorityEntity();
                    ae.setAuthority(a);
                    ae.setUser(user);
                    return ae;
                }).toList()));
        return user;
    }

    public static UserJson createdUser() {
        final ExtensionContext context = TestMethodContextExtension.context();
        return context.getStore(NAMESPACE).get(
                context.getUniqueId(),
                UserJson.class
        );
    }

    public static void setUser(UserJson testUser) {
        final ExtensionContext context = TestMethodContextExtension.context();
        context.getStore(NAMESPACE).put(
                context.getUniqueId(),
                testUser
        );
    }
}
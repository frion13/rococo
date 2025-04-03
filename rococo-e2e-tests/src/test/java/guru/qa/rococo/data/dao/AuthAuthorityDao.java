package guru.qa.rococo.data.dao;

import guru.qa.rococo.data.entity.auth.AuthorityEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface AuthAuthorityDao {

    void createAuthority(AuthorityEntity... authority);

}
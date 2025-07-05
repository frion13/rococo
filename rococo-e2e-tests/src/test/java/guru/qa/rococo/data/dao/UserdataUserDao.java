package guru.qa.rococo.data.dao;


import guru.qa.rococo.data.entity.UserEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface UserdataUserDao {

    UserEntity create(UserEntity user);
}
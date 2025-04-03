package guru.qa.rococo.service;

import com.google.protobuf.ByteString;
import guru.qa.grpc.rococo.grpc.RococoUserdataServiceGrpc;
import guru.qa.grpc.rococo.grpc.UpdateUserRequest;
import guru.qa.grpc.rococo.grpc.UserRequest;
import guru.qa.grpc.rococo.grpc.UserResponse;
import guru.qa.rococo.config.Config;
import guru.qa.rococo.model.UserJson;
import guru.qa.rococo.utils.AllureGrpcInterceptor;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qameta.allure.Step;
import lombok.NonNull;

public class UserdataGrpcClient {

    private final Config CFG = Config.getInstance();

    private final Channel channel = ManagedChannelBuilder
            .forAddress(CFG.userdataGrpcAddress(), CFG.userdataGrpcPort())
            .intercept(new AllureGrpcInterceptor())
            .usePlaintext()
            .build();

    private final RococoUserdataServiceGrpc.RococoUserdataServiceBlockingStub blockingStub
            = RococoUserdataServiceGrpc.newBlockingStub(channel);

    public UserJson getUser(String username) {
        try {
            final UserResponse response = blockingStub.getUser(
                    UserRequest.newBuilder()
                            .setUsername(username)
                            .build()
            );

            if (response == null) {
                return null;
            }

            return UserJson.fromGrpcResponse(response);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    @Step("Обновление информации пользователя - {userJson.username} по grpc")
    public UserJson updateUser(@NonNull UserJson userJson) {
        final UserResponse response = blockingStub.updateUser(UpdateUserRequest.newBuilder()
                .setUsername(userJson.username())
                .setFirstname(userJson.firstname())
                .setLastname(userJson.surname())
                .setAvatar(ByteString.copyFrom(userJson.avatar()))
                .build());
        return UserJson.fromGrpcResponse(response);
    }
}

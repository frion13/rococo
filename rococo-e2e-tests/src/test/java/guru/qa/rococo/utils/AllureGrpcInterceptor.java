package guru.qa.rococo.utils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.grpc.*;
import io.qameta.allure.Allure;
import io.qameta.allure.attachment.AttachmentData;
import io.qameta.allure.attachment.AttachmentProcessor;
import io.qameta.allure.attachment.DefaultAttachmentProcessor;
import io.qameta.allure.attachment.FreemarkerAttachmentRenderer;
import io.qameta.allure.attachment.http.HttpRequestAttachment;
import io.qameta.allure.attachment.http.HttpResponseAttachment;
import lombok.NonNull;

public class AllureGrpcInterceptor implements ClientInterceptor {

    private static final JsonFormat.Printer printer = JsonFormat.printer();

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            @NonNull Channel channel) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                channel.newCall(method, callOptions)) {

            @Override
            public void sendMessage(ReqT message) {
                try {
                    String jsonBody = printer.print((MessageOrBuilder) message);
                    logGrpcRequest(method.getFullMethodName(), jsonBody);
                } catch (InvalidProtocolBufferException e) {
                    Allure.addAttachment("gRPC Request Error",
                            "Failed to parse protobuf message: " + e.getMessage());
                }
                super.sendMessage(message);
            }

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                super.start(new ForwardingClientCallListener<RespT>() {
                    @Override
                    protected Listener<RespT> delegate() {
                        return responseListener;
                    }

                    @Override
                    public void onMessage(RespT message) {
                        try {
                            String jsonBody = printer.print((MessageOrBuilder) message);
                            logGrpcResponse(method.getFullMethodName(), jsonBody);
                        } catch (InvalidProtocolBufferException e) {
                            Allure.addAttachment("gRPC Response Error",
                                    "Failed to parse protobuf message: " + e.getMessage());
                        }
                        super.onMessage(message);
                    }
                }, headers);
            }
        };
    }

    private void logGrpcRequest(String methodName, String jsonBody) {
        final AttachmentProcessor<AttachmentData> processor = new DefaultAttachmentProcessor();

        final HttpRequestAttachment requestAttachment = HttpRequestAttachment.Builder
                .create("gRPC Request: " + methodName, "POST")
                .setHeader("Content-Type", "application/grpc+json")
                .setBody(jsonBody)
                .build();

        processor.addAttachment(
                requestAttachment,
                new FreemarkerAttachmentRenderer("grpc-request.ftl")
        );
    }

    private void logGrpcResponse(String methodName, String jsonBody) {
        final AttachmentProcessor<AttachmentData> processor = new DefaultAttachmentProcessor();

        final HttpResponseAttachment responseAttachment = HttpResponseAttachment.Builder
                .create("gRPC Response: " + methodName)
                .setResponseCode(200)
                .setHeader("Content-Type", "application/grpc+json")
                .setBody(jsonBody)
                .build();

        processor.addAttachment(
                responseAttachment,
                new FreemarkerAttachmentRenderer("grpc-response.ftl")
        );
    }
}

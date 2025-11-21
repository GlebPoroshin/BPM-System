package com.rut.glebporoshin.sop.grpc.onboarding;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.66.0)",
    comments = "Source: onboarding-service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class OnboardingServiceGrpc {

  private OnboardingServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "OnboardingService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingRequest,
      com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingResponse> getCreateOnboardingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateOnboarding",
      requestType = com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingRequest.class,
      responseType = com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingRequest,
      com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingResponse> getCreateOnboardingMethod() {
    io.grpc.MethodDescriptor<com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingRequest, com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingResponse> getCreateOnboardingMethod;
    if ((getCreateOnboardingMethod = OnboardingServiceGrpc.getCreateOnboardingMethod) == null) {
      synchronized (OnboardingServiceGrpc.class) {
        if ((getCreateOnboardingMethod = OnboardingServiceGrpc.getCreateOnboardingMethod) == null) {
          OnboardingServiceGrpc.getCreateOnboardingMethod = getCreateOnboardingMethod =
              io.grpc.MethodDescriptor.<com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingRequest, com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateOnboarding"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingResponse.getDefaultInstance()))
              .setSchemaDescriptor(new OnboardingServiceMethodDescriptorSupplier("CreateOnboarding"))
              .build();
        }
      }
    }
    return getCreateOnboardingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusRequest,
      com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusResponse> getGetOnboardingStatusMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetOnboardingStatus",
      requestType = com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusRequest.class,
      responseType = com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusRequest,
      com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusResponse> getGetOnboardingStatusMethod() {
    io.grpc.MethodDescriptor<com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusRequest, com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusResponse> getGetOnboardingStatusMethod;
    if ((getGetOnboardingStatusMethod = OnboardingServiceGrpc.getGetOnboardingStatusMethod) == null) {
      synchronized (OnboardingServiceGrpc.class) {
        if ((getGetOnboardingStatusMethod = OnboardingServiceGrpc.getGetOnboardingStatusMethod) == null) {
          OnboardingServiceGrpc.getGetOnboardingStatusMethod = getGetOnboardingStatusMethod =
              io.grpc.MethodDescriptor.<com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusRequest, com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetOnboardingStatus"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusResponse.getDefaultInstance()))
              .setSchemaDescriptor(new OnboardingServiceMethodDescriptorSupplier("GetOnboardingStatus"))
              .build();
        }
      }
    }
    return getGetOnboardingStatusMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskRequest,
      com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskResponse> getUpdateOnboardingTaskMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateOnboardingTask",
      requestType = com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskRequest.class,
      responseType = com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskRequest,
      com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskResponse> getUpdateOnboardingTaskMethod() {
    io.grpc.MethodDescriptor<com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskRequest, com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskResponse> getUpdateOnboardingTaskMethod;
    if ((getUpdateOnboardingTaskMethod = OnboardingServiceGrpc.getUpdateOnboardingTaskMethod) == null) {
      synchronized (OnboardingServiceGrpc.class) {
        if ((getUpdateOnboardingTaskMethod = OnboardingServiceGrpc.getUpdateOnboardingTaskMethod) == null) {
          OnboardingServiceGrpc.getUpdateOnboardingTaskMethod = getUpdateOnboardingTaskMethod =
              io.grpc.MethodDescriptor.<com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskRequest, com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateOnboardingTask"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskResponse.getDefaultInstance()))
              .setSchemaDescriptor(new OnboardingServiceMethodDescriptorSupplier("UpdateOnboardingTask"))
              .build();
        }
      }
    }
    return getUpdateOnboardingTaskMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static OnboardingServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OnboardingServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OnboardingServiceStub>() {
        @java.lang.Override
        public OnboardingServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OnboardingServiceStub(channel, callOptions);
        }
      };
    return OnboardingServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static OnboardingServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OnboardingServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OnboardingServiceBlockingStub>() {
        @java.lang.Override
        public OnboardingServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OnboardingServiceBlockingStub(channel, callOptions);
        }
      };
    return OnboardingServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static OnboardingServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OnboardingServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OnboardingServiceFutureStub>() {
        @java.lang.Override
        public OnboardingServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OnboardingServiceFutureStub(channel, callOptions);
        }
      };
    return OnboardingServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void createOnboarding(com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingRequest request,
        io.grpc.stub.StreamObserver<com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateOnboardingMethod(), responseObserver);
    }

    /**
     */
    default void getOnboardingStatus(com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusRequest request,
        io.grpc.stub.StreamObserver<com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetOnboardingStatusMethod(), responseObserver);
    }

    /**
     */
    default void updateOnboardingTask(com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskRequest request,
        io.grpc.stub.StreamObserver<com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateOnboardingTaskMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service OnboardingService.
   */
  public static abstract class OnboardingServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return OnboardingServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service OnboardingService.
   */
  public static final class OnboardingServiceStub
      extends io.grpc.stub.AbstractAsyncStub<OnboardingServiceStub> {
    private OnboardingServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OnboardingServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OnboardingServiceStub(channel, callOptions);
    }

    /**
     */
    public void createOnboarding(com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingRequest request,
        io.grpc.stub.StreamObserver<com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateOnboardingMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getOnboardingStatus(com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusRequest request,
        io.grpc.stub.StreamObserver<com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetOnboardingStatusMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateOnboardingTask(com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskRequest request,
        io.grpc.stub.StreamObserver<com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateOnboardingTaskMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service OnboardingService.
   */
  public static final class OnboardingServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<OnboardingServiceBlockingStub> {
    private OnboardingServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OnboardingServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OnboardingServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingResponse createOnboarding(com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateOnboardingMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusResponse getOnboardingStatus(com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetOnboardingStatusMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskResponse updateOnboardingTask(com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateOnboardingTaskMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service OnboardingService.
   */
  public static final class OnboardingServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<OnboardingServiceFutureStub> {
    private OnboardingServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OnboardingServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OnboardingServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingResponse> createOnboarding(
        com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateOnboardingMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusResponse> getOnboardingStatus(
        com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetOnboardingStatusMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskResponse> updateOnboardingTask(
        com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateOnboardingTaskMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CREATE_ONBOARDING = 0;
  private static final int METHODID_GET_ONBOARDING_STATUS = 1;
  private static final int METHODID_UPDATE_ONBOARDING_TASK = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CREATE_ONBOARDING:
          serviceImpl.createOnboarding((com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingRequest) request,
              (io.grpc.stub.StreamObserver<com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingResponse>) responseObserver);
          break;
        case METHODID_GET_ONBOARDING_STATUS:
          serviceImpl.getOnboardingStatus((com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusRequest) request,
              (io.grpc.stub.StreamObserver<com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusResponse>) responseObserver);
          break;
        case METHODID_UPDATE_ONBOARDING_TASK:
          serviceImpl.updateOnboardingTask((com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskRequest) request,
              (io.grpc.stub.StreamObserver<com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getCreateOnboardingMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingRequest,
              com.rut.glebporoshin.sop.grpc.onboarding.CreateOnboardingResponse>(
                service, METHODID_CREATE_ONBOARDING)))
        .addMethod(
          getGetOnboardingStatusMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusRequest,
              com.rut.glebporoshin.sop.grpc.onboarding.GetOnboardingStatusResponse>(
                service, METHODID_GET_ONBOARDING_STATUS)))
        .addMethod(
          getUpdateOnboardingTaskMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskRequest,
              com.rut.glebporoshin.sop.grpc.onboarding.UpdateOnboardingTaskResponse>(
                service, METHODID_UPDATE_ONBOARDING_TASK)))
        .build();
  }

  private static abstract class OnboardingServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    OnboardingServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.rut.glebporoshin.sop.grpc.onboarding.OnboardingServiceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("OnboardingService");
    }
  }

  private static final class OnboardingServiceFileDescriptorSupplier
      extends OnboardingServiceBaseDescriptorSupplier {
    OnboardingServiceFileDescriptorSupplier() {}
  }

  private static final class OnboardingServiceMethodDescriptorSupplier
      extends OnboardingServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    OnboardingServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (OnboardingServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new OnboardingServiceFileDescriptorSupplier())
              .addMethod(getCreateOnboardingMethod())
              .addMethod(getGetOnboardingStatusMethod())
              .addMethod(getUpdateOnboardingTaskMethod())
              .build();
        }
      }
    }
    return result;
  }
}

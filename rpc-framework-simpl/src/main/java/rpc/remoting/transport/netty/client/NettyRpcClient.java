package rpc.remoting.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import rpc.enums.CompressTypeEnum;
import rpc.enums.SerializationTypeEnum;
import rpc.enums.ServiceDiscoveryEnum;
import rpc.extension.ExtensionLoader;
import rpc.factory.SingletonFactory;
import rpc.registry.ServiceDiscovery;
import rpc.remoting.constants.RpcConstants;
import rpc.remoting.dto.RpcMessage;
import rpc.remoting.dto.RpcRequest;
import rpc.remoting.dto.RpcResponse;
import rpc.remoting.transport.RpcRequestTransport;
import rpc.remoting.transport.netty.coder.RpcMessageDecoder;
import rpc.remoting.transport.netty.coder.RpcMessageEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @ClassDescription:
 * 客户端中主要有一个用于向服务端发送消息的 sendMessage()方法，
 * 通过这个方法你可以将消息也就是RpcRequest 对象发送到服务端，
 * 并且你可以同步获取到服务端返回的结果也就是RpcResponse 对象
 * @Author: chensen
 * @Created: 2024/7/17 16:12
 */
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    // 初始化相关资源比如 EventLoopGroup, Bootstrap
    // Netty 客户端初始化
    public NettyRpcClient() {
        // initialize resources such as EventLoopGroup, Bootstrap
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //  The timeout period of the connection.
                //  设置连接超时时间为 5000 毫秒。
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // 设置通道初始化器，用于在每个新连接创建时配置通道管道。服务端的响应从这个channel走
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        // 如果在 5 秒内没有发送数据，将触发写空闲事件（用于心跳机制）
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageEncoder());
                        p.addLast(new RpcMessageDecoder());
                        // 客户端处理器，处理 RPC 响应和其他事件。
                        p.addLast(new NettyRpcClientHandler());
                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceDiscoveryEnum.NACOS.getName());
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    /**
     * @param inetSocketAddress:
     * @return Channel
     * @author xh
     * @description 主要功能是异步连接到指定的服务器地址，并返回连接后的 Channel 对象，之后他们在这个channel上通信
     * @date 2024/7/18 20:33
     */
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        // 为连接操作添加一个监听器，当连接操作完成（成功或失败）时会回调这个监听器。
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }

    /**
     * @param inetSocketAddress:
     * @return Channel
     * @author xh
     * @description 获取这个服务（用inetSocketAddress表示）的channel
     * @date 2024/7/18 20:37
     */
    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
    }


    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // build return value
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        // get server address
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        // get  server address related channel
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {
            // put unprocessed request
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.KRYO.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", rpcMessage);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }

        return resultFuture;
    }
}

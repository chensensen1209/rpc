package transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.remoting.dto.RpcRequest;
import rpc.remoting.dto.RpcResponse;
import rpc.remoting.transport.netty.coder.NettyKryoDecoder;
import rpc.remoting.transport.netty.coder.NettyKryoEncoder;
import rpc.serialize.kryo.KryoSerializer;

/**
 * @ClassDescription:
 * 客户端中主要有一个用于向服务端发送消息的 sendMessage()方法，
 * 通过这个方法你可以将消息也就是RpcRequest 对象发送到服务端，
 * 并且你可以同步获取到服务端返回的结果也就是RpcResponse 对象
 * @Author: chensen
 * @Created: 2024/7/17 16:12
 */
public class NettyClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private final String host;
    private final int port;
    private static final Bootstrap b;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // 初始化相关资源比如 EventLoopGroup, Bootstrap
    // Netty 客户端初始化
    static {
        //NioEventLoopGroup 是一个处理 I/O 操作的多线程事件循环组
        //每个 EventLoop 处理一个或多个 Channel 的 I/O 操作。
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        // Netty 中的一个辅助类 包括指定 EventLoopGroup、Channel 类型、处理器等。配置客户端 服务端用serverBootstrap
        b = new Bootstrap();
        //获取序列化器
        KryoSerializer kryoSerializer = new KryoSerializer();
        b.group(eventLoopGroup)
                //指定使用 NIO 传输的 SocketChannel 类
                .channel(NioSocketChannel.class)
                //设置一个日志处理器，用于记录所有的事件
                .handler(new LoggingHandler(LogLevel.INFO))
                // 连接的超时时间，超过这个时间还是建立不上的话则代表连接失败 设置连接的超时时间为 5000 毫秒
                //  如果 15 秒之内没有发送数据给服务端的话，就发送一次心跳请求
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                //用于在每个新连接的 Channel 上安装一组 ChannelHandler
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    // 初始化 ChannelPipeline
                    protected void initChannel(SocketChannel ch) {
                        /*
                         自定义序列化编解码器
                         */
                        // 添加自定义解码器，将字节数据解码为 RpcResponse 对象。
                        //解码来自服务短的响应，编码自身的请求
                        ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class));
                        // ByteBuf -> RpcRequest
                        ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));
                        //添加自定义的客户端处理器，用于处理业务逻辑。
                        ch.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }

    /**
     * 一个用于向服务端发送消息的方法
     * 通过这个方法可以将消息也就是RpcRequest 对象发送到服务端，
     * 并且你可以同步获取到服务端返回的结果也就是RpcResponse 对象
     *
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    public RpcResponse sendMessage(RpcRequest rpcRequest) {
        try {
            //首先初始化了一个 Bootstrap(静态方法做的) 通过 Bootstrap 对象连接服务端
            //sync() 方法会等待连接操作完成
            ChannelFuture f = b.connect(host, port).sync();
            logger.info("client connect  {}", host + ":" + port);
            //获取与服务器的连接通道 Channel ( host, port)
            Channel futureChannel = f.channel();
            logger.info("send message");
            if (futureChannel != null) {
                //将 RpcRequest 对象写入通道并刷新
                //addListener 加一个监听器，监听消息发送的结果
                futureChannel.writeAndFlush(rpcRequest).addListener(future -> {
                    if (future.isSuccess()) {
                        logger.info("client send message: [{}]", rpcRequest.toString());
                    } else {
                        logger.error("Send failed:", future.cause());
                    }
                });
                //阻塞等待 ，直到Channel关闭
                futureChannel.closeFuture().sync();
                // 将服务端返回的数据也就是RpcResponse对象取出
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                return futureChannel.attr(key).get();
            }
        } catch (InterruptedException e) {
            logger.error("occur exception when connect server:", e);
        }
        return null;
    }

    public static void main(String[] args) {
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName("interface")
                .methodName("hello").build();
        NettyClient nettyClient = new NettyClient("127.0.0.1", 8889);
        for (int i = 0; i < 3; i++) {
            nettyClient.sendMessage(rpcRequest);
        }
        RpcResponse rpcResponse = nettyClient.sendMessage(rpcRequest);
        System.out.println(rpcResponse.toString());
    }

}

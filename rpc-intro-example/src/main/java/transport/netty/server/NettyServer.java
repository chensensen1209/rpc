package transport.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import rpc.remoting.dto.RpcRequest;
import rpc.remoting.dto.RpcResponse;
import rpc.remoting.transport.netty.coder.NettyKryoDecoder;
import rpc.remoting.transport.netty.coder.NettyKryoEncoder;
import rpc.serialize.kryo.KryoSerializer;

/**
 * @ClassDescription: 开启了一个服务端用于接受客户端的请求并处理。
 * @Author: chensen
 * @Created: 2024/7/17 21:40
 */
@Slf4j
public class NettyServer {
    private final int port;

    private NettyServer(int port) {
        this.port = port;
    }
    /**
     * @param :
     * @return void
     * @author xh
     * @description 使用 Netty 创建并运行服务器的过程
     * @date 2024/7/17 22:08
     */

    private void run() {
        //bossGroup 主要用于处理连接的接收 通常只有一个线程。
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // workerGroup 主要用于处理已建立连接的 I/O 操作，例如读写操作 线程数通常较多。
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        KryoSerializer kryoSerializer = new KryoSerializer();
        try {
            //用于设置服务器
            // ServerBootstrap封装了服务器的基本配置和启动流程，使得开发者可以方便地创建和启动一个 Netty 服务器
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    //指定服务器通道类型 用于基于 NIO 的服务器端 Socket 通信
                    .channel(NioServerSocketChannel.class)
                    //设置通道选项。
                    //TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。
                    // TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁
                    // ，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    //为每个连接添加自定义处理器，包括序列化器和业务逻辑处理器。
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcRequest.class));
                            ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcResponse.class));
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    });
            //绑定端口并启动服务器。
            ChannelFuture f = b.bind(port).sync();
            //阻塞直到服务器通道关闭。
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server:", e);
        } finally {
            //关闭线程池，释放资源。
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new NettyServer(8889).run();
    }
}

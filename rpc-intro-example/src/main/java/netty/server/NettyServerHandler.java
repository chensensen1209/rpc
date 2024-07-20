package netty.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import rpc.remoting.dto.RpcRequest;
import rpc.remoting.dto.RpcResponse;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/17 21:50
 */
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    //atomicInteger 用于记录服务器接收到的消息次数
    private static final AtomicInteger atomicInteger = new AtomicInteger(1);

    /**
     * @param ctx:
     * @param msg:
     * @return void
     * @author xh
     * @description 用于接收客户端发送过来的消息并返回结果给客户端。当用户发送消息，就回到这里进行处理
     * @date 2024/7/17 21:52
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            RpcRequest rpcRequest = (RpcRequest) msg;

            log.info("server receive msg: [{}] ,times:[{}]", rpcRequest, atomicInteger.getAndIncrement());
            //构建器模式创建一个 RpcResponse 对象，并设置其 message 字段为 "message from server"
            RpcResponse messageFromServer = RpcResponse.builder().message("message from server").build();
            //在channel中发送 RpcResponse 消息，之后客户端在channel中读取
            ChannelFuture f = ctx.writeAndFlush(messageFromServer);
            f.addListener(ChannelFutureListener.CLOSE);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception",cause);
        ctx.close();
    }
}

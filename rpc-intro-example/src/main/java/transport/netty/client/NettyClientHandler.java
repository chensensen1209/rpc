package transport.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.remoting.dto.RpcResponse;

/**
 * @ClassDescription: 处理从服务器接收到的 RpcResponse 消息，并在处理完成后关闭通道。
 *                      如果在处理过程中遇到异常，则记录异常信息并关闭通道
 * ChannelHandler 处理服务端消息
 * @Author: chensen
 * @Created: 2024/7/17 18:21
 */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    /**
     * @param ctx: ChannelHandlerContext 提供了与 ChannelPipeline 交互的方法。
     * @param msg: 返回的response消息
     * @return void
     * @author xh
     * @description NettyClientHandler用于读取服务端发送过来的 RpcResponse 消息对象，
     * 并将 RpcResponse 消息对象保存到 AttributeMap 上，AttributeMap 可以看作是一个Channel的共享数据源。
     * @date 2024/7/17 18:24
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            RpcResponse rpcResponse = (RpcResponse) msg;
            log.info("client receive msg: [{}]", rpcResponse.toString());
            //声明AttributeKey
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
            //将服务端的返回结果保存到AttributeMap上,这是channel上的一个共享数据
            //// AttributeMap的key是AttributeKey，value是Attribute
            ctx.channel().attr(key).set(rpcResponse);
            /** 之后可以额这样的去读取
              *AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
             * return futureChannel.attr(key).get();
             */
            ctx.channel().close();
        } finally {
            //使用 finally 块确保不管是否发生异常，都会释放 msg，避免内存泄漏。
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client caught exception", cause);
        ctx.close();
    }
}

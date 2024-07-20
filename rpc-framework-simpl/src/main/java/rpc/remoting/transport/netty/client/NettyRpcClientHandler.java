package rpc.remoting.transport.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import rpc.enums.CompressTypeEnum;
import rpc.enums.SerializationTypeEnum;
import rpc.factory.SingletonFactory;
import rpc.remoting.constants.RpcConstants;
import rpc.remoting.dto.RpcMessage;
import rpc.remoting.dto.RpcResponse;

import java.net.InetSocketAddress;

/**
 * @ClassDescription: 处理从服务器接收到的 RpcResponse 消息，并在处理完成后关闭通道。
 * 如果在处理过程中遇到异常，则记录异常信息并关闭通道
 * ChannelHandler 处理服务端消息
 * @Author: chensen
 * @Created: 2024/7/17 18:21
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        // 不会循环调用吗？
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    /**
     * @param ctx: ChannelHandlerContext 提供了与 ChannelPipeline 交互的方法。
     * @param msg: 返回的response消息
     * @return void
     * @author xh
     * @description NettyClientHandler用于读取服务端发送过来的 RpcResponse 消息对象，
     * 并将 RpcResponse 消息对象保存到 AttributeMap 上，AttributeMap 可以看作是一个Channel的共享数据源。
     * 读取从服务端传回来的消息
     * @date 2024/7/17 18:24
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("client receive msg: [{}]", msg);
            //确保msg是rpc消息，格式要正确
            if (msg instanceof RpcMessage) {
                RpcMessage message = (RpcMessage) msg;
                byte messageType = message.getMessageType();
                if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
                    log.info("heart [{}]", message.getData());
                } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                    RpcResponse rpcResponse = (RpcResponse) message.getData();
                    //从未处理请求中删除该请求
                    unprocessedRequests.complete(rpcResponse);
                }
            }
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

    // Netty 心跳机制相关。保证客户端和服务端的连接不被断掉，避免重连。 否则30秒断链
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.KRYO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}

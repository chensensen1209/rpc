package rpc.remoting.transport.netty.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rpc.compress.Compress;
import rpc.enums.CompressTypeEnum;
import rpc.enums.SerializationTypeEnum;
import rpc.extension.ExtensionLoader;
import rpc.remoting.constants.RpcConstants;
import rpc.remoting.dto.RpcMessage;
import rpc.remoting.dto.RpcRequest;
import rpc.serialize.Serializer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassDescription:
 * 网络传输需要通过字节流来实现，
 * 可以看作是 Netty 提供的字节数据的容器，使用它会让我们更加方便地处理字节数据。
 *
 * @Author: chensen
 * @Created: 2024/7/17 16:16
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf out) throws Exception {
        try {
            // 写入魔数
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            // 写入版本号
            out.writeByte(RpcConstants.VERSION);
            // 留出4个字节空间用于写入完整消息长度，消息长度之后写，现在不读取
            out.writerIndex(out.writerIndex() + 4);
            // 写入消息类型
            byte messageType = rpcMessage.getMessageType();
            out.writeByte(messageType);
            // 写入序列化类型，消息发送方指定
            out.writeByte(rpcMessage.getCodec());
            // 写入压缩类型，框架自己定义是否压缩以及压缩类型，这里选择gzip压缩，可能现实中会有多种
            out.writeByte(CompressTypeEnum.GZIP.getCode());
            // 写入消息ID
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());
            // 初始化消息体
            byte[] bodyBytes = null;
            //魔数4 版本1 长度4 消息类型1（请求or响应or心跳信息其他） 序列化类型1（自带orKryo） 压缩1 请求id4
            //初始化的消息长度为16 byte
            int fullLength = RpcConstants.HEAD_LENGTH;
            // 如果消息不是心跳消息，计算消息体长度
            // 心跳消息是保持连接活跃、检测连接状态的一种特殊消息
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                // 获取序列化器
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                log.info("codec name: [{}] ", codecName);
                // 获取序列化器，并序列化消息体
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecName);
                bodyBytes = serializer.serialize(rpcMessage.getData());
                // 获取压缩器
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                        .getExtension(compressName);
                // 压缩消息体
                bodyBytes = compress.compress(bodyBytes);
                // 更新完整消息长度
                fullLength += bodyBytes.length;
            }
            // 写入消息体
            if (bodyBytes != null) {
                //out此时在第十七个字节上
                out.writeBytes(bodyBytes);
            }
            // 回写完整消息长度
            int writeIndex = out.writerIndex();
            out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            out.writeInt(fullLength);
            out.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("Encode request error!", e);
        }
    }
}

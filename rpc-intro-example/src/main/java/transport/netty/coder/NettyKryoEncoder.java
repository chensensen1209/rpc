package transport.netty.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import rpc.serialize.Serializer;

/**
 * @ClassDescription:
 * 网络传输需要通过字节流来实现，
 * 可以看作是 Netty 提供的字节数据的容器，使用它会让我们更加方便地处理字节数据。
 *
 * @Author: chensen
 * @Created: 2024/7/17 16:16
 */
@AllArgsConstructor
public class NettyKryoEncoder extends MessageToByteEncoder<Object> {
    private final Serializer serializer;
    private final Class<?> genericClass;

    /**
     * 将消息对象 o 编码为 ByteBuf 对象
     * 写入过程为，先将字节长度写入buf中，写入后开始写真正的内容，
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) {
        if (genericClass.isInstance(o)) {
            // 1. 将对象转换为byte
            byte[] body = serializer.serialize(o);
            // 2. 读取消息的长度
            int dataLength = body.length;
            // 3.写入消息对应的字节数组长度dataLength进入byteBuf, writerIndex会使得byteBuf长度增加4 ，指向下一个可以写入的位置
            byteBuf.writeInt(dataLength);
            //4.将字节数组写入 ByteBuf 对象中
            byteBuf.writeBytes(body);
        }
    }
}

package transport.netty.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rpc.serialize.Serializer;

import java.util.List;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/17 16:17
 */
@AllArgsConstructor
@Slf4j
public class NettyKryoDecoder extends ByteToMessageDecoder {

    private final Serializer serializer;
    private final Class<?> genericClass;
    //消息头长度，这部分不是消息体
    private static final int BODY_LENGTH = 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //如果这个消息是有效的，那么在之前消息编码过程中已经写入了四个字节的消息体长度，所以消息体长度必定大于的等于4
        if (in.readableBytes() >= BODY_LENGTH){
            //标记当前readIndex位置
            in.markReaderIndex();
            //读取消息的长度
            //从当前的位置读取 4 个字节的数据
            int dataLength = in.readInt();
            //消息无效，再次确认
            if (dataLength < 0 || in.readableBytes() < 0){
                log.error("data length or byteBuf readableBytes is not valid");
                return;
            }
            //5.如果可读字节数小于消息长度的话，说明是不完整的消息，重置readIndex
            if (in.readableBytes() < dataLength) {
                in.resetReaderIndex();
                return;
            }
            //序列化
            byte[] body = new byte[dataLength];
            in.readBytes(body);
            Object obj = serializer.deserialize(body, genericClass);
            out.add(obj);
            log.info("successful decode ByteBuf to Object");
        }

    }
}

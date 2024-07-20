package rpc.remoting.transport.netty.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rpc.compress.Compress;
import rpc.enums.CompressTypeEnum;
import rpc.enums.SerializationTypeEnum;
import rpc.extension.ExtensionLoader;
import rpc.remoting.constants.RpcConstants;
import rpc.remoting.dto.RpcMessage;
import rpc.remoting.dto.RpcRequest;
import rpc.remoting.dto.RpcResponse;
import rpc.serialize.Serializer;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/17 16:17
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    public RpcMessageDecoder() {
        // MAX_FRAME_LENGTH 如果超过这个长度，帧将被丢弃，以防止内存溢出
        // lengthFieldOffset: 表示长度字段相对于消息起始位置的偏移量 魔术+版本
        // lengthFieldLength: 长度字段的长度
        // lengthAdjustment: 表示在读取完长度字段后，消息的实际内容长度需要做的调整。
        //  自己算 (fullLength - 9)，所以 lengthAdjustment 是 -9
        // initialBytesToStrip: 初始字节的剥离数量。表示在解码时需要跳过的字节数
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 调用父类的 decode 方法进行初步解码
        Object decoded = super.decode(ctx, in);
        // 如果解码后的对象是 ByteBuf 类型
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            log.info("数据长度："+frame.readableBytes());
            // 如果 ByteBuf 中可读字节数大于等于消息的总长度
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    // 解码
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }

        }
        return decoded;
    }
    private Object decodeFrame(ByteBuf in) {
        // 检查魔数是否正确，不对抛异常
        checkMagicNumber(in);
        // 检查版本
        checkVersion(in);
        // 读取消息体长度
        int fullLength = in.readInt();
        // build RpcMessage object
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .messageType(messageType).build();
        //如果是心跳消息，返回ping 和 pong值
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);
            // 现在写入的还是压缩后的数据，进行解压，解压类型在compressType中
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                    .getExtension(compressName);
            //解压完成
            bs = compress.decompress(bs);
            // 反序列化
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.info("codec name: [{}] ", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                    .getExtension(codecName);
            // 如果发送的消息是请求消息，构建rpc请求
            if (messageType == RpcConstants.REQUEST_TYPE) {
                RpcRequest tmpValue = serializer.deserialize(bs, RpcRequest.class);
                rpcMessage.setData(tmpValue);
            } else {
                RpcResponse tmpValue = serializer.deserialize(bs, RpcResponse.class);
                rpcMessage.setData(tmpValue);
            }
        }
        return rpcMessage;

    }

    private void checkVersion(ByteBuf in) {
        // read the version and compare
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private void checkMagicNumber(ByteBuf in) {
        // read the first 4 bit, which is the magic number, and compare
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }
}

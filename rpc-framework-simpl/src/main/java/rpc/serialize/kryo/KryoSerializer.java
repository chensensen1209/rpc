package rpc.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import rpc.exception.SerializeException;
import rpc.remoting.dto.RpcRequest;
import rpc.remoting.dto.RpcResponse;
import rpc.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @ClassDescription: 使用kryo进行序列化，
 * @Author: chensen
 * @Created: 2024/7/17 14:22
 */
public class KryoSerializer implements Serializer {

    /**
     * @param null:
     * @return null
     * @author xh
     * @description 由于 Kryo 不是线程安全的。每个线程都应该有自己的 Kryo，Input 和 Output 实例。
     *       所以，使用 ThreadLocal 存放 Kryo 对象
     * @date 2024/7/17 22:20
     */

    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        return kryo;
    });

    @Override
    public byte[] serialize(Object object) {
        try {
            //收集序列化过程中产生的字节数据,
            //ByteArrayOutputStream,允许将字节数据写入一个可动态增长的字节数组中
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //使用 Kryo 库的 Output 类来包装 ByteArrayOutputStream。
            Output output = new Output(byteArrayOutputStream);
            //确保每个线程都有一个独立的 Kryo 实例，避免并发问题。
            Kryo kryo = kryoThreadLocal.get();
            //序列化对象
            kryo.writeObject(output,object);
            //从 ThreadLocal 中移除 Kryo 实例，
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (Exception e) {
            throw new SerializeException("序列化失败");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> tClass) {
        try {
            //将字节数组包装成输入流
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            Input input = new Input(byteArrayInputStream);
            Kryo kryo = kryoThreadLocal.get();
            Object object = kryo.readObject(input, tClass);
            kryoThreadLocal.remove();
            return tClass.cast(object);
        } catch (Exception e) {
            throw new SerializeException("反序列化失败");
        }
    }
}

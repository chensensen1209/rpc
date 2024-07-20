package rpc.serialize;

import rpc.extension.SPI;

/**
 * @ClassDescription: 序列化接口
 * @Author: chensen
 * @Created: 2024/7/17 14:29
 */
@SPI
public interface Serializer {
    /**
     * @param object:
     * @return byte
     * @author xh
     * @description 序列化对象为一个字节数组
     * @date 2024/7/17 14:30
     */
    byte[] serialize(Object object);
    /**
     * @param bytes:
     * @param tClass:
     * @return T
     * @author xh
     * @description 反序列化
     * @date 2024/7/17 14:31
     */

    <T> T deserialize(byte[] bytes,Class<T> tClass);

}

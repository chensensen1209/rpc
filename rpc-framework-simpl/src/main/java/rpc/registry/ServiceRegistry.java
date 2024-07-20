package rpc.registry;

import rpc.extension.SPI;

import java.net.InetSocketAddress;

/**
 * @ClassDescription: 服务注册接口
 * @Author: chensen
 * @Created: 2024/7/18 16:46
 */
@SPI
public interface ServiceRegistry {
    /**
     * @param rpcServiceName: 完整的服务名称，接口名称+群组+版本
     * @param inetSocketAddress: 远程服务地址
     * @return void
     * @author xh
     * @description 注册服务到服务中心
     * @date 2024/7/18 16:47
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}

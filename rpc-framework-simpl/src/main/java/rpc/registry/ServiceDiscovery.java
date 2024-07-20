package rpc.registry;

import rpc.extension.SPI;
import rpc.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * @ClassDescription: 发现服务
 * @Author: chensen
 * @Created: 2024/7/18 16:50
 */
@SPI
public interface ServiceDiscovery {
    /**
     * @return InetSocketAddress ： 返回远程服务的地址
     * @author xh
     * @description 寻找服务并返回服务地址
     * @date 2024/7/18 16:51
     */

    InetSocketAddress lookupService(RpcRequest rpcRequest);
}

package rpc.provider;

import rpc.config.RpcServiceConfig;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/18 20:43
 */
public interface ServiceProvider {
    /**
     * @param rpcServiceConfig rpc service related attributes
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * @param rpcServiceName rpc service name
     * @return service object
     */
    Object getService(String rpcServiceName);

    /**
     * @param rpcServiceConfig rpc service related attributes
     */
    void publishService(RpcServiceConfig rpcServiceConfig);

}

package rpc.provider.Impl;

import lombok.extern.slf4j.Slf4j;
import rpc.config.RpcServiceConfig;
import rpc.enums.RpcErrorMessageEnum;
import rpc.enums.ServiceRegistryEnum;
import rpc.exception.RpcException;
import rpc.extension.ExtensionLoader;
import rpc.provider.ServiceProvider;
import rpc.registry.ServiceRegistry;
import rpc.remoting.transport.netty.server.NettyRpcServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassDescription: 用于管理 RPC 服务的注册和获取。它使用 Zookeeper 作为服务注册中心
 * @Author: chensen
 * @Created: 2024/7/18 20:44
 */
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {
    /**
     * key: rpc service name(interface name + version + group)
     * value: service object
     */
    //键是 RPC 服务的名字（接口名 + 版本 + 组），值是服务对象,channel吗？
    private final Map<String, Object> serviceMap;
    // 一个 Set，存储已注册的服务名，防止重复注册。
    private final Set<String> registeredService;
    //服务注册器，用于将服务注册到 Zookeeper
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        // 使用 ExtensionLoader 加载 ServiceRegistry 的 Zookeeper 实现
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(ServiceRegistryEnum.ZK.getName());
    }

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
        log.info("ZK Add service: {} and interfaces:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (null == service) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    //调用 serviceRegistry 的 registerService 方法将服务注册到 Zookeeper。
    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}

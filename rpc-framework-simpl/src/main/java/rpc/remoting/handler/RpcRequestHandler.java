package rpc.remoting.handler;

import lombok.extern.slf4j.Slf4j;
import rpc.exception.RpcException;
import rpc.factory.SingletonFactory;
import rpc.provider.Impl.NacosServiceProviderImpl;
import rpc.provider.Impl.ZkServiceProviderImpl;
import rpc.provider.ServiceProvider;
import rpc.remoting.dto.RpcRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @ClassDescription: 这个类主要负责将 RPC 请求转换为本地方法调用，并返回调用结果，之后在包装会响应
 * @Author: chensen
 * @Created: 2024/7/18 21:00
 */
@Slf4j
public class RpcRequestHandler {
    private final ServiceProvider serviceProvider;

//    public RpcRequestHandler() {
//        // 它从 Zookeeper 注册中心获取服务
//        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
//    }

    public RpcRequestHandler() {
        // 它从 nacos 注册中心获取服务
        serviceProvider = SingletonFactory.getInstance(NacosServiceProviderImpl.class);
    }

    /**
     * @param rpcRequest:  请求
     * @return Object 返回的结果
     * @author xh
     * @description TODO
     * @date 2024/7/18 21:07
     */
    public Object handle(RpcRequest rpcRequest) {
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest, service);
    }
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            // 根据 RpcRequest 中的方法名和参数类型，使用反射获取服务对象中的方法
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            // 返回方法结果
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}

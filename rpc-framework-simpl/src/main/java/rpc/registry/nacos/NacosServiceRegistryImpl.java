package rpc.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import rpc.registry.ServiceRegistry;
import rpc.registry.nacos.utils.NacosUtil;

import java.net.InetSocketAddress;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/19 11:22
 */
@Slf4j
public class NacosServiceRegistryImpl implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        try {
            NacosUtil.registerService(rpcServiceName,inetSocketAddress);
        } catch (NacosException e) {
            log.error("注册失败:"+rpcServiceName);
            throw new RuntimeException(e);
        }
    }
}

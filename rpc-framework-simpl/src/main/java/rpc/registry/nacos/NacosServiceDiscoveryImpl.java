package rpc.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import rpc.enums.LoadBalanceEnum;
import rpc.enums.RpcErrorMessageEnum;
import rpc.exception.RpcException;
import rpc.extension.ExtensionLoader;
import rpc.extension.SPI;
import rpc.loadbalance.LoadBalance;
import rpc.registry.ServiceDiscovery;
import rpc.registry.nacos.utils.NacosUtil;
import rpc.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/19 10:49
 */
@Slf4j
public class NacosServiceDiscoveryImpl implements ServiceDiscovery {

    private final LoadBalance loadBalance;

    public NacosServiceDiscoveryImpl() {
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.LOADBALANCE.getName());
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        try {
            String rpcServiceName = rpcRequest.getRpcServiceName();
            NamingService nacosNamingService = NacosUtil.getNacosNamingService();
            List<Instance> serviceInstance = NacosUtil.getAllInstance(rpcServiceName);
            if (serviceInstance == null || serviceInstance.isEmpty()){
                throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
            }
            Instance instance = loadBalance.selectServiceInstance(serviceInstance, rpcRequest);
            return new InetSocketAddress(instance.getIp(),instance.getPort());
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

}

package rpc.registry.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import rpc.enums.LoadBalanceEnum;
import rpc.enums.RpcErrorMessageEnum;
import rpc.exception.RpcException;
import rpc.extension.ExtensionLoader;
import rpc.loadbalance.LoadBalance;
import rpc.registry.ServiceDiscovery;
import rpc.registry.zk.utils.CuratorUtils;
import rpc.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @ClassDescription:
 * 根据完整的服务名称便可以将对应的服务地址查出来，
 * 查出来的服务地址可能并不止一个，通过负载均衡策略选择服务地址
 * @Author: chensen
 * @Created: 2024/7/18 17:13
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl() {
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.LOADBALANCE.getName());
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        //获取服务名称
        String rpcServiceName = rpcRequest.getRpcServiceName();
        //获取zookeeper客户端
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        //获取服务地址列表
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if (serviceUrlList == null || serviceUrlList.size() == 0){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        //负载均衡进行选择地址
        String serviceAddress = loadBalance.selectServiceAddress(serviceUrlList,rpcRequest);
        log.info("Successfully found the service address:[{}]", serviceAddress);
        String[] socketAddressArray = serviceAddress.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host,port);
    }
}

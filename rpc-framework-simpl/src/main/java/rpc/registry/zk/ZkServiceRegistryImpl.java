package rpc.registry.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import rpc.registry.ServiceRegistry;
import rpc.registry.zk.utils.CuratorUtils;

import java.net.InetSocketAddress;

/**
 * @ClassDescription: 使用zookeeper完成服务注册
 * @Author: chensen
 * @Created: 2024/7/18 16:55
 */
@Slf4j
public class ZkServiceRegistryImpl implements ServiceRegistry {
    /**
     * @param rpcServiceName: 服务接口名，其实就是类名
     * @param inetSocketAddress: 远程服务地址
     * @return void
     * @author xh
     * @description TODO
     * @date 2024/7/18 16:59
     */
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        // CuratorUtils.getZkClient() 方法用于初始化并返回一个 CuratorFramework 对象，
        // 该对象是用于与 ZooKeeper 交互的客户端
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        // 调用 CuratorUtils.createPersistentNode 方法在 ZooKeeper 上创建一个持久节点
        CuratorUtils.createPersistentNode(zkClient,servicePath);
    }
}

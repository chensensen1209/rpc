package rpc.config;

import lombok.extern.slf4j.Slf4j;
import rpc.registry.nacos.utils.NacosUtil;
import rpc.registry.zk.utils.CuratorUtils;
import rpc.remoting.transport.netty.server.NettyRpcServer;
import rpc.utils.concurrent.threadpool.ThreadPoolFactoryUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @ClassDescription: 在 JVM 关闭时执行清理操作
 * @Author: chensen
 * @Created: 2024/7/18 20:56
 */
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearZkAll() {
        log.info("addShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
                // 调用 CuratorUtils 的 clearRegistry 方法，清理 Zookeeper 注册中心中的相关信息
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
            } catch (UnknownHostException ignored) {
            }
            // 关闭所有线程池
            ThreadPoolFactoryUtil.shutDownAllThreadPool();
        }));
    }

    public void clearNacosAll() {
        log.info("addShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
                // 调用 CuratorUtils 的 clearRegistry 方法，清理 Zookeeper 注册中心中的相关信息
                NacosUtil.clearRegistry();
            } catch (UnknownHostException ignored) {
            }
            // 关闭所有线程池
            ThreadPoolFactoryUtil.shutDownAllThreadPool();
        }));
    }
}

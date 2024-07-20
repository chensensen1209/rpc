package rpc.server;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import rpc.annotation.RpcScan;
import rpc.config.RpcServiceConfig;
import rpc.remoting.transport.netty.server.NettyRpcServer;
import rpc.server.serviceImpl.HelloServiceImpl2;
import rpc.service.HelloService;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/18 22:26
 */
@RpcScan(basePackage = {"rpc.server"})
public class NettyServerMain {
    public static void main(String[] args) {
        // Register service via annotation
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");
        // Register service manually
        HelloService helloService2 = new HelloServiceImpl2();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test2").version("version2").service(helloService2).build();
        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();
    }
}

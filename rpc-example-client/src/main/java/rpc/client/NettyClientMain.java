package rpc.client;

import rpc.controller.HelloController;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import rpc.annotation.RpcScan;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/18 22:09
 */
@RpcScan(basePackage = {"rpc"})
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
        HelloController helloController = (HelloController) applicationContext.getBean("helloController");
        helloController.test();
    }
}

package rpc.controller;

import org.springframework.stereotype.Component;
import rpc.annotation.RpcReference;
import rpc.service.Hello;
import rpc.service.HelloService;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/18 22:01
 */
@Component
public class HelloController {
    @RpcReference(version = "version1", group = "test1")
    private HelloService helloService;

    public void test() throws InterruptedException {
        String hello = this.helloService.hello(new Hello("111", "222"));
        //如需使用 assert 断言，需要在 VM options 添加参数：-ea
        assert "service.Hello description is 222".equals(hello);
        Thread.sleep(12000);
        for (int i = 0; i < 10; i++) {
            System.out.println(helloService.hello(new Hello("111", "222")));
        }
    }
}

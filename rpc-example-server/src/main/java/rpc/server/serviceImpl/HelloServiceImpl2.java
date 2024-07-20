package rpc.server.serviceImpl;

import rpc.service.Hello;
import lombok.extern.slf4j.Slf4j;
import rpc.service.HelloService;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/18 22:26
 */
@Slf4j
public class HelloServiceImpl2 implements HelloService {
    static {
        System.out.println("HelloServiceImpl2被创建");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl2收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl2返回: {}.", result);
        return result;
    }
}

package example;

import org.junit.jupiter.api.Test;
import rpc.proxy.example.service.SmsService;
import rpc.proxy.example.service.SmsServiceImpl;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/18 11:04
 */
public class example {
    @Test
    public void test(){
        SmsService smsService = (SmsService) JdkProxyFactory.getProxy(new SmsServiceImpl());
        smsService.send("java");
    }
}

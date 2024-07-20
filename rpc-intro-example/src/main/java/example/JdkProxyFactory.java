package example;

import java.lang.reflect.Proxy;
/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/18 11:01
 */
public class JdkProxyFactory {
    /**
     * @param target: 目标类 要代理的类
     * @return Object
     * @author xh
     * @description 获取代理类
     * @date 2024/7/18 11:15
     */

    public static Object getProxy(Object target) {
        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(), // 目标类的类加载
                target.getClass().getInterfaces(),  // 代理需要实现的接口，可指定多个
                new DebugInvocationHandler(target)   // 代理对象对应的自定义 InvocationHandler
        );
    }
}


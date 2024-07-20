package example;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/18 10:58
 */
public class DebugInvocationHandler implements InvocationHandler {
    /**
     * 代理类中的真实对象
     */
    private final Object target;

    //target代理类要代理的 SmsServiceImpl
    public DebugInvocationHandler(Object target) {
        this.target = target;
    }
    /**
     * 当你使用代理对象调用方法的时候实际会调用到这个方法
     * 动态代理对象调用原生方法的时候，最终实际上调用到的是 invoke() 方法，
     *      * 然后 invoke() 方法代替我们去调用了被代理对象的原生方法。
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("log:method:before"+ method.getName());
        Object result = method.invoke(target, args);
        System.out.println("log:method:after"+ method.getName());
        return result;

    }
}

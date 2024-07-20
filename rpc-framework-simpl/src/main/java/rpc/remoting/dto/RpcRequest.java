package rpc.remoting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @ClassDescription: :客户端请求实体类
 * 当你要调用远程方法的时候，你需要先传输一个 RpcRequest 给对方，
 * RpcRequest 里面包含了要调用的目标方法和类的名称、参数等数据。
 * @Author: chensen
 * @Created: 2024/7/17 14:41
 */

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RpcRequest implements Serializable {
    // 序列化号 serialVersionUID 属于版本控制的作用，被static修饰不会被真正的序列化
    private static final long serialVersionUID = 1905122041950251207L;
    //请求的id
    private String requestId;
    //请求的接口名称
    private String interfaceName;
    //请求的方法名
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    //服务版本 为后续不兼容升级提供可能
    private String version;
    // 处理一个接口有多个类实现的情况
    private String group;

    public String getRpcServiceName() {
        /**
         * class name : 服务接口名也就是类名比如：github.javaguide.HelloService。
         * version : 服务版本。主要是为后续不兼容升级提供可能
         * group :服务所在的组。主要用于处理一个接口有多个类实现的情况。
         * group使用后见md
         */
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }
}

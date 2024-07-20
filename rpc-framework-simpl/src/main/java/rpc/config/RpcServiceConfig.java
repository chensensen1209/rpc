package rpc.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassDescription: 配置和管理 RPC 服务的一些基本信息，包括服务版本、服务组以及目标服务对象
 * @Author: chensen
 * @Created: 2024/7/18 20:43
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcServiceConfig {
    /**
     * 表示服务的版本，默认为空字符串。版本号用于在同一接口有多个版本实现时进行区分
     */
    private String version = "";
    /**
     * 表示服务的组，默认为空字符串。组号用于在同一接口有多个实现类时进行区分。
     */
    private String group = "";

    /**
     * 目标服务对象，即具体的服务实现类。
     */
    private Object service;

    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    /**
     * @param :
     * @return String
     * @author xh
     * @description 该方法用于获取服务的接口名称。
     *
     * @date 2024/7/18 20:48
     */

    public String getServiceName() {
        // 表示获取第一个接口。假设每个服务实现类至少实现了一个接口。 返回接口的规范名称（包括包名）。 com.example.MyServicegroup1v1
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}

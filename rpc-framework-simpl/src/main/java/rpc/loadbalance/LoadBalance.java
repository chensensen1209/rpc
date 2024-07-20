package rpc.loadbalance;

import com.alibaba.nacos.api.naming.pojo.Instance;
import rpc.extension.SPI;
import rpc.remoting.dto.RpcRequest;

import java.util.List;

/**
 * @ClassDescription: 负载均衡策略接口
 * @Author: chensen
 * @Created: 2024/7/18 17:16
 */
@SPI
public interface LoadBalance {
    /**
     * @param serviceUrlList: 服务地址列表
     * @param rpcRequest:消息请求
     * @return String 服务地址
     * @author xh
     * @description TODO
     * @date 2024/7/18 17:16
     */

    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
    Instance selectServiceInstance(List<Instance> serviceInstanceList, RpcRequest rpcRequest);

}

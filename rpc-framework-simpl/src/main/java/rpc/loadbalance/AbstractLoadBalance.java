package rpc.loadbalance;

import com.alibaba.nacos.api.naming.pojo.Instance;
import rpc.remoting.dto.RpcRequest;
import rpc.utils.CollectionUtil;

import java.util.List;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/18 17:17
 */
public abstract class AbstractLoadBalance implements LoadBalance{
    @Override
    public Instance selectServiceInstance(List<Instance> serviceInstanceList, RpcRequest rpcRequest) {
        if (CollectionUtil.isEmpty(serviceInstanceList)){
            return null;
        }
        if (serviceInstanceList.size() == 1){
            return serviceInstanceList.get(0);
        }
        return doInstanceSelect(serviceInstanceList, rpcRequest);
    }

    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        if (CollectionUtil.isEmpty(serviceAddresses)) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);

    protected abstract Instance doInstanceSelect(List<Instance> serviceInstance, RpcRequest rpcRequest);
}

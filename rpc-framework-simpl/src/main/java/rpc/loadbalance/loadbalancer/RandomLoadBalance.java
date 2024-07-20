package rpc.loadbalance.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;
import rpc.loadbalance.AbstractLoadBalance;
import rpc.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/18 17:19
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
    @Override
    protected Instance doInstanceSelect(List<Instance> serviceAddresses, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }

}

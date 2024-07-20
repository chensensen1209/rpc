package rpc.remoting.transport;

import rpc.extension.SPI;
import rpc.remoting.dto.RpcRequest;

/**
 * @ClassDescription: 一个发送 RPC 请求的顶层接口
 * @Author: chensen
 * @Created: 2024/7/18 16:41
 */
@SPI
public interface RpcRequestTransport {
    Object sendRpcRequest(RpcRequest rpcRequest);
}

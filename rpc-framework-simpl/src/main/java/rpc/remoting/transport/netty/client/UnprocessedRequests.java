package rpc.remoting.transport.netty.client;

import rpc.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassDescription: 用于存放未被服务端处理的请求
 * @Author: chensen
 * todo 可以限制大小，防止内存溢出
 * @Created: 2024/7/18 19:03
 */
public class UnprocessedRequests {
    //使用concurrent hashmap保证并发线程的安全
    // completableFuture，可以更简洁地处理异步操作，并在异步操作完成后执行回调函数
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES
            = new ConcurrentHashMap<>();

    /**
     * @param requestId:
     * @param future:
     * @return void
     * @author xh
     * @description 在发起 RPC 请求时，调用 put 方法 将请求 ID 和 CompletableFuture 对象存入map中 UNPROCESSED_RESPONSE_FUTURES。
     * @date 2024/7/18 19:09
     */

    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        //请求的id，如何处理这个id
        UNPROCESSED_RESPONSE_FUTURES.put(requestId, future);
    }
    /**
     * @param rpcResponse:
     * @return void
     * @author xh
     * @description 当收到 RPC 响应时，调用 complete 方法，将响应数据传递给对应的 CompletableFuture 对象,从map中移除原来的请求
     * @date 2024/7/18 19:10
     */
    public void complete(RpcResponse<Object> rpcResponse) {
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
        if (null != future) {
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}

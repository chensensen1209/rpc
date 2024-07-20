package rpc.remoting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rpc.enums.RpcResponseCodeEnum;

import java.io.Serializable;

/**
 * @ClassDescription: 服务端相应类
 * @Author: chensen
 * @Created: 2024/7/17 14:42
 */

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 715745410605631233L;
    private String requestId;
    /**
     * response code
     */
    private Integer code;
    /**
     * response message
     * 关于响应结果的详细信息（如错误描述、成功消息等）
     */
    private String message;
    /**
     * response body
     * 用于存储实际的响应数据
     */
    private T data;

    /**
     * @param data:
     * @param requestId:
     * @return RpcResponse<T>
     * @author xh
     * @description 响应数据 data 和请求 ID requestId。该方法会创建一个 RpcResponse 实例，
     * 并将响应码和响应消息设置为成功状态，并设置请求 ID 和数据
     * @date 2024/7/18 16:37
     */

    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (null != data) {
            response.setData(data);
        }
        return response;
    }
    /**
     * @param rpcResponseCodeEnum:
     * @return RpcResponse<T>
     * @author xh
     * @description 当一个 RPC 请求执行失败时，可以使用 fail 方法返回失败响应，包括失败的状态码和错误消息
     * @date 2024/7/18 16:38
     */
    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }

}

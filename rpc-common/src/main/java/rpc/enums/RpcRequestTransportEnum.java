package rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum RpcRequestTransportEnum {

    NETTY("netty");

    private final String name;
}

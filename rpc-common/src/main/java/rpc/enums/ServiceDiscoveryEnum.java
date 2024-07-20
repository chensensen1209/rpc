package rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum ServiceDiscoveryEnum {

    ZK("zk"),
    NACOS("nacos");

    private final String name;
}

package rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum ServiceRegistryEnum {

    ZK("zk"),
    NACOS("nacos");

    private final String name;
}

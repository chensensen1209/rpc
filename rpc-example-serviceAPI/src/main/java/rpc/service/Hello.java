package rpc.service;

import lombok.*;

import java.io.Serializable;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/18 21:55
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class Hello implements Serializable {
    private String message;
    private String description;
}

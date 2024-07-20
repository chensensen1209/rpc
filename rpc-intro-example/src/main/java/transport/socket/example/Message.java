package transport.socket.example;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/17 15:55
 */
@AllArgsConstructor
@Data
public class Message implements Serializable {
    private String content;
}

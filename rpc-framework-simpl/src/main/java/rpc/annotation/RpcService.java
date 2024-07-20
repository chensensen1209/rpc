package rpc.annotation;

import java.lang.annotation.*;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/18 21:31
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
/**
    实现例子 @RpcService(version = "1.0", group = "example")
 */

public @interface RpcService {
    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";
}

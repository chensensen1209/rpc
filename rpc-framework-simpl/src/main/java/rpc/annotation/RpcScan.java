package rpc.annotation;

import org.springframework.context.annotation.Import;
import rpc.spring.CustomScannerRegistrar;

import java.lang.annotation.*;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/18 21:31
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegistrar.class)
@Documented
public @interface RpcScan {
    String[] basePackage();
}

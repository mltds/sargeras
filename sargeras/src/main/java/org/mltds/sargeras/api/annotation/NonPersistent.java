package org.mltds.sargeras.api.annotation;

import java.lang.annotation.*;

/**
 * 如果某个参数不希望被持久化，则可以使用这个注解标注不希望持久化的参数
 * 
 * @author sunyi.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface NonPersistent {
}
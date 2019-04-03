package org.mltds.sargeras.api.annotation;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 *
 * 默认会持久化所有参数，如果某个参数不想被持久化，可以使用 {@link NonPersistent}，用于注入到补偿方法的入参中
 *
 * @author sunyi.
 */
@Inherited
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SagaTx {

    /**
     * 补偿方法名（Compensate Method Name），补偿方法可以没有，但如果有则必须在当前类中，且没有其他同名方法。<br/>
     * 执行补偿方法时，框架会扫描之前执行时持久化的参数，如果发现有类型和名称都相同的参数，则注入到补偿方法的入参中。
     * 
     * @return 补偿方法名（Compensate Method Name）
     */
    String compensate() default "";

    // /**
    // * 是否需要持久化这个TX的参数
    // *
    // * @return true：持久化参数和结果，有一定的性能损耗
    // */
    // boolean paramPersistent() default true;
    //
    //
    // boolean resultPersistent() default true;

}

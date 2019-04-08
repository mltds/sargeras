package org.mltds.sargeras.api.annotation;

import java.lang.annotation.*;

/**
 * 声明这是一个 Saga Tx， 关于 Saga Tx 的定义请参考 README。<br/>
 *
 * 默认会持久化 {@link #compensate()} 方法所需要的参数，判断所需要的参数规则为，参数类型+参数名称完全一致。<br/>
 * 持久化入参的目的是执行补偿方法 {@link #compensate()} 时，提供所需要的信息。<br/>
 * 如果某个参数不想被持久化，可以使用 {@link NonPersistent} 标注在入参上。<br/>
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

}
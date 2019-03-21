package org.mltds.sargeras.api.annotation;

import java.lang.annotation.*;

/**
 * @author sunyi.
 */
@Inherited
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SagaTx {

    /**
     * 获取补偿方法名（Compensate Method Name），通过名称获取，所以补偿方法可以没有，但如果有则必须在当前类中，且没有其他同名方法。<br/>
     * 执行补偿方法时，如果 {@link #paramPersistent()} 为<code>true</code>,框架会将类型和形参名相同都相同的参数注入到补偿方法的参数中。
     * 
     * @return 补偿方法名（Compensate Method Name）
     */
    String compensate();

    /**
     * 是否需要持久化这个TX的参数
     *
     * @return true：持久化参数和结果，有一定的性能损耗
     */
    boolean paramPersistent() default true;


    boolean resultPersistent() default true;

}

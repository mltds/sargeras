package org.mltds.sargeras.api.annotation;

import java.lang.annotation.*;

import org.mltds.sargeras.api.listener.SagaListener;

import com.sun.istack.internal.NotNull;

import static org.mltds.sargeras.api.Saga.*;

/**
 * @author sunyi.
 */
@Inherited
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Saga {

    @NotNull
    String appName();

    @NotNull
    String bizName();

    Class<? extends SagaListener>[] listeners();

    int lockTimeout() default DEFAULT_BIZ_TIMEOUT;

    int bizTimeout() default DEFAULT_BIZ_TIMEOUT;

    String triggerInterval() default DEFAULT_TRIGGER_INTERVAL_STR;



}

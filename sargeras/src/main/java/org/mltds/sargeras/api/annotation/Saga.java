package org.mltds.sargeras.api.annotation;

import static org.mltds.sargeras.api.Saga.*;

import java.lang.annotation.*;

/**
 * @author sunyi.
 */
@Inherited
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Saga {

    String appName();

    String bizName();

    int lockTimeout() default DEFAULT_LOCK_TIMEOUT;

    int bizTimeout() default DEFAULT_BIZ_TIMEOUT;

    String triggerInterval() default DEFAULT_TRIGGER_INTERVAL_STR;

}

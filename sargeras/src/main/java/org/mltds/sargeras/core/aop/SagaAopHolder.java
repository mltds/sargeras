package org.mltds.sargeras.core.aop;

import org.mltds.sargeras.core.SagaContext;
import org.springframework.stereotype.Component;

/**
 * @author sunyi.
 */
@Component
public class SagaAopHolder {

    private ThreadLocal<SagaContext> context = new ThreadLocal<>();

    public SagaContext getContext() {
        return context.get();
    }

    public void setContext(SagaContext context) {
        this.context.set(context);
    }

    public void removeContext() {
        this.context.remove();
    }

}

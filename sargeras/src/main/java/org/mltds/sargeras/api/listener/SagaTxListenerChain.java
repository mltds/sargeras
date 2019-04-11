package org.mltds.sargeras.api.listener;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sunyi.
 */
public class SagaTxListenerChain implements SagaTxListener {

    private static final Logger logger = LoggerFactory.getLogger(SagaTxListenerChain.class);

    private final List<SagaTxListener> listeners;

    public SagaTxListenerChain(List<SagaTxListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void event(SagaTxEvent event) {

        if (CollectionUtils.isEmpty(listeners)) {
            return;
        }

        for (SagaTxListener listener : listeners) {
            try {
                listener.event(event);
            } catch (Exception e) {
                // 不抛出异常，希望其他 Listener 能够被通知到，并且不阻断外部逻辑。
                logger.error("SagaTxListener 处理事件时发生异常", e);
            }
        }
    }

}

package org.mltds.sargeras.spi.service.defaultimpl;

import java.util.List;

import org.mltds.sargeras.api.SagaContext;
import org.mltds.sargeras.api.listener.SagaListener;
import org.mltds.sargeras.api.SagaTx;
import org.mltds.sargeras.api.SagaTxStatus;
import org.mltds.sargeras.api.exception.SagaException;

public class ListenerChain implements SagaListener {

    private List<SagaListener> listenerList;

    ListenerChain(List<SagaListener> listenerList) {
        this.listenerList = listenerList;
    }

    @Override
    public void onStart(SagaContext context) {
        for (SagaListener l : listenerList) {
            try {
                l.onStart(context);
            } catch (Exception e) {
                this.onException(context, e);
            }
        }
    }

    @Override
    public void onRestart(SagaContext context) {
        for (SagaListener l : listenerList) {
            try {
                l.onRestart(context);
            } catch (Exception e) {
                this.onException(context, e);
            }
        }
    }

    @Override
    public void onExecuteSucc(SagaContext context) {
        for (SagaListener l : listenerList) {
            try {
                l.onExecuteSucc(context);
            } catch (Exception e) {
                this.onException(context, e);
            }
        }
    }

    @Override
    public void onCompensateSucc(SagaContext context) {
        for (SagaListener l : listenerList) {
            try {
                l.onCompensateSucc(context);
            } catch (Exception e) {
                this.onException(context, e);
            }
        }
    }

    @Override
    public void onExeFailToComp(SagaContext context) {
        for (SagaListener l : listenerList) {
            try {
                l.onExeFailToComp(context);
            } catch (Exception e) {
                this.onException(context, e);
            }
        }
    }

    @Override
    public void onComFailToFinal(SagaContext context) {
        for (SagaListener l : listenerList) {
            try {
                l.onComFailToFinal(context);
            } catch (Exception e) {
                this.onException(context, e);
            }
        }
    }

    @Override
    public void beforeExecute(SagaContext context, SagaTx tx) {
        for (SagaListener l : listenerList) {
            try {
                l.beforeExecute(context, tx);
            } catch (Exception e) {
                this.onException(context, e);
            }
        }
    }

    @Override
    public void afterExecute(SagaContext context, SagaTx tx, SagaTxStatus status) {
        for (SagaListener l : listenerList) {
            try {
                l.afterExecute(context, tx, status);
            } catch (Exception e) {
                this.onException(context, e);
            }
        }
    }

    @Override
    public void beforeCompensate(SagaContext context, SagaTx tx) {
        for (SagaListener l : listenerList) {
            try {
                l.beforeCompensate(context, tx);
            } catch (Exception e) {
                this.onException(context, e);
            }
        }
    }

    @Override
    public void afterCompensate(SagaContext context, SagaTx tx, SagaTxStatus status) {
        for (SagaListener l : listenerList) {
            try {
                l.afterCompensate(context, tx, status);
            } catch (Exception e) {
                this.onException(context, e);
            }
        }
    }

    @Override
    public void onOvertime(SagaContext context) {
        for (SagaListener l : listenerList) {
            try {
                l.onOvertime(context);
            } catch (Exception e) {
                this.onException(context, e);
            }
        }
    }

    @Override
    public void onException(SagaContext context, Throwable t) {
        for (SagaListener l : listenerList) {
            try {
                l.onException(context, t);
            } catch (Exception e) {
                throw new SagaException("SagaListener 处理异常事件时发生异常", e);
            }
        }
    }
}
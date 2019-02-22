package org.mltds.sargeras;

import org.mltds.sargeras.utils.Pair;

/**
 * @author sunyi 2019/2/20.
 */
public class SagaService {

    public static final String BIZ_PARAM = "BIZ_PARAM";

    Pair<SagaStatus, Object> runSaga(Saga saga, String bizId, String bizParam) {
        // Build Saga Context

        SagaContext context = new SagaContext(saga);
        context.setBizId(bizId);
        context.setBizParam(bizParam);
        context.setStatus(SagaStatus.EXECUTING);

        // Store Saga Context
        context.putPersistentCache(BIZ_PARAM, bizParam);


        // Run txs

        // Return Result
        context.getBizResult();


        return new Pair<>(SagaStatus.EXECUTE_OVER, new Object());

    }

}

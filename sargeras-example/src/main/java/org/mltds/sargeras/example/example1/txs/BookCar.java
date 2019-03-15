package org.mltds.sargeras.example.example1.txs;

import org.mltds.sargeras.worker.api.SagaContext;
import org.mltds.sargeras.worker.api.SagaTx;
import org.mltds.sargeras.common.core.SagaTxStatus;

public class BookCar implements SagaTx {

    public static final String CAR_ORDER_NO = "CAR_ORDER_NO";

    @Override
    public SagaTxStatus execute(SagaContext context) {
        // 预约汽车成功，对方给了一个汽车预约单号
        String bookCarOrderNo = "car123";
        context.saveInfo(CAR_ORDER_NO, bookCarOrderNo);
        return SagaTxStatus.SUCCESS;
    }

    @Override
    public SagaTxStatus compensate(SagaContext context) {
        String bookCarOrderNo = context.getPersistentInfo(CAR_ORDER_NO, String.class);
        // 用这个 bookCarOrderNo 取消预定
        return SagaTxStatus.SUCCESS;
    }
}
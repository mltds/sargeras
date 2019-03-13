package org.mltds.sargeras.test.example.example1;

import org.mltds.sargeras.api.SagaContext;
import org.mltds.sargeras.api.SagaTx;
import org.mltds.sargeras.api.SagaTxStatus;

public class BookCar implements SagaTx {

    public static final String CAR_ORDER_NO = "CAR_ORDER_NO";

    @Override
    public SagaTxStatus execute(SagaContext context) {
        // 预约汽车成功，对方给了一个汽车预约单号
        String bookCarOrderNo = "car123";
        context.savePersistentInfo(CAR_ORDER_NO, bookCarOrderNo);
        return SagaTxStatus.SUCCESS;
    }

    @Override
    public SagaTxStatus compensate(SagaContext context) {
        String bookCarOrderNo = context.getPersistentInfo(CAR_ORDER_NO, String.class);
        // 用这个 bookCarOrderNo 取消预定
        return SagaTxStatus.SUCCESS;
    }
}
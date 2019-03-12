package org.mltds.sargeras.test.example;

import org.mltds.sargeras.api.SagaContext;
import org.mltds.sargeras.api.SagaTx;
import org.mltds.sargeras.api.SagaTxStatus;

public class BookAir implements SagaTx {
    public static final String AIR_ORDER_NO = "AIR_ORDER_NO";

    @Override
    public SagaTxStatus execute(SagaContext context) {
        // 预约飞机成功，对方返回一个飞机预约单号
        String bookAirOrderNo = "air789";
        context.savePersistentInfo(AIR_ORDER_NO, bookAirOrderNo);
        return SagaTxStatus.SUCCESS;
    }

    @Override
    public SagaTxStatus compensate(SagaContext context) {
        String bookAirOrderNo = context.getPersistentInfo(AIR_ORDER_NO, String.class);
        // 用这个 bookAirOrderNo 取消预定
        return SagaTxStatus.SUCCESS;
    }
}
package org.mltds.sargeras.example.example1.txs;

import org.mltds.sargeras.api.SagaContext;
import org.mltds.sargeras.api.SagaTx;
import org.mltds.sargeras.common.core.SagaTxStatus;

public class BookAir implements SagaTx {
    public static final String AIR_ORDER_NO = "AIR_ORDER_NO";

    @Override
    public SagaTxStatus execute(SagaContext context) {
        // 预约飞机成功，对方返回一个飞机预约单号
        String bookAirOrderNo = "air789";
        context.saveInfo(AIR_ORDER_NO, bookAirOrderNo);
        return SagaTxStatus.SUCCESS;
    }

    @Override
    public SagaTxStatus compensate(SagaContext context) {
        String bookAirOrderNo = context.getInfo(AIR_ORDER_NO, String.class);
        // 用这个 bookAirOrderNo 取消预定
        return SagaTxStatus.SUCCESS;
    }
}
package org.mltds.sargeras.test.example;

import java.util.HashMap;
import java.util.Map;

import org.mltds.sargeras.api.SagaContext;
import org.mltds.sargeras.api.SagaTx;
import org.mltds.sargeras.api.SagaTxStatus;

/**
 * @author sunyi.
 */
public class NotifyFamily implements SagaTx {
    @Override
    public SagaTxStatus execute(SagaContext context) {

        String travelDestination = context.getBizId();
        FamilyMember member = context.getBizParam(FamilyMember.class);

        String carOrderNo = context.getPersistentInfo(BookCar.CAR_ORDER_NO, String.class);
        String airOrderNo = context.getPersistentInfo(BookAir.AIR_ORDER_NO, String.class);
        String hotelOrderNo = context.getPersistentInfo(BookHotel.HOTEL_ORDER_NO, String.class);

        Map<String, Object> bookInfo = new HashMap<>();

        bookInfo.put("BizId", travelDestination);
        bookInfo.put("Member", member);
        bookInfo.put("CarOrderNo", carOrderNo);
        bookInfo.put("AirOrderNo", airOrderNo);
        bookInfo.put("HotelOrderNo", hotelOrderNo);

        // 将所有的预定信息汇总起来，返回结果（告诉家里人）
        context.setBizResult(bookInfo);

        return SagaTxStatus.SUCCESS;
    }

    @Override
    public SagaTxStatus compensate(SagaContext context) {
        context.setBizResult("Hi, guys, 你去不了了，咱们去开黑吧！");
        return SagaTxStatus.SUCCESS;
    }
}

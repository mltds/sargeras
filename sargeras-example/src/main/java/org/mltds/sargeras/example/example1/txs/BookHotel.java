package org.mltds.sargeras.example.example1.txs;

import org.mltds.sargeras.worker.api.SagaContext;
import org.mltds.sargeras.worker.api.SagaTx;
import org.mltds.sargeras.common.core.SagaTxStatus;
import org.mltds.sargeras.example.example1.FamilyMember;
import org.mltds.sargeras.example.example1.Result;

public class BookHotel implements SagaTx {
    public static final String HOTEL_ORDER_NO = "HOTEL_ORDER_NO";

    private boolean bookSucc;

    public BookHotel() {
        this(true);
    }

    public BookHotel(boolean bookSucc) {
        this.bookSucc = bookSucc;
    }

    @Override
    public SagaTxStatus execute(SagaContext context) {

        // 获取家人信息，用于预定酒店
        FamilyMember member = context.getBizParam(FamilyMember.class);

        if (bookSucc) {
            // 预约酒店成功，对方返回一个酒店预约单号
            String bookHotelOrderNo = "hotel456";
            context.saveInfo(HOTEL_ORDER_NO, bookHotelOrderNo);
            return SagaTxStatus.SUCCESS;
        } else {

            Result result = new Result();
            result.success = false;
            result.errorMessage = "预定酒店失败，没有房间了，别去了~~~";

            context.saveBizResult(result);
            return SagaTxStatus.FAILURE;
        }

    }

    @Override
    public SagaTxStatus compensate(SagaContext context) {
        String bookHotelOrderNo = context.getPersistentInfo(HOTEL_ORDER_NO, String.class);
        // 用这个 bookHotelOrderNo 取消预定
        return SagaTxStatus.SUCCESS;
    }
}
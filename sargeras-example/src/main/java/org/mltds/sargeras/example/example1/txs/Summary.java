package org.mltds.sargeras.example.example1.txs;

import org.mltds.sargeras.api.SagaContext;
import org.mltds.sargeras.api.SagaTx;
import org.mltds.sargeras.api.SagaTxStatus;
import org.mltds.sargeras.example.example1.FamilyMember;
import org.mltds.sargeras.example.example1.BookResult;

/**
 * @author sunyi.
 */
public class Summary implements SagaTx {
    @Override
    public SagaTxStatus execute(SagaContext context) {

        String bizId = context.getBizId();
        FamilyMember member = context.getBizParam(FamilyMember.class);
        String carOrderNo = context.getInfo(BookCar.CAR_ORDER_NO, String.class);
        String airOrderNo = context.getInfo(BookAir.AIR_ORDER_NO, String.class);
        String hotelOrderNo = context.getInfo(BookHotel.HOTEL_ORDER_NO, String.class);

        BookResult bookResult = new BookResult();

        bookResult.success = true;

        bookResult.bizId = bizId;
        bookResult.member = member;

        bookResult.carOrderNo = carOrderNo;
        bookResult.aireOrderNo = airOrderNo;
        bookResult.hotelOrderNo = hotelOrderNo;

        // 将所有的预定信息汇总起来，返回结果
        context.saveBizResult(bookResult);

        return SagaTxStatus.SUCCESS;
    }

    @Override
    public SagaTxStatus compensate(SagaContext context) {
        return SagaTxStatus.SUCCESS;
    }
}

package org.mltds.sargeras.example.example1.txs;

import org.mltds.sargeras.api.SagaContext;
import org.mltds.sargeras.api.SagaTx;
import org.mltds.sargeras.api.SagaTxStatus;
import org.mltds.sargeras.example.example1.FamilyMember;
import org.mltds.sargeras.example.example1.Result;

/**
 * @author sunyi.
 */
public class Summary implements SagaTx {
    @Override
    public SagaTxStatus execute(SagaContext context) {

        String bizId = context.getBizId();
        FamilyMember member = context.getBizParam(FamilyMember.class);
        String carOrderNo = context.loadInfo(BookCar.CAR_ORDER_NO, String.class);
        String airOrderNo = context.loadInfo(BookAir.AIR_ORDER_NO, String.class);
        String hotelOrderNo = context.loadInfo(BookHotel.HOTEL_ORDER_NO, String.class);

        Result result = new Result();

        result.success = true;

        result.bizId = bizId;
        result.member = member;

        result.carOrderNo = carOrderNo;
        result.aireOrderNo = airOrderNo;
        result.hotelOrderNo = hotelOrderNo;

        // 将所有的预定信息汇总起来，返回结果
        context.saveBizResult(result);

        return SagaTxStatus.SUCCESS;
    }

    @Override
    public SagaTxStatus compensate(SagaContext context) {
        return SagaTxStatus.SUCCESS;
    }
}

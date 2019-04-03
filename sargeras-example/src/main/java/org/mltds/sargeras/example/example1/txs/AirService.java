package org.mltds.sargeras.example.example1.txs;

import org.mltds.sargeras.api.annotation.SagaTx;
import org.springframework.stereotype.Service;

@Service
public class AirService {
    public static final String AIR_ORDER_NO = "AIR_ORDER_NO";

    @SagaTx(compensate = "compensate")
    public String book(String bizId) {
        // 预约飞机成功，对方返回一个飞机预约单号
        String bookAirOrderNo = "air789";
        return bookAirOrderNo;
    }

    public void compensate(String bizId) {
        // 用这个 bizId 取消预定
    }
}
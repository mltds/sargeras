package org.mltds.sargeras.example.example1.txs;

import org.mltds.sargeras.api.annotation.SagaTx;
import org.springframework.stereotype.Service;

@Service
public class CarService {

    @SagaTx(compensate = "compensate")
    public String book(String bizId) {
        // 预约汽车成功，对方给了一个汽车预约单号
        String bookCarOrderNo = "car123";
        return bookCarOrderNo;
    }

    public void compensate(String bizId) {
        // 用这个 bizId 取消预定
    }
}
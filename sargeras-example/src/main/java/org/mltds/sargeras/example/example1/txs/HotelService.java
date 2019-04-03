package org.mltds.sargeras.example.example1.txs;

import org.mltds.sargeras.api.annotation.SagaTx;
import org.mltds.sargeras.api.exception.expectation.SagaTxFailureException;
import org.mltds.sargeras.example.example1.FamilyMember;
import org.springframework.stereotype.Service;

@Service
public class HotelService {

    private boolean bookSucc;

    public HotelService() {
        this(true);
    }

    public HotelService(boolean bookSucc) {
        this.bookSucc = bookSucc;
    }

    @SagaTx(compensate = "compensate")
    public String book(String bizId, FamilyMember member) {

        if (bookSucc) {
            // 预约酒店成功，对方返回一个酒店预约单号
            String bookHotelOrderNo = "hotel456";
           return bookHotelOrderNo;
        } else {
            throw new SagaTxFailureException("预定酒店失败");
        }

    }

    public void compensate(String bizId, FamilyMember member) {
    }
}
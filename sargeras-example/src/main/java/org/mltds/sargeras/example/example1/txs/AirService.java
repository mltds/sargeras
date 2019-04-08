package org.mltds.sargeras.example.example1.txs;

import org.mltds.sargeras.api.annotation.SagaTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AirService {

    private static final Logger logger = LoggerFactory.getLogger(AirService.class);

    @SagaTx(compensate = "compensate")
    public String book(String bizId) {
        // 预约飞机成功，对方返回一个飞机预约单号
        String bookAirOrderNo = "air465";

        logger.info(bizId + "预定机票成功。");

        return bookAirOrderNo;
    }

    public void compensate(String bizId) {
        // 用这个 bizId 取消预定

        logger.info(bizId + "机票退票成功。");

    }
}
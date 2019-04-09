package org.mltds.sargeras.example.example1.txs;

import org.apache.commons.lang3.RandomUtils;
import org.mltds.sargeras.api.annotation.SagaTx;
import org.mltds.sargeras.api.exception.expectation.SagaTxFailureException;
import org.mltds.sargeras.api.exception.expectation.SagaTxProcessingException;
import org.mltds.sargeras.example.example1.FamilyMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HotelService {

    private static final Logger logger = LoggerFactory.getLogger(HotelService.class);

    @SagaTx(compensate = "compensate")
    public String book(String bizId, FamilyMember member) {

        int bookStatus = RandomUtils.nextInt(0, 3);

        // int bookStatus = 2;

        if (bookStatus == 0) {
            // 预约酒店成功，对方返回一个酒店预约单号
            String bookHotelOrderNo = "hotel789";

            logger.info(bizId + "预定酒店成功。");

            return bookHotelOrderNo;
        } else if (bookStatus == 1) {
            logger.warn(bizId + "预定酒店处理中");
            throw new SagaTxProcessingException("预定酒店处理中");
        } else {
            logger.warn(bizId + "预定酒店失败！");
            throw new SagaTxFailureException("预定酒店失败！");
        }

    }

    public void compensate(String bizId, FamilyMember member) {

        logger.warn(bizId + "取消酒店的预定。");

    }
}
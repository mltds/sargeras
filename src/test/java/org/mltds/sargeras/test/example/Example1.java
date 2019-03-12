package org.mltds.sargeras.test.example;

import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.mltds.sargeras.api.*;
import org.mltds.sargeras.listener.LogListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * 一个正向执行的例子<br/>
 * 比如出门旅游，有四个TX：订汽车 -> 订机票 -> 订酒店 -> 告诉家人。
 * 
 * @author sunyi
 */
public class Example1 {

    public static final Logger logger = LoggerFactory.getLogger(Example1.class);

    @Test
    public void test() {

        Saga saga = SagaBuilder.newBuilder("Travel", "LongTrip")// 定义一个业务
                .addTx(new BookCar())// 订汽车
                .addTx(new BookAir())// 订机票
                .addTx(new BookHotel(false))// 订酒店
                .addTx(new NotifyFamily()) // 告诉家人
                .addListener(new LogListener()).build();
        SagaLauncher.launch();

        String bizId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        FamilyMember member = new FamilyMember();
        member.id = "123456789012345678";
        member.name = "小乌龟";
        member.tel = "13100000000";
        member.travelDestination = "Croatia Plitvice Lakes National Park";

        SagaResult result = saga.start(bizId, member);

        SagaStatus status = result.getStatus();
        if (SagaStatus.EXECUTE_SUCC.equals(status)) {
            Map bizResult = result.getBizResult(Map.class);
            logger.info("预定成功，相关信息为：" + JSON.toJSONString(bizResult, true));
        } else if (SagaStatus.COMPENSATE_SUCC.equals(status) || SagaStatus.COMPENSATE_FAIL.equals(status)) {
            String bizResult = result.getBizResult(String.class);
            logger.info("预定失败:" + bizResult);
        } else {
            logger.info("预定中，请稍后");

        }

    }

}

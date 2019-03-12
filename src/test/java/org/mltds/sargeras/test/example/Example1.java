package org.mltds.sargeras.test.example;

import java.util.UUID;

import org.junit.Test;
import org.mltds.sargeras.api.Saga;
import org.mltds.sargeras.api.SagaBuilder;
import org.mltds.sargeras.api.SagaLauncher;
import org.mltds.sargeras.api.SagaStatus;
import org.mltds.sargeras.listener.LogListener;
import org.mltds.sargeras.utils.Pair;
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
                .addTx(new BookHotel())// 订酒店
                .addTx(new NotifyFamily()) // 告诉家人
                .addListener(new LogListener()).build();
        SagaLauncher.launch();

        String bizId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        FamilyMember member = new FamilyMember();
        member.id = "123456789012345678";
        member.name = "小乌龟";
        member.tel = "13100000000";
        member.travelDestination = "Croatia Plitvice Lakes National Park";

        Pair<SagaStatus, Object> result = saga.start(bizId, member);

        SagaStatus status = result.getA();
        if (SagaStatus.EXECUTE_SUCC.equals(status)) {
            Object resultB = result.getB();
            logger.info("预定成功，相关信息为：" + JSON.toJSONString(resultB, true));
        } else if (SagaStatus.COMPENSATE_SUCC.equals(status) || SagaStatus.COMPENSATE_FAIL.equals(status)) {
            Object resultB = result.getB();
            logger.info("预定失败，很遗憾" + JSON.toJSONString(resultB, true));
        } else {
            logger.info("预定中，请稍后");

        }

    }

}

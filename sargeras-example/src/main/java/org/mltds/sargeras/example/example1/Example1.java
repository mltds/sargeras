package org.mltds.sargeras.example.example1;

import java.util.UUID;

import org.mltds.sargeras.api.*;
import org.mltds.sargeras.api.listener.LogListener;
import org.mltds.sargeras.example.example1.txs.BookAir;
import org.mltds.sargeras.example.example1.txs.BookCar;
import org.mltds.sargeras.example.example1.txs.BookHotel;
import org.mltds.sargeras.example.example1.txs.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * 一个正向执行/逆向补偿的例子<br/>
 * 比如家人要去旅游，帮家里人订票的例子，有四个TX：订汽车 -> 订机票 -> 订酒店 -> 汇总结果。<br/>
 * PS：可以通过修改 {@link BookHotel} 的构造方法入参，来控制流程的成功失败。
 *
 * @author sunyi
 */
public class Example1 {

    public static final Logger logger = LoggerFactory.getLogger(Example1.class);

    public static final String appName = "Travel";
    public static final String bizName = "LongTrip";

    public static void main(String[] args) {

        init();

        book();

        Runtime.getRuntime().exit(0);
    }

    public static void init() {

        SagaBuilder.newBuilder(appName, bizName)// 定义一个业务
                .addTx(new BookCar())// 订汽车
                .addTx(new BookAir())// 订机票
                .addTx(new BookHotel(true))// 订酒店，false为强制失败
                .addTx(new Summary()) // 汇总结果
                .addListener(new LogListener()) // 增加一些log输出方便跟踪
                .build();

        SagaLauncher.launch(); // 需要先 Build Saga

    }

    public static void book() {

        // 业务订单ID，唯一且必须先生成。
        String bizId = UUID.randomUUID().toString().replace("-", "").toUpperCase();

        // 家人信息
        FamilyMember member = new FamilyMember();
        member.id = "123456789012345678";
        member.name = "小乌龟";
        member.tel = "13100000000";
        member.travelDestination = "Croatia Plitvice Lakes National Park";

        // 获取业务流程（模板）
        Saga saga = SagaApplication.getSaga(appName, bizName); // 任何地方都可以获取到这个Saga

        // 执行业务
        SagaResult sagaResult = saga.start(bizId, member);
        BookResult bookResult = sagaResult.getBizResult(BookResult.class);

        logger.info(JSON.toJSONString(bookResult, true));



    }

    public static void testTimeConsuming() {
        // 测试耗时情况
        int count = 100;
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            book();
        }
        long t2 = System.currentTimeMillis();
        logger.info("运行{}次，总计耗时{}毫秒，平均耗时{}毫秒。", count, t2 - t1, (t2 - t1) / count);

    }

    public static void testPollRetry() throws InterruptedException {

        Thread.sleep(1000 * 60 * 60);

    }

}

package org.mltds.sargeras.example.example1;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mltds.sargeras.example.example1.txs.TravelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSON;

/**
 * @author sunyi.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:example1.xml")
public class Example1 {

    private static final Logger logger = LoggerFactory.getLogger(Example1.class);

    @Autowired
    private TravelService travelService;

    @Test
    public void test() {

        // 业务订单ID，唯一且必须先生成。
        String bizId = UUID.randomUUID().toString().replace("-", "").toUpperCase();

        // 家人信息
        FamilyMember member = new FamilyMember();
        member.id = "123456789012345678";
        member.name = "小乌龟";
        member.tel = "13100000000";
        member.travelDestination = "Croatia Plitvice Lakes National Park";

        BookResult bookResult = travelService.travel(bizId, member);

        logger.info(JSON.toJSONString(bookResult, true));

    }

}

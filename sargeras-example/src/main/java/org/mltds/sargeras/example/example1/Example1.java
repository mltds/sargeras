package org.mltds.sargeras.example.example1;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author sunyi.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:example1.xml")
public class Example1 {

    @Test
    public void test() {

        System.out.println("test");

    }

}

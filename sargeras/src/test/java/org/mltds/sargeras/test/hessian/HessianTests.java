package org.mltds.sargeras.test.hessian;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.mltds.sargeras.spi.serializer.hessian.HessianSerializer;

import java.io.IOException;
import java.util.Random;

/**
 * @author sunyi.
 */

public class HessianTests {

    int i =  RandomUtils.nextInt(0,1000);

    private HessianSerializer hessianSerializer = new HessianSerializer();

    @Test
    public void test() throws IOException {

        byte[] encode = hessianSerializer.encode(this);

        HessianTests decode = hessianSerializer.decode(encode, this.getClass());

        System.out.println(decode.i);


    }


}

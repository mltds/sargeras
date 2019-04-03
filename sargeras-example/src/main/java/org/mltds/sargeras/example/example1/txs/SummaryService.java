package org.mltds.sargeras.example.example1.txs;

import org.mltds.sargeras.api.annotation.SagaTx;
import org.mltds.sargeras.example.example1.BookResult;
import org.springframework.stereotype.Service;

/**
 * @author sunyi.
 */
@Service
public class SummaryService {

    @SagaTx
    public BookResult summary(String bizId, String carOrderNo, String airOrderNo, String hotelOrderNo) {

        BookResult bookResult = new BookResult();

        bookResult.success = true;

        bookResult.bizId = bizId;

        bookResult.carOrderNo = carOrderNo;
        bookResult.aireOrderNo = airOrderNo;
        bookResult.hotelOrderNo = hotelOrderNo;

        return bookResult;
    }

}

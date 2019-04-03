package org.mltds.sargeras.example.example1.txs;

import org.mltds.sargeras.api.annotation.Saga;
import org.mltds.sargeras.api.annotation.SagaBizId;
import org.mltds.sargeras.example.example1.BookResult;
import org.mltds.sargeras.example.example1.FamilyMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author sunyi.
 */
@Service
public class TravelService {

    @Autowired
    private CarService carService;

    @Autowired
    private AirService airService;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private SummaryService summaryService;

    @Saga(appName = "Example", bizName = "Travel")
    public BookResult travel(@SagaBizId String bizId, FamilyMember familyMember) {

        String bookCarNo = carService.book(bizId);
        String bookAirNo = airService.book(bizId);
        String bookHotelNo = hotelService.book(bizId, familyMember);

        BookResult result = summaryService.summary(bizId, bookCarNo, bookAirNo, bookHotelNo);

        return result;
    }

}

package com.airlines.service;

import com.airlines.entity.SearchAirticketsInput;
import com.airlines.entity.SearchAirticketsInputSegment;
import com.airlines.entity.SearchAirticketsPriceDetail;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;


@Slf4j
@SpringBootTest
class JejuAirServiceTest {

    @Resource
    JejuAirService jejuAirService;

    @Test
    void searchAirticketsPriceDetail() {

        SearchAirticketsInputSegment searchAirticketsInputSegment = new SearchAirticketsInputSegment();
        searchAirticketsInputSegment.setDepCityCode("ICN");
        searchAirticketsInputSegment.setArrCityCode("HKG");
        searchAirticketsInputSegment.setDepDate(LocalDate.now());
        List<SearchAirticketsInputSegment> searchAirticketsInputSegments = List.of(searchAirticketsInputSegment);

        SearchAirticketsInput searchAirticketsInput = new SearchAirticketsInput();
        searchAirticketsInput.setAdultNum(1);
        searchAirticketsInput.setChildNum(0);
        searchAirticketsInput.setTripType(1);
        searchAirticketsInput.setFromSegments(searchAirticketsInputSegments);
        searchAirticketsInput.setCabinClass(1);
        searchAirticketsInput.setCabin("E");
        searchAirticketsInput.setRetSegments(null);

        SearchAirticketsPriceDetail searchAirticketsPriceDetail = jejuAirService.searchAirticketsPriceDetail(searchAirticketsInput);
        log.info(JSON.toJSONString(searchAirticketsPriceDetail));
    }
}
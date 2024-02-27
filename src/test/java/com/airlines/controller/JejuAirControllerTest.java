package com.airlines.controller;

import com.alibaba.fastjson2.JSON;
import com.heytrip.common.domain.SearchAirticketsBaseInput;
import com.heytrip.common.domain.SearchAirticketsInput;
import com.heytrip.common.domain.SearchAirticketsInputSegment;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JejuAirControllerTest {

    @Resource
    private JejuAirController jejuAirController;

//    @BeforeEach
//    void setUp() {
//        jejuAirController = new JejuAirController();
//    }

    @Test
    void searchAirticketsPriceDetail() {
        SearchAirticketsInputSegment searchAirticketsInputSegment = new SearchAirticketsInputSegment();
        searchAirticketsInputSegment.setDepCityCode("GMP");
        searchAirticketsInputSegment.setArrCityCode("PUS");
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

        SearchAirticketsBaseInput searchAirticketsBaseInput = new SearchAirticketsBaseInput();
        searchAirticketsBaseInput.setData(searchAirticketsInput);

        Object o = jejuAirController.searchAirticketsPriceDetail(searchAirticketsBaseInput);
        System.out.println(JSON.toJSONString(o));
    }

}
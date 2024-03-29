package com.airlines.service;

import com.heytrip.common.domain.SearchAirticketsInput;
import com.heytrip.common.domain.SearchAirticketsInputSegment;
import com.heytrip.common.domain.SearchAirticketsPriceDetail;
import com.alibaba.fastjson2.JSON;
import com.heytrip.common.enums.CurrencyEnums;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@SpringBootTest
class JejuAirServiceTest {

    @Resource
    JejuAirService jejuAirService;

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

        List<SearchAirticketsPriceDetail> searchAirticketsPriceDetails = jejuAirService.searchAirticketsPriceDetail(searchAirticketsInput);
        log.info(JSON.toJSONString(searchAirticketsPriceDetails));
    }

    public static void main(String[] args) throws InterruptedException {
        String currency = "KRW";
        // 如果currency的值和CurrencyEnums中的code一样，则输出他的number
//        CurrencyEnums.getCode(currency);
    }
}
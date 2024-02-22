package com.airlines.service;

import com.heytrip.common.domain.SearchAirticketsInput;
import com.heytrip.common.domain.SearchAirticketsInputSegment;
import com.heytrip.common.domain.SearchAirticketsPriceDetail;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public static void main(String[] args) throws InterruptedException {
        String s = "2024.02.2221:25";
        // 将这种格式的s转换成时间格式
        LocalDateTime localDate = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy.MM.ddHH:mm"));
        // localDate加一天
        localDate = localDate.plusDays(1);
        log.info("==========="+localDate.toString());
    }
}
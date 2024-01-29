package com.airlines.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import com.heytrip.hotelinfosync.dto.RateDTO;
import com.heytrip.hotelinfosync.dto.RoomDTO;
import com.heytrip.hotelinfosync.dto.UpdateRoomAndRateDTO;
import com.heytrip.hotelinfosync.service.GrabApiService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MeritResortSamuiServiceImpl {
    @Resource
    GrabApiService grabApiService;

    // 每三个小时执行一次这个方法
    @Scheduled(cron = "0 30 0/3 * * ?")
    public Object insert() {

        // T+3 月后结束
        DateTime beginDate = DateTime.now();
        DateTime endOfMonth = DateUtil.offsetMonth(beginDate, 3);
        DateTime endDate = DateUtil.endOfMonth(endOfMonth);

        insertHotelInfoByRange(beginDate, endDate);

        // 调用数据推送接口将数据推送
        grabApiService.PushDataUpdateDate(546, "73701", "meritresortsamui", beginDate);

        return "成功";
    }

    public void insertHotelInfoByRange(DateTime bDate, DateTime eDate) {
        List<DateTime> dateList = DateUtil.rangeToList(bDate, eDate, DateField.DAY_OF_YEAR);
        List<List<DateTime>> partition = ListUtil.partition(dateList, 30);
        for (List<DateTime> partitionDateList : partition) {
            insertHotelInfoOf30Day(partitionDateList);
        }
    }

    private void insertHotelInfoOf30Day(List<DateTime> partitionDateList) {
        List<UpdateRoomAndRateDTO> list = queryHotelInfoOf30Day(partitionDateList);
        // 调用数据新增接口将数据同步
        grabApiService.UpdateRoomAndRateMuti(list);
    }

    private List<UpdateRoomAndRateDTO> queryHotelInfoOf30Day(List<DateTime> partitionDateList) {
        List<UpdateRoomAndRateDTO> list = new ArrayList<>();
        partitionDateList.forEach(date -> {
            try {
                log.info("====== Merit Resort Samui =======");
                log.info("开始爬取{}到{}的酒店信息", DateUtil.format(date, "yyyy-MM-dd"), DateUtil.format(DateUtil.offsetDay(date, 1), "yyyy-MM-dd"));
                UpdateRoomAndRateDTO updateRoomAndRateDTO = queryHotelInfo(date);
                list.add(updateRoomAndRateDTO);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        return list;
    }

    public UpdateRoomAndRateDTO queryHotelInfo(DateTime checkInDate) throws IOException, InterruptedException {

        ArrayList<RoomDTO> roomList = new ArrayList<>();
        ArrayList<RateDTO> rateList = new ArrayList<>();

        String checkInParam = getCheckInParam(checkInDate);
        String checkOutParam = getCheckOutParam(checkInDate);
        String url = getUrl(checkInParam, checkOutParam);

        //设置chrome选项
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");//开启无头模式
        options.addArguments("--disable-gpu");//禁止gpu渲染
        options.addArguments("–-no-sandbox");//关闭沙盒模式
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--blink-settings=imagesEnabled=false");//禁止加载图片

        WebDriver driver = new ChromeDriver(options);
        //设置5秒,全局寻找元素的等待时间
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        driver.get(url);
        String title = driver.getTitle();
        log.info(title);
        String pageSource = driver.getPageSource();
        driver.quit();

        Document document = Jsoup.parse(pageSource);
        Elements elements = document.getElementsByClass("hotex-box-rates-content ng-scope hotex-border");
        elements.forEach(element -> {
            Element room = element.getElementsByClass("room-information-button").first();
            String roomId = room.attr("id");
            String roomName = room.attr("data-roomname");
            String priceStr = element.getElementsByTag("ng-rateaverageamount").first().text();

            RateDTO rateDTO = getRateDTO(checkInDate, roomId);
            rateList.add(rateDTO);
            RoomDTO roomDTO = getRoomDTO(roomId, roomName);
            roomList.add(roomDTO);
        });
        return getUpdateRoomAndRateDTO(checkInDate, roomList, rateList);
    }

    private static String getCheckOutParam(DateTime checkInDate) {
        DateTime checkOutDate = DateUtil.offsetDay(checkInDate, 1);
        String checkOutStr = DateUtil.format(checkOutDate, "MM/dd/yyyy");
        return URLEncoder.encode(checkOutStr, StandardCharsets.UTF_8);
    }

    private static String getCheckInParam(DateTime checkInDate) {
        String checkInStr = DateUtil.format(checkInDate, "MM/dd/yyyy");
        return URLEncoder.encode(checkInStr, StandardCharsets.UTF_8);
    }

    private static String getUrl(String checkInParam, String checkOutParam) {
        String url = "https://booking.allhandsmarketing.com/booking/portal.aspx" +
                "?langcd=en" +
                "&hotelcd=HT18005337" +
                "&ci=" + checkInParam +
                "&co=" + checkOutParam +
                "&room=1" +
                "&adult=1" +
                "&child=" +
                "&promocd=";
        return url;
    }

    private static RateDTO getRateDTO(DateTime checkInDate, String roomId) {
        RateDTO rateDTO = new RateDTO();
        rateDTO.setRoomId(roomId);
        rateDTO.setRoomCode(roomId);
        rateDTO.setDate(checkInDate);
        rateDTO.setEndDate(DateUtil.offsetDay(checkInDate, 1));
        rateDTO.setStock(1);
        rateDTO.setStatus(1);
//        rateDTO.setPrice(new BigDecimal(priceStr));
        rateDTO.setCurrency("THB");
        return rateDTO;
    }

    private static RoomDTO getRoomDTO(String roomId, String title) {
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setRoomId(roomId);
        roomDTO.setRoomCode(roomId);
        roomDTO.setRoomName(title);
        roomDTO.setPriceType(1);
        roomDTO.setStayPriceType(0);
        return roomDTO;
    }

    private static UpdateRoomAndRateDTO getUpdateRoomAndRateDTO(DateTime checkInDate, ArrayList<RoomDTO> roomList, ArrayList<RateDTO> rateList) {
        UpdateRoomAndRateDTO dto = new UpdateRoomAndRateDTO();
        dto.setHotelId("73701");
        dto.setSourceType(546);
        dto.setCurrency("THB");
        dto.setCheckIn(checkInDate);
        dto.setCheckOut(DateUtil.offsetDay(checkInDate, 1));
        dto.setAdultNum(1);
        dto.setRoomList(roomList);
        dto.setRateList(rateList);
        log.info("dto: {}", JSON.toJSONString(dto));
        return dto;
    }
}

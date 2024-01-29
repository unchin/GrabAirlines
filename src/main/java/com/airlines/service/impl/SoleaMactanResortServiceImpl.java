package com.airlines.service.impl;


import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.heytrip.hotelinfosync.dto.RateDTO;
import com.heytrip.hotelinfosync.dto.RoomDTO;
import com.heytrip.hotelinfosync.dto.UpdateRoomAndRateDTO;
import com.heytrip.hotelinfosync.service.GrabApiService;
import com.heytrip.hotelinfosync.service.SoleaMactanResortService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Unchin
 * @date 2024-01-16
 */
@Service
@Slf4j
public class SoleaMactanResortServiceImpl implements SoleaMactanResortService {

    @Resource
    GrabApiService grabApiService;

    // 每三个小时执行一次这个方法
    @Scheduled(cron = "0 0 0/3 * * ?")
    public Object insert() {

        // 爬取酒店信息
        log.info("开始爬取 SoleaMactanResort 数据");
        DateTime beginDate = DateTime.now();
        DateTime endOfMonth = DateUtil.offsetMonth(beginDate, 3);
        DateTime endDate = DateUtil.endOfMonth(endOfMonth);
        insertHotelInfoByRange(beginDate, endDate);

        // 调用数据推送接口将数据推送
        grabApiService.PushDataUpdateDate(545, "70984", "soleamactanresort", beginDate);

        return "成功";
    }

    public void insertHotelInfoByRange(DateTime bDate, DateTime eDate) {
        List<DateTime> dateList = DateUtil.rangeToList(bDate, eDate, DateField.DAY_OF_YEAR);
        List<List<DateTime>> partition = Lists.partition(dateList, 30);
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
                log.info("开始爬取{}到{}的酒店信息", DateUtil.format(date, "yyyy-MM-dd"), DateUtil.format(DateUtil.offsetDay(date, 1), "yyyy-MM-dd"));
                UpdateRoomAndRateDTO updateRoomAndRateDTO = queryHotelInfo(date);
                list.add(updateRoomAndRateDTO);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return list;
    }

    public UpdateRoomAndRateDTO queryHotelInfo(DateTime checkInDate) throws IOException {

        DateTime checkOutDate = DateUtil.offsetDay(checkInDate, 1);
        String checkInParam = DateUtil.format(checkInDate, "dd+MM+yyyy");
        String checkOutParam = DateUtil.format(checkOutDate, "dd+MM+yyyy");

        ArrayList<RoomDTO> roomList = new ArrayList<>();
        ArrayList<RateDTO> rateList = new ArrayList<>();

        String url = getUrl(checkInParam, checkOutParam);
        Document document = Jsoup.connect(url).get();

        // 获取房源列表
        Element booking_result = document.getElementById("booking_result");
        assert booking_result != null;

        // 获取不同类型房源列表
        Elements elements = booking_result.getElementsByClass("col-sm-8 special-room-type");
        for (Element element : elements) {
            String title = element.getElementsByClass("roomtype-title").first().text();
            String roomId = getRoomId(element);
            String priceStr = getPriceStr(element);

            Element ratesTable = booking_result.getElementById("ratestable-"+roomId);
            assert ratesTable != null;
            Element select = ratesTable.select("select[data-total]").first();

            // 可以查到库存就是可预定状态，更新列表
            if (select != null) {
                String store = select.attr("data-total");
                RoomDTO roomDTO = getRoomDTO(roomId, title);
                roomList.add(roomDTO);
                RateDTO rateDTO = getRateDTO(checkInDate, roomId, checkOutDate, priceStr,Integer.parseInt(store));
                rateList.add(rateDTO);
            }
        }
        return getUpdateRoomAndRateDTO(checkInDate, checkOutDate, roomList, rateList);
    }

    private static UpdateRoomAndRateDTO getUpdateRoomAndRateDTO(DateTime checkInDate, DateTime checkOutDate, ArrayList<RoomDTO> roomList, ArrayList<RateDTO> rateList) {
        UpdateRoomAndRateDTO dto = new UpdateRoomAndRateDTO();
        dto.setHotelId("70984");
        dto.setSourceType(545);
        dto.setCurrency("USD");
        dto.setCheckIn(checkInDate);
        dto.setCheckOut(checkOutDate);
        dto.setAdultNum(1);
        dto.setRoomList(roomList);
        dto.setRateList(rateList);
        return dto;
    }

    private static RateDTO getRateDTO(DateTime checkInDate, String roomId, DateTime checkOutDate, String priceStr,Integer store) {
        RateDTO rateDTO = new RateDTO();
        rateDTO.setRoomId(roomId);
        rateDTO.setRoomCode(roomId);
        rateDTO.setDate(checkInDate);
        rateDTO.setEndDate(checkOutDate);
        rateDTO.setStock(store);
        rateDTO.setStatus(1);
        rateDTO.setPrice(new BigDecimal(priceStr));
        rateDTO.setCurrency("USD");
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

    private static String getRoomId(Element element) {
        Element moreinformation = element.getElementsByClass("moreinformation").first();
        String attrStr = moreinformation.attr("data-target");
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(attrStr);
        String roomId = m.replaceAll("").trim();
        return roomId;
    }

    private static String getPriceStr(Element element) {
        String priceStr = element.getElementsByClass("rate").first().text();
        // 去掉$符号
        priceStr = StrUtil.removePrefix(priceStr, "$");
        return priceStr;
    }

    private static String getUrl(String checkInParam, String checkOutParam) {
        String url = "https://book.hiverooms.com/booking/resp_booking_page.php?property_id=4419" +
                "&user_id=0" +
                "&checkin=" + checkInParam +
                "&checkout=" + checkOutParam +
                "&currency=PHP" +
                "&currency_convert=USD" +
                "&deposit=percent_100__0" +
                "&payment=%5B%22direct_percent_0_confirm*check.php%22%5D&taxes=" +
                "&roomid=" +
                "&promo_code=" +
                "&property_lang=" +
                "&lang_id=100" +
                "&source_booking=" +
                "&google_tag=GTM-KXW8S2CK" +
                "&type=Room" +
                "&direct_domain=&extra_only=" +
                "&max_infants_limit=0&whatsapp=" +
                "&soldcontact=1" +
                "&captcha=0" +
                "&ota_check=0" +
                "&timezone=Asia%2FKuala_Lumpur" +
                "&get_country_country=United+States+of+America";
        return url;
    }


}

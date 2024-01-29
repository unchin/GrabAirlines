package com.airlines.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.heytrip.hotelinfosync.dto.UpdateRoomAndRateDTO;
import com.heytrip.hotelinfosync.service.GrabApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class GrabApiServiceImpl implements GrabApiService {

    @Value("${grabapi.url}")
    private String GRABAPI_URL;

    @Override
    public Boolean UpdateRoomAndRateMuti(List<UpdateRoomAndRateDTO> list) {

        String api = "/api/HotelData/UpdateRoomAndRateMuti";
        String url = GRABAPI_URL + api;

        String json = JSON.toJSONString(list, "yyyy-MM-dd", JSONWriter.Feature.PrettyFormat);
        log.info("数据推送（UpdateRoomAndRateMuti）参数 =========>" + json);

        HttpResponse response = HttpRequest.post(url).body(json).execute();
        log.info("数据推送结果（UpdateRoomAndRateMuti） =========>" + response.body());
        return response.getStatus() == 200;
    }

    @Override
    public Object PushDataUpdateDate(Integer sourceType, String hotelId, String type, Date periodBegin) {

        String api = "/api/HotelData/PushDataUpdateDate";
        String url = GRABAPI_URL + api;

        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("HotelId", hotelId);
        paramMap.put("SourceType", sourceType);
        paramMap.put("Type", type);
        paramMap.put("PeriodBegin", periodBegin);

        String json = JSON.toJSONString(paramMap);
        log.info("消息推送参数（PushDataUpdateDate） =========>" + json);
        HttpResponse response = HttpRequest.post(url).body(json).execute();
        log.info("消息推送结果（PushDataUpdateDate） =========>" + response.body());
        return response.getStatus() == 200;
    }

    @Override
    public Object getHotelInfo(String sourceType, String hotelId, String key) {

        String api = "/api/HotelData/GetHotel";
        String url = GRABAPI_URL + api;

        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("HotelId", hotelId);
        paramMap.put("SourceType", sourceType);
        paramMap.put("key", key);
        String json = JSON.toJSONString(paramMap);
        log.info(json);

        HttpResponse response = HttpRequest.post(url)
                .body(json)
                .execute();
        int status = response.getStatus();
        if (status == 200) {
            String body = response.body();
            log.info(body);
        }
        return response.body();
    }
}

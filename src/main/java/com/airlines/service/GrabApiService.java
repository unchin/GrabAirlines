package com.airlines.service;

import com.heytrip.hotelinfosync.dto.UpdateRoomAndRateDTO;

import java.util.Date;
import java.util.List;

public interface GrabApiService{


    Object getHotelInfo(String sourceType, String hotelId, String key);

    Object UpdateRoomAndRateMuti(List<UpdateRoomAndRateDTO> list);

    Object PushDataUpdateDate(Integer sourceType, String hotelId, String type, Date periodBegin);
}

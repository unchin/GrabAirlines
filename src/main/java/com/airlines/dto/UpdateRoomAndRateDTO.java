package com.airlines.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UpdateRoomAndRateDTO {

    //必填 枚举值 （酒店的集团枚举值）
    private int SourceType;

    //必填 酒店ID
    private String HotelId;

    //选填 官网币种
    private String Currency;

    //必填 入住日期 （跟RateList里面的保持一致，当 RateList 为空数组时，需要用它来关联关闭房态）
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date CheckIn;

    //选填 离店日期 （跟RateList里面的保持一致，当 RateList 为空数组时，需要用它来关联关闭房态）
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date CheckOut;

    //成人数 （跟RateList里面的AdultNum保持一致，当 RateList 为空数组时，需要用它来关联关闭房态）
    private int AdultNum;

    //必填 有效房型集合 （无房时，传空数组）
    private List<RoomDTO> RoomList;

    //必填 有效价格集合 （无房时，传空数组）
    private List<RateDTO> RateList;

}

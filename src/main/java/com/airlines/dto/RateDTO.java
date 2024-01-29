package com.airlines.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class RateDTO {

        //必填  与关联房间中的RoomId保持一致
        private String RoomId;

        //选填 房型ID
        private String RoomCode;

        //必填 入住日期
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date Date;

        //选填 离店日期
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date EndDate;

        //必填 库存(与房态开关联动)
        private int Stock;

        //必填  actionCode：房态开关(与库存联动)  1.开 0.关
        private int Status;

        //最新价格
        private BigDecimal Price;

        //价格的币种
        private String Currency;

}

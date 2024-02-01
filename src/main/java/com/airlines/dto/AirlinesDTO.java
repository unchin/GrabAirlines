package com.airlines.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class AirlinesDTO {

    /**
     * 航线id
     */
    private String airlineId;

    /**
     * 航线报价id
     */
    private String airlinesRateId;

    /**
     * 航班号
     */
    private String flightNumber;

    /**
     * 出发城市
     */
    private String takeoffCity;

    /**
     * 到达城市
     */
    private String landingCity;

    /**
     * 起飞机场
     */
    private String takeoffStation;

    /**
     * 落地机场
     */
    private String landingStation;

    /**
     * 起飞日期
     */
    @JSONField(format = "yyyy-MM-dd")
    private String takeoffDate;

    /**
     * 落地日期
     */
    @JSONField(format = "yyyy-MM-dd")
    private String landingDate;

    /**
     * 起飞时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private String takeoffTime;

    /**
     * 落地时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private String landingTime;

    /**
     * 飞行时间（分钟）
     */
    private Integer duration;

    /**
     * 舱位等级
     */
    private String cabinClass ;
}

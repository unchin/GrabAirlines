package com.airlines.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AirlinesRateDTO {

    /**
     * id
     */
    private String airlinesRateId;

    /**
     * 航空公司名称
     */
    private String airlinesName;

    /**
     * 航空公司代码
     */
    private String airlinesCode;

    /**
     * 出发日期
     */
    @JSONField(format = "yyyy-MM-dd")
    private String departureDate;

    /**
     * 到达日期
     */
    @JSONField(format = "yyyy-MM-dd")
    private String arriveDate;

    /**
     * 出发时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private String departureTime;

    /**
     * 到达时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private String arriveTime;

    /**
     * 中转次数
     */
    private String transferTimes;

    /**
     * 出发城市
     */
    private String departureCity;

    /**
     * 到达城市
     */
    private String arriveCity;

    /**
     * 第一次起飞机场代码
     */
    private String departureStation;

    /**
     * 最后一次落地机场代码
     */
    private String arriveStation;

    /**
     * 总花费时间（分钟）
     */
    private Integer durationTotal;

    /**
     * 人数
     */
    private int adultNum;

    /**
     * 最新价格
     */
    private BigDecimal price;

    /**
     * 价格的币种
     */
    private String currency;

    /**
     * 航班信息列表
     */
    private List<AirlinesDTO> airlinesList;

    /**
     * 价格类型列表
     */
    private List<RateDTO> rateList;

}

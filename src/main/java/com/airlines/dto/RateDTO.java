package com.airlines.dto;

import lombok.Data;

@Data
public class RateDTO {

    /**
     * id
     */
    private String rateId;

    /**
     * 航线报价id
     */
    private String airlinesRateId;

    /**
     * 价格类型方案
     */
    private int rateType;

    /**
     * 行李限额
     */
    private int capacity;
}

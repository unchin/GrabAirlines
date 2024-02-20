package com.airlines.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 航段
 */
@Setter
@Getter
public class SearchAirticketsInputSegment {
    /**
     * 出发地 （IATA 三字码代码） CAN
     */
    @JsonProperty("DepCityCode")
    private String depCityCode;
    /**
     * 出发时间（格式：yyyy-MM-dd）  2023-12-27
     */
    @JsonProperty("DepDate")
    private LocalDate depDate;
    /**
     * 目的地 （IATA 三字码代码） BAK
     */
    @JsonProperty("ArrCityCode")
    private String arrCityCode;
}

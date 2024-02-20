package com.airlines.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SearchAirticketsInput {
    /**
     * 单程 1
     * 往返 2
     * 多程 3
     * 往返中推荐多程 4
     */
    @JsonProperty("TripType")
    private Integer tripType;
    /**
     * 成人数
     */
    @JsonProperty("AdultNum")
    private Integer adultNum;
    /**
     * 儿童数
     */
    @JsonProperty("ChildNum")
    private Integer childNum;
    /**
     * 去程
     */
    @JsonProperty("FromSegments")
    private List<SearchAirticketsInputSegment> fromSegments;
    /**
     * 回程
     */
    @JsonProperty("RetSegments")
    private List<SearchAirticketsInputSegment> retSegments;
    /**
     * 1：经济舱，2：商务舱，3：头等舱，4：超级经济舱，5：超级商务舱，6：超级头等舱
     */
    @JsonProperty("CabinClass")
    private Integer cabinClass;
    /**
     * 舱位
     */
    @JsonProperty("Cabin")
    private String cabin;
    /**
     * 航司
     */
    @JsonProperty("Carrier")
    private String carrier;
    /**
     * 航班号
     */
    @JsonProperty("FlightNumber")
    private String flightNumber;
}

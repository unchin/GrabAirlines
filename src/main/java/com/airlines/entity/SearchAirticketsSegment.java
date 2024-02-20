package com.airlines.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchAirticketsSegment {
    /**
     * 航司   BR
     */
    private String carrier;
    /**
     * 舱位   Y
     */
    private String cabin;
    /**
     * 舱位等级 1
     */
    private Integer cabinClass;
    /**
     * 航班号（统一去除前面的0，如009，传9）    368
     */
    private String flightNumber;
    /**
     * 出发机场 CAN
     */
    private String depAirport;
    /**
     * 到达机场 BAK
     */
    private String arrAirport;
    /**
     * 出发时间（格式：yyyy-MM-dd HH:mm）    2023-12-18 14:15
     */
    private String depDate;
    /**
     * 到达时间（格式：yyyy-MM-dd HH:mm）    2023-12-18 16:15
     */
    private String arrDate;
    /**
     * 经停地，/分隔城市三字码 CNF/CFD
     */
    private String stopCities;
    /**
     * 代码共享标识（true 代码共享/false 非代码共享）    false
     */
    private Boolean codeShare;
    /**
     * 共享航司（实际承运航司） CK
     */
    private String shareCarrier;
    /**
     * 共享航班号（实际承运航班号）   678
     */
    private String shareFlightNumber;
    /**
     * 机型   Boyin 737
     */
    private String aircraftCode;
    /**
     * 航段标识，单程可不标识，多程必填 1
     */
    private Integer group;
    /**
     * 运价基础	GAK4AQBS
     */
    private String fareBasis;
    /**
     * 该航段对应的gds类型	
     */
    private Integer gdsType;
    /**
     * 该航段对应的销售地，国家二字码：CA CN    CN
     */
    private String posArea;
    /**
     * 托运行李规则
     */
    private BaggageRule baggageRule;
    /**
     * 航司大编码	AJS4S3
     */
    private String airlinePnrCode;
}

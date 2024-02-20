package com.airlines.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaggageRule {
    /**
     * 是否存在免费行李额
     */
    private Boolean hasBaggage;
    /**
     * 每件行李最大公斤数
     */
    private Integer baggageKg;
    /**
     * 行李最大件数
     */
    private Integer baggagePiece;
}

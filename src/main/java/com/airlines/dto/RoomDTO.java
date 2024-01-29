package com.airlines.dto;

import lombok.Data;

@Data
public class RoomDTO {

    /**
     * 必填 唯一值  rpId  roomCode_RatePlanCode_MinDay
     * 房型级：RoomId={RoomCode}
     * RatePlan级：RoomId={RoomCode}_{RatePlanCode}
     * 有多晚需求，MinDay>1时：RoomId={RoomCode}_{MinDay} 或 {RoomCode}_{RatePlanCode}_{MinDay}
     * 有不同成人数需求时，外层AdultNum>0，程序会自动拼接P|{AdultNum},这里不需要处理
     * 注：RoomId，不能含空格，无论前后或中间；不能含特殊字符；不能含中文或乱码。可以含字母，数字，符号-_ 。注意长度不要超100，过长时可用md5加密
     */
    private String RoomId;

    //必填 房型ID
    private String RoomCode;

    //必填 房型名称
    private String RoomName;

    //可选 房价ID （当按房型级时，可空。按RatePlan级时，必填）
    private String RatePlanCode;

    //可选 房价名称（当按房型级时，可空。按RatePlan级时，必填）
    private String RatePlanName;

    //价格按哪一个级别来 0.未赋值  1.房型级  2.RatePlan级 (当不传时，默认与hotel的PriceType一致)
    private int PriceType;

    //连住天数(没有多晚需求时，不要传值)
    private int MinDay;

    //连住的价格类型 默认值为0  0.首晚价格  1.均价(没有多晚需求时，不要传值)
    private int StayPriceType;
}

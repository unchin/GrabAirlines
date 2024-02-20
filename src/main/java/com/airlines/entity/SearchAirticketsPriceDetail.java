package com.airlines.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SearchAirticketsPriceDetail {
    /**
     * 报价编码，可自定义值，不超过1000字符
     */
    private String rateCode;
    /**
     * 货币
     */
    private String currency;
    /**
     * 成人单价
     */
    private BigDecimal adultPrice;
    /**
     * 成人税费
     */
    private BigDecimal adultTax;
    /**
     * 儿童单价
     */
    private BigDecimal childPrice;
    /**
     * 儿童税费
     */
    private BigDecimal childTax;
    /**
     * 乘客国籍适用类型：0 全部/1 适用/2 不适用   
     */
    private Integer nationalityType;
    /**
     * 适用乘客国籍，可以为空，如果NationalityType 选择1,2  此项不为空，需填写国家二字码，最多填写五个国家或地区
     */
    private List nationality;
    /**
     * 适用乘客年龄，输入格式为12~59，表示适用于12~59 岁的乘客预订，年龄限制范围为 12~99，仅支持录入一个年龄段
     */
    private String suitAge;
    /**
     * 成人报价是否含税（如果AdultPrice未含税，成人总价会加上AdultTax，如果含税，则不会加上成人税费）
     */
    private Boolean adultTaxIncluded;
    /**
     * 儿童报价是否含税（ChildPrice如果未含税，成人总价会加上AdultTax，如果含税，则不会加上成人税费）
     */
    private Boolean childTaxIncluded;
    /**
     * 出票速度：[1~10080] （以分钟为单位）供应商无法提供可不传，默认0
     */
    private Integer ticketTimeLimit;
    /**
     * 发票报销类型（ 0 不提供发票  1行程票 2 发票 3 行程单+差额发票）
     */
    private Integer ticketInvoiceType;
    /**
     * 最小适用人数 
     */
    private Integer minFittableNum;
    /**
     * 最大适用人数
     */
    private Integer maxFittableNum;
    /**
     * 产品类型
     */
    private String productType;
    /**
     * 去程航段按顺序
     */
    private List<SearchAirticketsSegment> fromSegments;
    /**
     * 返程航段
     */
    private List<SearchAirticketsSegment> retSegments;
    /**
     * 出票航司 
     */
    private String ticketAirline;

}

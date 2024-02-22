package com.airlines.service;

import com.heytrip.common.domain.SearchAirticketsInput;
import com.heytrip.common.domain.SearchAirticketsPriceDetail;

public interface JejuAirService {

    /**
     * 查询有价航空数据
     */
//    String getJejuAir();

    /**
     * 搜索指定航空票价
     */
    SearchAirticketsPriceDetail searchAirticketsPriceDetail(SearchAirticketsInput searchAirticketsInput);
}

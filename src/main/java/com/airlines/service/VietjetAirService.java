package com.airlines.service;

import com.heytrip.common.domain.SearchAirticketsInput;
import com.heytrip.common.domain.SearchAirticketsPriceDetail;
public interface VietjetAirService {

    SearchAirticketsPriceDetail searchAirticketsPriceDetail(SearchAirticketsInput searchAirticketsInput);
}

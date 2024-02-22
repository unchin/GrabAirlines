package com.airlines.service;

import com.airlines.entity.SearchAirticketsInput;
import com.airlines.entity.SearchAirticketsPriceDetail;

public interface VietjetAirService {

    SearchAirticketsPriceDetail searchAirticketsPriceDetail(SearchAirticketsInput searchAirticketsInput);
}

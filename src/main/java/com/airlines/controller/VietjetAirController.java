package com.airlines.controller;

import com.airlines.service.VietjetAirService;
import com.heytrip.common.domain.SearchAirticketsInput;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/vietjet")
public class VietjetAirController {

    @Resource
    private VietjetAirService vietjetAirService;

    @PostMapping("/searchAirticketsPriceDetail")
    public Object searchAirticketsPriceDetail(@RequestBody SearchAirticketsInput searchAirticketsInput){
        return vietjetAirService.searchAirticketsPriceDetail(searchAirticketsInput);
    }
}

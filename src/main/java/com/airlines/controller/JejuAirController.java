package com.airlines.controller;

import com.airlines.entity.SearchAirticketsInput;
import com.airlines.service.JejuAirService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/jeju")
public class JejuAirController {

    @Resource
    private JejuAirService jejuAirService;

    @PostMapping("/searchAirticketsPriceDetail")
    public Object searchAirticketsPriceDetail(@RequestBody SearchAirticketsInput searchAirticketsInput){
        return jejuAirService.searchAirticketsPriceDetail(searchAirticketsInput);
    }

}

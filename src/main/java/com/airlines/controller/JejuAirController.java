package com.airlines.controller;

import com.airlines.service.JejuAirService;
import com.heytrip.common.base.BaseResult;
import com.heytrip.common.domain.SearchAirticketsBaseInput;
import com.heytrip.common.domain.SearchAirticketsInput;
import com.heytrip.common.domain.SearchAirticketsOutput;
import com.heytrip.common.domain.SearchAirticketsPriceDetail;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/jeju")
public class JejuAirController {

    @Resource
    private JejuAirService jejuAirService;

    @PostMapping("/searchAirticketsPriceDetail")
    public Object searchAirticketsPriceDetail(@RequestBody SearchAirticketsBaseInput searchAirticketsBaseInput){
        // todo 校验接口基础信息

        // 查询
        SearchAirticketsInput input = searchAirticketsBaseInput.getData();
        List<SearchAirticketsPriceDetail> searchAirticketsPriceDetails = jejuAirService.searchAirticketsPriceDetail(input);

        // 封装返回结果
        SearchAirticketsOutput output = new SearchAirticketsOutput();
        output.setPriceDetails(searchAirticketsPriceDetails);
        output.setSuccess(true);
        return BaseResult.success(output);
    }

}

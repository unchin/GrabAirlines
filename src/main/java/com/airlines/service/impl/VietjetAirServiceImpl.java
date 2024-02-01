package com.airlines.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.airlines.util.GrabUtil.getPageSource;

@Service
@Slf4j
public class VietjetAirServiceImpl {

    public static void main(String[] args) throws IOException, InterruptedException {

        String url = "https://www.jejuair.net/zh-cn/main/base/index.do";
        String pageSource = getPageSource(url);
    }
}

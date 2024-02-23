package com.airlines.util;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SeleniumUtil {

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();

        driver.get("F:\\rep\\GrabAirlines\\src\\main\\java\\com\\airlines\\util\\HtmlUtil.html");
        driver.getTitle();
        WebElement statusElement;
            statusElement = driver.findElement(By.id("divArrArea"));
        // divArrArea的上上级
        WebElement statusElementParent = statusElement.findElement(By.xpath(".."));
        statusElementParent = statusElementParent.findElement(By.xpath(".."));

        List<WebElement> departureStationList = statusElementParent.findElements(By.cssSelector("button[data-stationcode]"));
        log.info("查询出来的地点列表大小" + departureStationList.size());

        // 取出所有地点的code
        List<String> departureStationCodeList = new ArrayList<>();
        for (WebElement station : departureStationList) {
            String stationCode = station.getAttribute("data-stationcode");
            departureStationCodeList.add(stationCode);
        }
        log.info("查询出来的地点列表" + departureStationCodeList);

        driver.quit();
    }

    public static WebDriver getWebDriver() {
        //设置chrome选项
        ChromeOptions options = new ChromeOptions();
        //开启无头模式
//        options.addArguments("--headless");
        //禁止gpu渲染
        options.addArguments("--disable-gpu");
        //关闭沙盒模式
        options.addArguments("–-no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        //禁止加载图片
        options.addArguments("--blink-settings=imagesEnabled=false");
        // 去掉 webdriver痕迹
        options.addArguments("disable-blink-features=AutomationControlled");

        options.addArguments("--user-agent=Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");

        return new ChromeDriver(options);
    }
}

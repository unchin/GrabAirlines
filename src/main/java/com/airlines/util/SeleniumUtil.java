package com.airlines.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.util.List;

public class SeleniumUtil {

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();

        driver.get("F:\\rep\\GrabAirlines\\src\\main\\java\\com\\airlines\\util\\HtmlUtil.html");
        driver.getTitle();
        List<WebElement> elementsWithDataStationCode = driver.findElements(By.cssSelector("[data-stationcode]"));

        // 遍历并打印每个元素的 data-stationcode 值
        for (WebElement element : elementsWithDataStationCode) {
            String dataStationCodeValue = element.getAttribute("data-stationcode");
            System.out.println("data-stationcode value: " + dataStationCodeValue);
        }

        driver.quit();
    }

    public static WebDriver getWebDriver() {
        //设置chrome选项
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless");//开启无头模式
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
//        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.79 Safari/537.36");

        return new ChromeDriver(options);
    }
}

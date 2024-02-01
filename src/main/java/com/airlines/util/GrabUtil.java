package com.airlines.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.util.List;

@Slf4j
public class GrabUtil {

    public static String getPageSource(String url) throws InterruptedException {
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

        WebDriver driver = new ChromeDriver(options);
        driver.get(url);

        //设置5秒,全局寻找元素的等待时间
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        // 单程
        driver.findElement(By.xpath("//li[@data-triptype='OW']")).click();

        // 出发地点
        driver.findElement(By.id("spanDepartureDesc")).click();
        driver.findElement(By.id("plugin-DEPtab-4")).click();
        driver.findElement(By.cssSelector("button[data-stationcode='MFM']")).click();

        // 到达地点
        Thread.sleep(1000);
        driver.findElement(By.id("spanArrivalDesc")).click();
        driver.findElement(By.id("plugin-ARRtab-3")).click();
        WebElement element = driver.findElement(By.xpath("//div[@aria-labelledby='plugin-ARRtab-3']"));
        element.findElements(By.className("choise")).get(0).click();

        // 出发时间
        driver.findElement(By.id("btnDatePicker")).click();

        //获取当前日期
        DateTime now = DateTime.now();
        DateTime tomorrow = DateUtil.offsetDay(now, 1);
        String dateStr = tomorrow.toString("yyyyMMdd");

        // 日历组件
        Thread.sleep(1000);
        List<WebElement> oneDateList = driver.findElements(By.xpath("//span[@aria-label=" + dateStr+"]"));
        for (WebElement oneDate : oneDateList) {
            String oneDateClassStr = oneDate.getAttribute("class");
            // 避开被隐藏的数据块
            if (!oneDateClassStr.contains("hidden")) {
                oneDate.click();
                break;
            }
        }

        // 确认出发时间
        Thread.sleep(1000);
        WebElement selectDate = driver.findElement(By.xpath("//*[@id=\"dateLayer\"]/div[2]/div[3]/button[3]"));
        selectDate.getText();
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", selectDate);

        // 搜索
        Thread.sleep(1000);
        driver.findElement(By.id("searchFlight")).click();

        // 最大尝试次数
        int maxAttempts = 15;
        int attempts = 0;
        while (attempts < maxAttempts) {
            try {
                // 尝试找到元素
                driver.findElement(By.className("air-flight-list"));
                break;
            } catch (org.openqa.selenium.NoSuchElementException e) {
                // 元素未找到的异常处理

                // 刷新页面
                Thread.sleep(1000);
                driver.navigate().refresh();

                // 增加尝试次数
                attempts++;
                log.info("第" + attempts + "次查询航班信息");
            }
        }
        if (attempts == maxAttempts) {
            log.error("超过最大尝试次数");
            return "";
        }

        grab(driver);


        // 获取第二天的数据
        WebElement nextDay = driver.findElement(By.cssSelector("[class = 'air-flight-slide swiper-slide swiper-slide-next']"));
        nextDay.click();


        String pageSource = driver.getPageSource();
        driver.quit();
        return pageSource;
    }

    private static void grab(WebDriver driver) {
        // 航班日期报价列表
        WebElement airFlightList = driver.findElement(By.className("air-flight-list"));
        WebElement active = airFlightList.findElement(By.cssSelector("[class = 'air-flight-slide swiper-slide swiper-slide-active active']"));
        String date = active.findElement(By.className("date")).getText();
        String unit = active.findElement(By.className("unit")).getText();
        String priceTxt = active.findElement(By.className("price_txt")).getText();
        String priceStr = StrUtil.cleanBlank(priceTxt).replaceAll(",", "");
        int price = Integer.parseInt(priceStr);
        log.info("日期：" + date + " " + unit + " " + price);

        // 机票信息列表
        WebElement fareList = driver.findElement(By.className("fare-list"));
        WebElement chip = fareList.findElement(By.cssSelector("[class = 'chip lowest']"));
        WebElement chips = chip.findElement(By.xpath(".."));
        WebElement head = chips.findElement(By.xpath(".."));
        WebElement listSummary = head.findElement(By.xpath(".."));
        WebElement listItem = listSummary.findElement(By.xpath(".."));

        // 航班号名称
        WebElement tkNum = listSummary.findElement(By.className("tk-num"));
        String tkNumText = tkNum.getText();
        log.info("航班号：" + tkNumText);

        // 开始时间
        WebElement departureTime = listSummary.findElement(By.cssSelector("[class = 'time-num start']"));
        String departureTimeText = departureTime.getText();
        String departureDate = departureTime.getAttribute("data-gmt");
        log.info("出发时间：" + departureDate + " " + departureTimeText);

        // 持续时间
        WebElement duration = listSummary.findElement(By.cssSelector("[class = 'moving-time']"));
        String durationText = duration.getText();
        log.info("持续时间：" + durationText);

        // 结束时间
        WebElement arrivalTime = listSummary.findElement(By.cssSelector("[class = 'time-num target']"));
        String arrivalTimeText = arrivalTime.getText();
        String arrivalDate = arrivalTime.getAttribute("data-landingdate");
        log.info("到达时间：" + arrivalDate + " " + arrivalTimeText);

        // 机票类型
        WebElement farePareTab = listItem.findElement(By.className("fare-pare-tab"));
        List<WebElement> gradeBags = farePareTab.findElements(By.cssSelector("[class = 'tab-btn grade-bag']"));
        gradeBags.forEach(gradeBag -> {
            // 舱位等级
            String cabinClass = gradeBag.findElement(By.cssSelector("[class = 'grade fly-bag']")).getText();
            log.info("舱位等级：" + cabinClass);

            // 剩余座位
            String remainingSeat = gradeBag.findElement(By.cssSelector("[class = 'remaining-seat']")).getText();
            log.info("剩余座位：" + remainingSeat);
        });
    }


    public static void main(String[] args){
        DateTime now = DateTime.now();
        String dateStr = now.toString("yyyyMMdd");
        System.out.println(dateStr);
    }
}

package com.airlines.service.impl;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.airlines.service.JejuAirService;
import com.airlines.util.SeleniumUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Slf4j
public class JejuAirServiceImpl implements JejuAirService {

    public static void main(String[] args) throws InterruptedException {
        getGrabData();
    }

    private static void getGrabData() throws InterruptedException {
        DateTime start = DateTime.now();
        log.info("=============== 开始抓取7C航空 ==================" + start);

        String url = "https://www.jejuair.net/zh-cn/main/base/index.do";
        WebDriver driver = SeleniumUtil.getWebDriver();
        driver.get(url);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().maximize();

        String dateStr = getDateStr();
        log.info("------------- " + dateStr + "开始抓取 -------------");

        try {
            mockClick(driver, dateStr);
        } catch (NoSuchElementException e) {
            log.info("没有找到元素" + e.getMessage());
        } finally {
            driver.quit();
            // 打印出时间耗时
            log.info("=============== 抓取7C航空耗时 ==================" + DateUtil.formatBetween(start, DateTime.now(),BetweenFormatter.Level.MINUTE));
        }
    }

    private static void mockClickNextday(WebDriver driver) throws InterruptedException, NoSuchElementException {
        Thread.sleep(1000);
        WebElement nextDay = driver.findElement(By.cssSelector("[class = 'air-flight-slide swiper-slide swiper-slide-next']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextDay);
        Thread.sleep(3000);

        refreshResult(driver);
    }

    private static String getDateStr() {
        DateTime now = DateTime.now();
        return now.toString("yyyyMMdd");
    }

    private static void mockClick(WebDriver driver, String dateStr) throws InterruptedException, NoSuchElementException {
        // 点击单程
        Thread.sleep(1000);
        driver.findElement(By.xpath("//li[@data-triptype='OW']")).click();

        mockStationAndDate(driver, dateStr);
    }

    private static void mockStationAndDate(WebDriver driver, String dateStr) throws InterruptedException, NoSuchElementException {

        // 获取出发地点CODE列表
        List<String> departureStationCodeList = getStationCodeList(driver, "DEP");
        log.info("出发地点CODE列表：" + departureStationCodeList);

        for (String departureStationCode : departureStationCodeList.subList(0,2)) {
            log.info("出发地点CODE：" + departureStationCode);
            mockClickDepartureStation(driver, departureStationCode);

            // 获取到达地点CODE列表
            List<String> arrivalStationCodeList = getArrivalStationCodeList(driver, departureStationCode);
            for (String arrivalStationCode : arrivalStationCodeList.subList(0,2)) {
                mockClickArrivalStation(driver, departureStationCode, arrivalStationCode);
                mockDateAndSelectClick(driver, dateStr);
                grab(driver);
                // 需要往后爬几天，就在这里循环几次
                for (int i = 1; i < 5; i++) {
                    log.info("-------------" + DateUtil.offsetDay(DateUtil.date(), i).toString("yyyyMMdd") + "-------------");
                    mockClickNextday(driver);
                    grab(driver);
                }
            }
        }
    }

    private static void mockClickArrivalStation(WebDriver driver, String departureStationCode, String arrivalStationCode) throws InterruptedException {
        // 选择到达地点CODE
        driver.findElement(By.id("spanArrivalDesc")).click();
        Thread.sleep(1000);
        List<WebElement> arrivalStations = driver.findElements(By.cssSelector("button[data-stationcode=" + arrivalStationCode + "][data-stationtype='ARR']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", arrivalStations.get(0));
        log.info("==============================");
        log.info("出发地点CODE：" + departureStationCode +" -- 到达地点CODE：" + arrivalStationCode);
    }

    private static void mockClickDepartureStation(WebDriver driver, String departureStationCode) throws InterruptedException {
        // 选择出发地点CODE
        driver.findElement(By.id("spanDepartureDesc")).click();
        Thread.sleep(1000);
        WebElement departureStation = driver.findElement(By.cssSelector("button[data-stationcode = " + departureStationCode + "][data-stationtype='DEP']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", departureStation);
    }

    private static List<String> getArrivalStationCodeList(WebDriver driver, String departureStationCode) throws InterruptedException {
        Thread.sleep(1000);
        List<String> arrivalStationCodeList = getStationCodeList(driver, "ARR");

        // 过滤掉出发地点CODE
        arrivalStationCodeList = arrivalStationCodeList.stream().filter(arrivalStationCode -> !arrivalStationCode.equals(departureStationCode)).toList();

        log.info("到达地点CODE列表：" + arrivalStationCodeList);
        return arrivalStationCodeList;
    }

    /**
     * 获取地点列表,这里的列表内容是动态的，不同的出发地点对应不同的到达地点
     *
     * @param driver 浏览器内容
     * @return 地点CODE列表
     */
    private static List<String> getStationCodeList(WebDriver driver, String status) throws NoSuchElementException, InterruptedException {

        WebElement statusElement;
        if (status.equals("DEP")) {
            driver.findElement(By.id("spanDepartureDesc")).click();
            Thread.sleep(1000);
            statusElement = driver.findElement(By.id("divDepArea"));
        } else {
            driver.findElement(By.id("spanArrivalDesc")).click();
            Thread.sleep(1000);
            statusElement = driver.findElement(By.id("divArrArea"));
        }
        // divArrArea的上上级
        WebElement statusElementParent = statusElement.findElement(By.xpath(".."));
        statusElementParent = statusElementParent.findElement(By.xpath(".."));

        List<WebElement> departureStationList = statusElementParent.findElements(By.cssSelector("button[data-stationcode]"));

        List<String> departureStationCodeList = new ArrayList<>();
        for (WebElement station : departureStationList) {
            String stationCode = station.getAttribute("data-stationcode");
            departureStationCodeList.add(stationCode);
        }
        return departureStationCodeList;
    }

    private static void mockDateAndSelectClick(WebDriver driver, String dateStr) throws InterruptedException {
        // 选择日历组件
        Thread.sleep(1000);
        List<WebElement> oneDateList = driver.findElements(By.xpath("//span[@aria-label=" + dateStr + "]"));
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
        WebElement searchFlight = driver.findElement(By.id("searchFlight"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchFlight);
        Thread.sleep(3000);

        refreshPage(driver);
    }

    private static void refreshPage(WebDriver driver) throws InterruptedException {
        // 最大尝试次数
        int maxAttempts = 15;
        int attempts = 0;
        while (attempts < maxAttempts) {
            try {
                // 尝试找到有效的机票信息列表
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
    }

    private static void refreshResult(WebDriver driver) throws InterruptedException {
        // 最大尝试次数
        int maxAttempts = 15;
        int attempts = 0;
        while (attempts < maxAttempts) {

            try {
                // 尝试找到有效的机票信息列表
                driver.findElement(By.cssSelector("[class = 'air-flight-slide swiper-slide swiper-slide-active active']"));
                break;
            } catch (org.openqa.selenium.NoSuchElementException e) {

                // 元素未找到的异常处理：重新点击日期标签
                mockReclick(driver);

                // 增加尝试次数
                attempts++;
                log.info("第" + attempts + "次重新点击日期标签");
            }
        }
    }

    private static void mockReclick(WebDriver driver) throws InterruptedException {
        List<WebElement> activeNotList = driver.findElements(By.cssSelector("[class = 'air-flight-slide swiper-slide swiper-slide-active']"));
        if (activeNotList.isEmpty()) {
            return;
        }
        WebElement button = activeNotList.get(0);
        Thread.sleep(1000);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
    }

    private static void grab(WebDriver driver) throws InterruptedException,NoSuchElementException {
        // 航班日期报价列表
        String departureCity = driver.findElement(By.id("spanDepartureDesc")).getText();
        String arrivalCity = driver.findElement(By.id("spanArrivalDesc")).getText();
        log.info(departureCity + " -- " + arrivalCity);

        WebElement active = driver.findElement(By.cssSelector("[class = 'air-flight-slide swiper-slide swiper-slide-active active']"));
        String date = active.findElement(By.className("date")).getText();

        String price = active.findElement(By.className("price")).getText();
        if ("-".equals(price)) {
            // 表示这一天没有航班信息
            log.info("日期：" + date + " 没有航班信息");
            return;
        } else {
            log.info("日期：" + date);
        }

        String unit = active.findElement(By.className("unit")).getText();
        String priceTxt = active.findElement(By.className("price_txt")).getText();
        String priceStr = StrUtil.cleanBlank(priceTxt).replaceAll(",", "");
        int priceNum = Integer.parseInt(priceStr);
        log.info("价格：" + priceNum + " " + unit);

        // 最低价机票信息栏
        List<WebElement> fareList = driver.findElements(By.className("fare-list"));
        if (fareList.isEmpty()) {
            log.info("没有最低价机票信息");
            return;
        }
        WebElement chip = fareList.get(0).findElement(By.cssSelector("[class = 'chip lowest']"));
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

        // 将departureTimeText通过换行符拆分为两个字符串
        if (departureTimeText.contains("\n")) {
            String[] departureTimeTextArr = departureTimeText.split("\n");
            departureTimeText = departureTimeTextArr[0];
            String departureTimeText2 = departureTimeTextArr[1];
            log.info("出发地点：" + departureTimeText2);
        }

        log.info("出发时间：" + departureTimeText);

        // 结束时间
        WebElement arrivalTime = listSummary.findElement(By.cssSelector("[class = 'time-num target']"));
        String arrivalTimeText = arrivalTime.getText();
        if (arrivalTimeText.contains("\n")) {
            String[] arrivalTimeTextArr = arrivalTimeText.split("\n");
            arrivalTimeText = arrivalTimeTextArr[0];
            String arrivalTimeText2 = arrivalTimeTextArr[1];
            log.info("到达地点：" + arrivalTimeText2);
        }
        log.info("到达时间：" + arrivalTimeText);


        // 持续时间
        WebElement durationTotal = listSummary.findElement(By.cssSelector("[class = 'moving-time']"));
        String durationText = durationTotal.getText();
        log.info("持续时间：" + durationText);

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


        List<WebElement> transferFlag = listSummary.findElements(By.cssSelector("[onclick = 'openConnectSection(this);']"));
        if (transferFlag.isEmpty()) {
            // 不需要中转
            log.info("不需要中转");
        } else {
            // 中转详情
            WebElement detail = listSummary.findElement(By.cssSelector("[onclick = 'openConnectSection(this);']"));
            log.info("中转次数: " + detail.getText());

            mockTransferClick((JavascriptExecutor) driver, listSummary);

            WebElement flightList = driver.findElement(By.className("flight-time__list"));

            // 起飞列表
            List<WebElement> takeoffList = flightList.findElements(By.cssSelector("[class = 'flight-time__list-item']"));
            for (WebElement takeoff : takeoffList) {
                // 记录这是第几次循环
                int index = takeoffList.indexOf(takeoff) + 1;
                log.info("第" + index + "次行程");

                String takeoffCity = takeoff.findElement(By.className("title")).getText();
                String takeoffStation = takeoff.findElement(By.className("title-code")).getText();
                log.info("起飞城市：" + takeoffCity + " 起飞机场：" + takeoffStation);
                WebElement info = takeoff.findElement(By.className("info"));
                List<WebElement> spans = info.findElements(By.cssSelector("span"));
                String takeoffTime = spans.get(0).getText();
                String flightNumber = spans.get(1).getText();
                String duration = takeoff.findElement(By.className("time")).getText();
                log.info("起飞时间：" + takeoffTime + " 航班号：" + flightNumber + " 持续时间：" + duration);
            }

            // 落地列表
            List<WebElement> landingList = flightList.findElements(By.cssSelector("[class = 'flight-time__list-item via-line']"));
            for (WebElement landing : landingList) {
                // 记录这是第几次循环
                int index = landingList.indexOf(landing) + 1;
                log.info("第" + index + "次行程");

                String takeoffCity = landing.findElement(By.className("title")).getText();
                String takeoffStation = landing.findElement(By.className("title-code")).getText();
                log.info("落地城市：" + takeoffCity + " 落地机场：" + takeoffStation);
                WebElement info = landing.findElement(By.className("info"));
                List<WebElement> spans = info.findElements(By.cssSelector("span"));
                String takeoffTime = spans.get(0).getText();
                log.info("落地时间：" + takeoffTime);
            }

            // 转机等候时间
            WebElement via = flightList.findElement(By.cssSelector("[class = 'flight-time__list-item via-line via']"));
            log.info("转机等候时间：" + via.getText());

            // 关闭详情
            mockCloseClick(driver);
        }
    }

    private static void mockCloseClick(WebDriver driver) {
        List<WebElement> closes = driver.findElements(By.className("modal__close"));
        closes.get(closes.size() - 1).click();
    }

    private static void mockTransferClick(JavascriptExecutor driver, WebElement listSummary) throws InterruptedException {
        Thread.sleep(1000);
        WebElement detail = listSummary.findElement(By.cssSelector("[onclick = 'openConnectSection(this);']"));
        driver.executeScript("arguments[0].click();", detail);
        Thread.sleep(1000);
    }

}

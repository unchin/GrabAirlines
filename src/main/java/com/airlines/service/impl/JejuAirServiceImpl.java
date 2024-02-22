package com.airlines.service.impl;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.airlines.service.JejuAirService;
import com.airlines.util.SeleniumUtil;
import com.heytrip.common.domain.SearchAirticketsInput;
import com.heytrip.common.domain.SearchAirticketsInputSegment;
import com.heytrip.common.domain.SearchAirticketsPriceDetail;
import com.heytrip.common.domain.SearchAirticketsSegment;
import com.heytrip.common.enums.CabinClass;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: unchin
 * @description: JejuAirServiceImpl
 * @create: 2024-02-01 14:36
 */
@Service
@Slf4j
public class JejuAirServiceImpl implements JejuAirService {

    public static final int NEXT_DAY_NUM = 3;
    public static final String DEP = "DEP";
    public static final String NO_FLIGHT = "-";
    public static final String LINE_FEED = "\n";
    public static final String ADD_DAY = "+1日";


    public static void main(String[] args) throws InterruptedException {
        String dateStr = getDateStr();
        log.info("------------- " + dateStr + "开始抓取 -------------");
        try {
            getGrabData(dateStr);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        }
    }

    private static void getGrabData(String dateStr) throws InterruptedException {
        DateTime start = DateTime.now();
        log.info("=============== 开始抓取7C航空 ==================" + start);

        String url = "https://www.jejuair.net/zh-cn/main/base/index.do";
        WebDriver driver = SeleniumUtil.getWebDriver();
        driver.get(url);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().maximize();

        try {
            mockClick(driver, dateStr);
        } catch (NoSuchElementException e) {
            log.info("没有找到元素" + e.getMessage());
        } finally {
            driver.quit();
            // 打印出时间耗时
            log.info("=============== 抓取7C航空耗时 ==================" + DateUtil.formatBetween(start, DateTime.now(), BetweenFormatter.Level.MINUTE));
        }
    }

    private static boolean mockClickNextday(WebDriver driver) throws InterruptedException, NoSuchElementException {
        Thread.sleep(1000);
        WebElement nextDay = driver.findElement(By.cssSelector("[class = 'air-flight-slide swiper-slide swiper-slide-next']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextDay);
        Thread.sleep(3000);

        return refreshResult(driver);
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

        for (String departureStationCode : departureStationCodeList) {
            log.info("出发地点CODE：" + departureStationCode);
            mockClickDepartureStation(driver, departureStationCode);

            // 获取到达地点CODE列表
            List<String> arrivalStationCodeList = getArrivalStationCodeList(driver, departureStationCode);
            for (String arrivalStationCode : arrivalStationCodeList) {
                mockClickArrivalStation(driver, departureStationCode, arrivalStationCode);
                boolean flag = mockDateAndSelectClick(driver, dateStr);
                // 如果 flag 为 false，说明没有航班，直接跳过
                if (!flag) {
                    continue;
                }
                grab(driver);
                // 需要往后爬几天，就在这里循环几次
                for (int i = 1; i < NEXT_DAY_NUM; i++) {
                    log.info("-------------" + DateUtil.offsetDay(DateUtil.date(), i).toString("yyyyMMdd") + "-------------");
                    boolean b = mockClickNextday(driver);
                    // 如果 b 为 false，说明没有航班，直接跳过
                    if (!b) {
                        continue;
                    }
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
        log.info("出发地点CODE：" + departureStationCode + " -- 到达地点CODE：" + arrivalStationCode);
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
        if (DEP.equals(status)) {
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
        // 将departureStationCodeList的顺序打乱
        Collections.shuffle(departureStationCodeList);
        return departureStationCodeList;
    }

    private static boolean mockDateAndSelectClick(WebDriver driver, String dateStr) throws InterruptedException {
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

        return refreshPage(driver);
    }

    private static boolean refreshPage(WebDriver driver) {
        // 最大尝试次数
        int maxAttempts = 15;
        int attempts = 0;
        while (attempts < maxAttempts) {
            List<WebElement> list = driver.findElements(By.className("air-flight-list"));
            if (!list.isEmpty()) {
                break; // 成功找到元素，无需刷新
            }
            // 刷新页面
            driver.navigate().refresh();
            attempts++;
            log.info("第" + attempts + "次查询航班信息");
        }
        return attempts != maxAttempts;
    }

    private static boolean refreshResult(WebDriver driver) throws InterruptedException {
        // 最大尝试次数
        int maxAttempts = 15;
        int attempts = 0;
        while (attempts < maxAttempts) {

            // 尝试找到有效的机票信息列表
            List<WebElement> list = driver.findElements(By.cssSelector("[class = 'air-flight-slide swiper-slide swiper-slide-active active']"));
            if (!list.isEmpty()) {
                break;// 成功找到元素，无需刷新
            }
            // 元素未找到的异常处理：重新点击日期标签
            mockReclick(driver);
            // 增加尝试次数
            attempts++;
            log.info("第" + attempts + "次重新点击日期标签");
        }
        return attempts != maxAttempts;
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

    private static SearchAirticketsSegment grab(WebDriver driver) throws InterruptedException, NoSuchElementException {

        if (!checkActive(driver)) {
            return null;
        }

        SearchAirticketsSegment result = new SearchAirticketsSegment();
        result.setCarrier("7C");
        result.setCabin("Y");
        result.setCabinClass(CabinClass.EconomyClass.getType());

        String departureCity = driver.findElement(By.id("spanDepartureDesc")).getText();
        String arrivalCity = driver.findElement(By.id("spanArrivalDesc")).getText();
        log.info(departureCity + " -- " + arrivalCity);

        String depDateStr = getDepDate(driver);
        log.info("日期：" + LocalDate.parse(depDateStr, DateTimeFormatter.ofPattern("yyyy.MM.dd")));




        WebElement active = driver.findElement(By.cssSelector("[class = 'air-flight-slide swiper-slide swiper-slide-active active']"));
        String unit = active.findElement(By.className("unit")).getText();
        String priceTxt = active.findElement(By.className("price_txt")).getText();
        String priceStr = StrUtil.cleanBlank(priceTxt).replaceAll(",", "");
        BigDecimal priceNum = new BigDecimal(priceStr);
        log.info("价格：" + priceNum + " " + unit);

        List<WebElement> fareList = driver.findElements(By.className("fare-list"));
        List<WebElement> chipList = fareList.get(0).findElements(By.cssSelector("[class = 'chip lowest']"));
        WebElement chips = chipList.get(0).findElement(By.xpath(".."));
        WebElement head = chips.findElement(By.xpath(".."));
        WebElement listSummary = head.findElement(By.xpath(".."));

        String tkNumText = getTkNumText(listSummary);
        log.info("航班号：" + tkNumText);
        result.setFlightNumber(tkNumText);

        // 开始时间
        WebElement departureTime = listSummary.findElement(By.cssSelector("[class = 'time-num start']"));
        String departureTimeText = departureTime.getText();

        // 将departureTimeText通过换行符拆分为两个字符串
        if (departureTimeText.contains(LINE_FEED)) {
            String[] departureTimeTextArr = departureTimeText.split(LINE_FEED);
            departureTimeText = departureTimeTextArr[0];
            String departureTimeText2 = departureTimeTextArr[1];
            log.info("出发地点：" + departureTimeText2);
        }


        String depDateTimeStr = depDateStr + departureTimeText;
        LocalDateTime depDateTime = LocalDateTime.parse(depDateTimeStr, DateTimeFormatter.ofPattern("yyyy.MM.ddHH:mm"));
        log.info("出发时间：" + depDateTime);
        result.setDepDate(depDateTime);

        // 结束时间
        WebElement arrivalTime = listSummary.findElement(By.cssSelector("[class = 'time-num target']"));
        String arrivalTimeText = arrivalTime.getText();
        if (arrivalTimeText.contains(LINE_FEED)) {
            String[] arrivalTimeTextArr = arrivalTimeText.split(LINE_FEED);
            arrivalTimeText = arrivalTimeTextArr[0];
            String arrivalTimeText2 = arrivalTimeTextArr[1];
            log.info("到达地点：" + arrivalTimeText2);
        }
        LocalDateTime arrDateTime;
        if (arrivalTimeText.contains(ADD_DAY)) {
            arrivalTimeText = arrivalTimeText.replace(ADD_DAY, "");
            String arrDate = depDateStr + arrivalTimeText;
            arrDateTime = LocalDateTime.parse(arrDate, DateTimeFormatter.ofPattern("yyyy.MM.ddHH:mm"));
            arrDateTime = arrDateTime.plusDays(1);
        } else {
            String arrDate = depDateStr + arrivalTimeText;
            arrDateTime = LocalDateTime.parse(arrDate, DateTimeFormatter.ofPattern("yyyy.MM.ddHH:mm"));
        }
        log.info("到达时间：" + arrDateTime);
        result.setArrDate(arrDateTime);

        // 持续时间
        WebElement durationTotal = listSummary.findElement(By.cssSelector("[class = 'moving-time']"));
        String durationText = durationTotal.getText();
        if (durationText.contains(LINE_FEED)) {
            String[] arrivalTimeTextArr = durationText.split(LINE_FEED);
            durationText = arrivalTimeTextArr[0];
        }
        log.info("持续时间：" + durationText);

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

            // 经停地code
            String viaStation = landingList.get(0).findElement(By.className("title-code")).getText();
            result.setStopCities(viaStation);

            // 关闭详情
            mockCloseClick(driver);
        }
        return result;
    }

    private static boolean checkActive(WebDriver driver) {
        WebElement active = driver.findElement(By.cssSelector("[class = 'air-flight-slide swiper-slide swiper-slide-active active']"));
        String price = active.findElement(By.className("price")).getText();
        if (NO_FLIGHT.equals(price)) {
            log.info("没有航班信息");
            return false;
        }

        List<WebElement> fareList = driver.findElements(By.className("fare-list"));
        if (fareList.isEmpty()) {
            log.info("没有最低价机票信息");
            return false;
        }
        List<WebElement> chipList = fareList.get(0).findElements(By.cssSelector("[class = 'chip lowest']"));
        if (chipList.isEmpty()) {
            log.info("没有最低价机票信息");
            return false;
        }
        return true;
    }

    private static String getDepDate(WebDriver driver) {
        String btnDatePicker = driver.findElement(By.id("btnDatePicker")).getText();
        String departureTimeText10 = btnDatePicker.substring(0, 10);
        return departureTimeText10;
    }

    private static String getTkNumText(WebElement listSummary) {
        // 航班号名称
        WebElement tkNum = listSummary.findElement(By.className("tk-num"));
        String tkNumText = tkNum.getText();
        return tkNumText;
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

    @Override
    public SearchAirticketsPriceDetail searchAirticketsPriceDetail(SearchAirticketsInput searchAirticketsInput) {
        DateTime start = DateTime.now();

        Integer adultNum = searchAirticketsInput.getAdultNum();
        // 最多9个成人
        if (adultNum > 9) {
            adultNum = 9;
        }

        Integer childNum = searchAirticketsInput.getChildNum();
        // 最多9个儿童
        if (childNum > 9) {
            childNum = 9;
        }

        Integer tripType = searchAirticketsInput.getTripType();
        List<SearchAirticketsInputSegment> fromSegments = searchAirticketsInput.getFromSegments();
        List<SearchAirticketsInputSegment> retSegments = searchAirticketsInput.getRetSegments();

        List<SearchAirticketsSegment> fromSegmentsList = getFromSegmentsList(fromSegments);
        SearchAirticketsPriceDetail result = new SearchAirticketsPriceDetail();
        result.setRateCode(UUID.fastUUID().toString());
        result.setFromSegments(fromSegmentsList);

        return result;
    }

    private static List<SearchAirticketsSegment> getFromSegmentsList(List<SearchAirticketsInputSegment> fromSegments) {
        List<SearchAirticketsSegment> fromSegmentsList = new ArrayList<>();
        fromSegments.forEach(fromSegment -> {
            String depCityCode = fromSegment.getDepCityCode();
            String arrCityCode = fromSegment.getArrCityCode();
            LocalDate depDate = fromSegment.getDepDate();

            // 将depDate格式化
            String dateStr = depDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            String url = "https://www.jejuair.net/zh-cn/main/base/index.do";
            WebDriver driver = SeleniumUtil.getWebDriver();
            driver.get(url);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            driver.manage().window().maximize();

            try {
                // 点击单程
                Thread.sleep(1000);
                driver.findElement(By.xpath("//li[@data-triptype='OW']")).click();

                mockClickDepartureStation(driver, depCityCode);
                mockClickArrivalStation(driver, depCityCode, arrCityCode);
                mockDateAndSelectClick(driver, dateStr);
                SearchAirticketsSegment searchAirticketsSegment = grab(driver);
                searchAirticketsSegment.setDepAirport(depCityCode);
                searchAirticketsSegment.setArrAirport(arrCityCode);
                fromSegmentsList.add(searchAirticketsSegment);
            } catch (NoSuchElementException | InterruptedException e) {
                log.info("没有找到元素" + e.getMessage());
            } finally {
                driver.quit();
            }
        });
        return fromSegmentsList;
    }
}

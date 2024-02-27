package com.airlines.service.impl;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.airlines.service.JejuAirService;
import com.airlines.util.SeleniumUtil;
import com.heytrip.common.domain.*;
import com.heytrip.common.enums.CabinClass;
import com.heytrip.common.enums.CurrencyEnums;
import lombok.SneakyThrows;
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
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
//                mockDate(driver, dateStr);
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

    /**
     * 选择到达地点CODE
     *
     * @param driver               浏览器驱动
     * @param departureStationCode 出发地点CODE
     * @param arrivalStationCode   到达地点CODE
     * @return 是否成功选择到达地点CODE
     */
    private static boolean mockClickArrivalStation(WebDriver driver, String departureStationCode, String arrivalStationCode) throws InterruptedException {
//        driver.findElement(By.id("spanArrivalDesc")).click();
        List<WebElement> arrivalStations = driver.findElements(By.cssSelector("button[data-stationcode=" + arrivalStationCode + "][data-stationtype='ARR']"));
        if (arrivalStations.isEmpty()) {
            log.info("没有找到到达地点CODE：" + arrivalStationCode);
            return false;
        }
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", arrivalStations.get(0));
        Thread.sleep(1000);
        log.info("==============================");
        log.info("出发地点CODE：" + departureStationCode + " -- 到达地点CODE：" + arrivalStationCode);
        return true;
    }
    private static boolean mockClickMultiArrivalStation(WebDriver driver, String departureStationCode, String arrivalStationCode) throws InterruptedException {
//        driver.findElement(By.id("spanMultiArrivalDesc")).click();
        List<WebElement> arrivalStations = driver.findElements(By.cssSelector("button[data-stationcode=" + arrivalStationCode + "][data-stationtype='ARR']"));
        if (arrivalStations.isEmpty()) {
            log.info("没有找到到达地点CODE：" + arrivalStationCode);
            return false;
        }
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", arrivalStations.get(0));
        Thread.sleep(1000);
        log.info("==============================");
        log.info("出发地点CODE：" + departureStationCode + " -- 到达地点CODE：" + arrivalStationCode);
        return true;
    }

    private static void mockClickDepartureStation(WebDriver driver, String departureStationCode) throws InterruptedException {
        // 选择出发地点CODE
        driver.findElement(By.id("spanDepartureDesc")).click();
        WebElement departureStation = driver.findElement(By.cssSelector("button[data-stationcode = " + departureStationCode + "][data-stationtype='DEP']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", departureStation);
        Thread.sleep(1000);
    }
    private static void mockClickMutiDepartureStation(WebDriver driver, String departureStationCode) throws InterruptedException {
        // 选择出发地点CODE
//        driver.findElement(By.id("spanMultiDepartureDesc")).click();
        WebElement departureStation = driver.findElement(By.cssSelector("button[data-stationcode = " + departureStationCode + "][data-stationtype='DEP']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", departureStation);
        Thread.sleep(1000);
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

    private static void mockDate(WebDriver driver, String dateStr) throws InterruptedException {

        WebElement currencyElement = driver.findElement(By.name("currency"));
        WebElement infoWrap = currencyElement.findElement(By.xpath(".."));
        WebElement picker = infoWrap.findElement(By.xpath(".."));

        // 选择日历组件
        List<WebElement> oneDateList = picker.findElements(By.xpath("//span[@aria-label=" + dateStr + "]"));

        for (WebElement oneDate : oneDateList) {
            String oneDateClassStr = oneDate.getAttribute("class");
            // 避开被隐藏的数据块
            if (!oneDateClassStr.contains("hidden")) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", oneDate);
                Thread.sleep(1000);
                break;
            }
        }
    }

    private static void confirmDepDate(WebDriver driver) {
        // 确认出发时间
        WebElement selectDate = driver.findElement(By.xpath("//*[@id=\"dateLayer\"]/div[2]/div[3]/button[3]"));
        selectDate.getText();
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", selectDate);
    }

    private static boolean mockSelectClick(WebDriver driver) {
        // 搜索
        WebElement searchFlight = driver.findElement(By.id("searchFlight"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchFlight);
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

    private static SearchAirticketsSegment grab(WebDriver driver) {

        if (!checkActive(driver)) {
            return null;
        }

        SearchAirticketsSegment result = new SearchAirticketsSegment();
        result.setCarrier("7C");
        result.setCabin("Y");
        result.setCabinClass(CabinClass.EconomyClass.getType());
        result.setCodeShare(false);
        result.setArrAirport("");

        String departureCity = driver.findElement(By.id("spanDepartureDesc")).getText();
        String arrivalCity = driver.findElement(By.id("spanArrivalDesc")).getText();
        log.info(departureCity + " -- " + arrivalCity);

        String depDateStr = getDepDate(driver);
        log.info("日期：" + LocalDate.parse(depDateStr, DateTimeFormatter.ofPattern("yyyy.MM.dd")));

        WebElement listSummary = getListSummary(driver);
        String tkNumText = getTkNumText(listSummary);
        log.info("航班号：" + tkNumText);
        result.setFlightNumber(tkNumText);

        LocalDateTime depDateTime = getDepDateTime(listSummary, depDateStr);
        log.info("出发时间：" + depDateTime);
        result.setDepDate(depDateTime);

        LocalDateTime arrDateTime = getArrDateTime(listSummary, depDateStr);
        log.info("到达时间：" + arrDateTime);
        result.setArrDate(arrDateTime);

        String durationText = getDurationText(listSummary);
        log.info("持续时间：" + durationText);

        List<WebElement> transferFlag = listSummary.findElements(By.cssSelector("[onclick = 'openConnectSection(this);']"));
        if (transferFlag.isEmpty()) {
            log.info("不需要中转");
        } else {
            String viaStation = getViaStation(driver, listSummary);
            result.setStopCities(viaStation);
            mockCloseClick(driver);
        }

        BaggageRule baggageRule = getBaggageRule(driver, listSummary);
        result.setBaggageRule(baggageRule);

        return result;
    }

    private static BaggageRule getBaggageRule(WebDriver driver, WebElement listSummary) {
        // 托运行李规则
        BaggageRule baggageRule = new BaggageRule();
        baggageRule.setHasBaggage(false);

        WebElement listItem = listSummary.findElement(By.xpath(".."));
        WebElement farePareTab = listItem.findElement(By.className("fare-pare-tab"));
        List<WebElement> farePareTabList = farePareTab.findElements(By.className("tab-btn-in"));

        farePareTabList.removeIf(webElement -> webElement.findElement(By.xpath("..")).getAttribute("class").contains("tab-btn sold-out"));

        if (farePareTabList.size() == 1){
            String text1 = farePareTabList.get(0).getText();
            String attribute = farePareTabList.get(0).getAttribute("class");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", farePareTabList.get(0));
        }else {
            String text2 = farePareTabList.get(0).getText();
            String attribute2 = farePareTabList.get(0).getAttribute("class");
            farePareTabList = farePareTabList.stream().filter(webElement -> webElement.findElement(By.xpath("..")).getAttribute("class").contains("tab-btn grade-default")).toList();
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", farePareTabList.get(0));
        }

        List<WebElement> benefitList = listItem.findElements(By.className("benefit-list-item"));
        if (!benefitList.isEmpty()) {
            for (WebElement benefit : benefitList) {
                String benefitText = benefit.getText();
                if (benefitText.contains("托运行李")) {
                    String regEx = "[^0-9]";
                    Pattern p = Pattern.compile(regEx);
                    Matcher m = p.matcher(benefitText);
                    String s = m.replaceAll("").trim();
                    // s 转成数字
                    int i = Integer.parseInt(s);
                    baggageRule.setHasBaggage(true);
                    baggageRule.setBaggageKg(i);
                    baggageRule.setBaggagePiece(1);
                }
            }
        }
        return baggageRule;
    }


    private static String getViaStation(WebDriver driver, WebElement listSummary) {
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
        return viaStation;
    }

    private static String getDurationText(WebElement listSummary) {
        // 持续时间
        WebElement durationTotal = listSummary.findElement(By.cssSelector("[class = 'moving-time']"));
        String durationText = durationTotal.getText();
        if (durationText.contains(LINE_FEED)) {
            String[] arrivalTimeTextArr = durationText.split(LINE_FEED);
            durationText = arrivalTimeTextArr[0];
        }
        return durationText;
    }

    private static LocalDateTime getArrDateTime(WebElement listSummary, String depDateStr) {
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
        return arrDateTime;
    }

    private static LocalDateTime getDepDateTime(WebElement listSummary, String depDateStr) {
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
        return LocalDateTime.parse(depDateTimeStr, DateTimeFormatter.ofPattern("yyyy.MM.ddHH:mm"));
    }

    private static WebElement getListSummary(WebDriver driver) {
        List<WebElement> fareList = driver.findElements(By.className("fare-list"));

        // 去除掉fareList中getText()为空字符串的元素
        fareList.removeIf(element -> element.getText().isEmpty());
        List<WebElement> chipList = fareList.get(0).findElements(By.cssSelector("[class = 'chip lowest']"));
        log.info(chipList.get(0).getText());
        WebElement chips = chipList.get(0).findElement(By.xpath(".."));
        WebElement head = chips.findElement(By.xpath(".."));
        return head.findElement(By.xpath(".."));
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
        return btnDatePicker.substring(0, 10);
    }

    private static String getTkNumText(WebElement listSummary) {
        // 航班号名称
        WebElement tkNum = listSummary.findElement(By.className("tk-num"));
        String tkNumText = tkNum.getText();
        // 去掉tkNumText前面两位
        tkNumText = tkNumText.substring(2);
        // 如果tkNumText前面有0，则去掉所有的0
        tkNumText = tkNumText.replaceFirst("^0*", "");
        return tkNumText;
    }

    private static void mockCloseClick(WebDriver driver) {
        List<WebElement> closes = driver.findElements(By.className("modal__close"));
        closes.get(closes.size() - 1).click();
    }

    private static void mockTransferClick(JavascriptExecutor driver, WebElement listSummary) {
        WebElement detail = listSummary.findElement(By.cssSelector("[onclick = 'openConnectSection(this);']"));
        driver.executeScript("arguments[0].click();", detail);
    }

    @Override
    public List<SearchAirticketsPriceDetail> searchAirticketsPriceDetail(SearchAirticketsInput searchAirticketsInput) {

        String url = "https://www.jejuair.net/zh-cn/main/base/index.do";
        WebDriver driver = SeleniumUtil.getWebDriver();
        driver.get(url);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().maximize();

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

        List<SearchAirticketsSegment> fromSegmentsList = new ArrayList<>();
        Integer currency = null;
        BigDecimal price = null;

        Integer tripType = searchAirticketsInput.getTripType();
        try {
            // 单程
            if (tripType == 1) {
                // 点击单程
                driver.findElement(By.xpath("//li[@data-triptype='OW']")).click();
                Thread.sleep(1000);

                List<SearchAirticketsInputSegment> fromSegments = searchAirticketsInput.getFromSegments();
                //单程只会有一个航班参数，为了避免参数错误导致的代码运行错误，只取第一个航线参数
                SearchAirticketsInputSegment fromSegment = fromSegments.get(0);

                String depCityCode = fromSegment.getDepCityCode();
                String arrCityCode = fromSegment.getArrCityCode();
                LocalDate depDate = fromSegment.getDepDate();
                String dateStr = depDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

                mockClickDepartureStation(driver, depCityCode);
                boolean arrivalStation = mockClickArrivalStation(driver, depCityCode, arrCityCode);
                if (!arrivalStation) {
                    throw new RuntimeException(depCityCode + "到" + arrCityCode + "：该航线未开通");
                }


                price = getPrice(driver, dateStr);
                if (price == null) {
                    log.info("没有航班信息");
                    return null;
                }

                // 获取货币
                currency = getCurrency(driver);

                mockDate(driver, dateStr);
                confirmDepDate(driver);
                mockSelectClick(driver);
                SearchAirticketsSegment searchAirticketsSegment = grab(driver);
                searchAirticketsSegment.setDepAirport(depCityCode);
                searchAirticketsSegment.setArrAirport(arrCityCode);
                fromSegmentsList.add(searchAirticketsSegment);
            }
            // 往返
            if (tripType == 2) {
                // 去程的参数
                List<SearchAirticketsInputSegment> fromSegments = searchAirticketsInput.getFromSegments();
                SearchAirticketsInputSegment fromSegment = fromSegments.get(0);

                // 回程的参数
                List<SearchAirticketsInputSegment> retSegments = searchAirticketsInput.getRetSegments();
                SearchAirticketsInputSegment retSegment = retSegments.get(0);
                LocalDate retDate = retSegment.getDepDate();
                String retDateStr = retDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

                String depCityCode = fromSegment.getDepCityCode();
                String arrCityCode = fromSegment.getArrCityCode();
                LocalDate depDate = fromSegment.getDepDate();
                String depDateStr = depDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

                mockClickDepartureStation(driver, depCityCode);
                boolean arrivalStation = mockClickArrivalStation(driver, depCityCode, arrCityCode);
                if (!arrivalStation) {
                    throw new RuntimeException(depCityCode + "到" + arrCityCode + "：该航线未开通");
                }


                price = getPrice(driver, depDateStr);
                if (price == null) {
                    log.info("没有航班信息");
                    return null;
                }
                price = price.add(getPrice(driver, retDateStr));

                // 获取货币
                currency = getCurrency(driver);

                mockDate(driver, depDateStr);
                mockDate(driver, retDateStr);
                confirmDepDate(driver);
                mockSelectClick(driver);

                // 去程
                SearchAirticketsSegment searchAirticketsSegment = grab(driver);
                searchAirticketsSegment.setDepAirport(depCityCode);
                searchAirticketsSegment.setArrAirport(arrCityCode);
                fromSegmentsList.add(searchAirticketsSegment);

                // 回程
                WebElement btnAvailSch = driver.findElement(By.name("btnAvailSch"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnAvailSch);
                Thread.sleep(1000);
                SearchAirticketsSegment ret = grab(driver);
                ret.setDepAirport(arrCityCode);
                ret.setArrAirport(depCityCode);
                fromSegmentsList.add(ret);
            }
            // 多程（只能接受两程）
            if (tripType == 3) {
                driver.findElement(By.xpath("//li[@data-triptype='MT']")).click();
                Thread.sleep(1000);

                // 第一段行程的参数
                List<SearchAirticketsInputSegment> fromSegments = searchAirticketsInput.getFromSegments();
                SearchAirticketsInputSegment fromSegment = fromSegments.get(0);
                LocalDate depDate = fromSegment.getDepDate();
                String depDateStr = depDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String depCityCode = fromSegment.getDepCityCode();
                String arrCityCode = fromSegment.getArrCityCode();
                mockClickDepartureStation(driver, depCityCode);
                boolean arrivalStation = mockClickArrivalStation(driver, depCityCode, arrCityCode);
                if (!arrivalStation) {
                    throw new RuntimeException(depCityCode + "到" + arrCityCode + "：该航线未开通");
                }

                // 第二段行程的参数
                SearchAirticketsInputSegment fromSegment2 = fromSegments.get(1);
                LocalDate depDate2 = fromSegment2.getDepDate();
                String depDateStr2 = depDate2.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String depCityCode2 = fromSegment2.getDepCityCode();
                String arrCityCode2 = fromSegment2.getArrCityCode();
                mockClickMutiDepartureStation(driver, depCityCode2);
                boolean arrivalStation2 = mockClickMultiArrivalStation(driver, depCityCode2, arrCityCode2);
                if (!arrivalStation2) {
                    throw new RuntimeException(depCityCode + "到" + arrCityCode + "：该航线未开通");
                }

//                price = getPrice(driver, depDateStr);
//                if (price == null) {
//                    log.info("没有航班信息");
//                    return null;
//                }
//                price = price.add(getPrice(driver, depDateStr2));
//
//                // 获取货币
//                currency = getCurrency(driver);

                mockDate(driver, depDateStr);
                mockDate(driver, depDateStr2);
                confirmDepDate(driver);
                mockSelectClick(driver);

                // 去程
                SearchAirticketsSegment searchAirticketsSegment = grab(driver);
                searchAirticketsSegment.setDepAirport(depCityCode);
                searchAirticketsSegment.setArrAirport(arrCityCode);
                fromSegmentsList.add(searchAirticketsSegment);

                // 回程
                WebElement btnAvailSch = driver.findElement(By.name("btnAvailSch"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnAvailSch);
                Thread.sleep(1000);
                SearchAirticketsSegment ret = grab(driver);
                ret.setDepAirport(arrCityCode);
                ret.setArrAirport(depCityCode);
                fromSegmentsList.add(ret);

            }
        } catch (NoSuchElementException | InterruptedException e) {
            log.info("没有找到元素" + e.getMessage());
        } finally {
            driver.quit();
        }

        List<SearchAirticketsInputSegment> fromSegments = searchAirticketsInput.getFromSegments();
        List<SearchAirticketsInputSegment> retSegments = searchAirticketsInput.getRetSegments();

        SearchAirticketsPriceDetail result = new SearchAirticketsPriceDetail();
        result.setRateCode(UUID.fastUUID().toString());
        result.setCurrency(currency);
        result.setAdultPrice(price);
        result.setFromSegments(fromSegmentsList);

        List<SearchAirticketsPriceDetail> resultList = new ArrayList<>();
        resultList.add(result);
        return resultList;
    }

    private BigDecimal getPrice(WebDriver driver, String dateStr) {

        // 选择日历组件
        List<WebElement> oneDateList = driver.findElements(By.xpath("//span[@aria-label=" + dateStr + "]"));

        for (WebElement oneDate : oneDateList) {
            String oneDateClassStr = oneDate.getAttribute("class");
            // 避开被隐藏的数据块
            if (!oneDateClassStr.contains("hidden")) {
                // 查询价格
                List<WebElement> priceLabel = oneDate.findElements(By.className("label"));
                if (!priceLabel.isEmpty()) {
                    String price = priceLabel.get(0).getText();
                    String replace = price.replace(",", "");
                    return new BigDecimal(replace);
                } else {
                    System.out.println("未找到价格");
                }
                break;
            }
        }
        return null;
    }

    private static Integer getCurrency(WebDriver driver) {
        String currencyText = driver.findElement(By.name("currency")).getText();
        Pattern pattern = Pattern.compile(":\\s*(\\w+)");
        Matcher matcher = pattern.matcher(currencyText);

        if (matcher.find()) {
            String currency = matcher.group(1);
            return CurrencyEnums.getCurrencyEnum(currency).getNumber();
        } else {
            System.out.println("未找到匹配的币种");
            // 重试机制
        }
        return null;
    }

    private boolean VerifyPricedFlight(WebDriver driver) {
        return true;
    }
}

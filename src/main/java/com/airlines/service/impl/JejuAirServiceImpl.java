package com.airlines.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.airlines.service.JejuAirService;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class JejuAirServiceImpl implements JejuAirService {

    public static void main(String[] args) throws InterruptedException {

        String url = "https://www.jejuair.net/zh-cn/main/base/index.do";
        WebDriver driver = getWebDriver();
        driver.get(url);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        String dateStr = getDateStr();
        mockClick(driver, dateStr);
        grab(driver);

        // 需要往后爬几天，就在这里循环几次
        mockClickNextday(driver);
        grab(driver);

        driver.quit();
    }

    private static void mockClickNextday(WebDriver driver) throws InterruptedException {
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

    private static void mockClick(WebDriver driver, String dateStr) throws InterruptedException {
        // 点击单程
        Thread.sleep(1000);
        driver.findElement(By.xpath("//li[@data-triptype='OW']")).click();

        // 点击出发地点
        Thread.sleep(1000);
        driver.findElement(By.id("spanDepartureDesc")).click();

        // 选择出发地点
        Thread.sleep(1000);
        driver.findElement(By.id("plugin-DEPtab-4")).click();
        driver.findElement(By.cssSelector("button[data-stationcode='MFM']")).click();

        // 点击到达地点
        Thread.sleep(1000);
        driver.findElement(By.id("spanArrivalDesc")).click();

        // 选择到达地点
        Thread.sleep(1000);
        driver.findElement(By.id("plugin-ARRtab-3")).click();
        WebElement element = driver.findElement(By.xpath("//div[@aria-labelledby='plugin-ARRtab-3']"));
        element.findElements(By.className("choise")).get(0).click();

        // 点击出发时间
        Thread.sleep(1000);
        driver.findElement(By.id("btnDatePicker")).click();

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
        driver.findElement(By.id("searchFlight")).click();
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
        Optional<WebElement> button = activeNotList.stream().findFirst();
        Thread.sleep(1000);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
    }

    private static WebDriver getWebDriver() {
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

    private static void grab(WebDriver driver) throws InterruptedException {
        log.info("=============== 开始抓取7C航空 ==================");
        // 航班日期报价列表
        String departureCity = driver.findElement(By.id("moSpanDepartureDesc")).getText();
        String arrivalCity = driver.findElement(By.id("moSpanArrivalDesc")).getText();
        log.info("出发城市：" + departureCity + " 到达城市：" + arrivalCity);

        WebElement active = driver.findElement(By.cssSelector("[class = 'air-flight-slide swiper-slide swiper-slide-active active']"));
        String date = active.findElement(By.className("date")).getText();
        log.info("日期：" + date);

        String price = active.findElement(By.className("price")).getText();
        if ("-".equals(price)) {
            // 表示这一天没有航班信息
            log.info("日期：" + date + " 没有航班信息");
            return;
        }

        String unit = active.findElement(By.className("unit")).getText();
        String priceTxt = active.findElement(By.className("price_txt")).getText();
        String priceStr = StrUtil.cleanBlank(priceTxt).replaceAll(",", "");
        int priceNum = Integer.parseInt(priceStr);
        log.info("价格：" + priceNum + " " + unit);

        // 最低价机票信息栏
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
        WebElement durationTotal = listSummary.findElement(By.cssSelector("[class = 'moving-time']"));
        String durationText = durationTotal.getText();
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
                int index = takeoffList.indexOf(landing) + 1;
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

package com.airlines;

import com.airlines.util.SeleniumUtil;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sound.midi.Soundbank;
import java.time.Duration;
import java.util.List;

@SpringBootTest
class AirlinesSyncApplicationTests {

    @Test
    void contextLoads() throws InterruptedException {
        System.out.println("================test");

        String url = "https://www.jejuair.net/zh-cn/main/base/index.do";
        WebDriver driver = SeleniumUtil.getWebDriver();
        driver.get(url);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().maximize();

//        driver.findElement(By.xpath("//li[@data-triptype='RT']")).click();
//        Thread.sleep(1000);

        driver.findElement(By.id("spanDepartureDesc")).click();
        WebElement departureStation = driver.findElement(By.cssSelector("button[data-stationcode = " + "HKG" + "][data-stationtype='DEP']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", departureStation);
        Thread.sleep(1000);

        driver.findElement(By.id("spanArrivalDesc")).click();
        List<WebElement> arrivalStations = driver.findElements(By.cssSelector("button[data-stationcode=" + "ICN" + "][data-stationtype='ARR']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", arrivalStations.get(0));
        Thread.sleep(1000);

        WebElement currency = driver.findElement(By.name("currency"));
        WebElement infoWrap = currency.findElement(By.xpath(".."));
        WebElement picker = infoWrap.findElement(By.xpath(".."));

        // 选择出发时间
        List<WebElement> oneDateList = picker.findElements(By.xpath("//span[@aria-label=" + "20240305" + "]"));
        for (WebElement oneDate : oneDateList) {
            String oneDateClassStr = oneDate.getAttribute("class");
            // 避开被隐藏的数据块
            if (!oneDateClassStr.contains("hidden")) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", oneDate);
                Thread.sleep(1000);
                break;
            }
        }

        WebElement currencyElement = driver.findElement(By.name("currency"));
        WebElement infoWrap2 = currencyElement.findElement(By.xpath(".."));
        WebElement picker2 = infoWrap2.findElement(By.xpath(".."));

        // 选择返程时间
        List<WebElement> oneDateList2 = picker2.findElements(By.xpath("//span[@aria-label=" + "20240307" + "]"));
        for (WebElement oneDate : oneDateList2) {
            String oneDateClassStr = oneDate.getAttribute("class");
            // 避开被隐藏的数据块
            if (!oneDateClassStr.contains("hidden")) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", oneDate);
                Thread.sleep(1000);
                break;
            }
        }

        // 确认出发时间
        WebElement selectDate = driver.findElement(By.xpath("//*[@id=\"dateLayer\"]/div[2]/div[3]/button[3]"));
        selectDate.getText();
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", selectDate);

    }

}

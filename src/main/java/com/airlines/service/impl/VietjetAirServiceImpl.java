package com.airlines.service.impl;

import cn.hutool.core.date.DateTime;
import com.airlines.service.VietjetAirService;
import com.airlines.util.SeleniumUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;
import com.heytrip.common.domain.SearchAirticketsInput;
import com.heytrip.common.domain.SearchAirticketsPriceDetail;

import java.time.Duration;
import java.util.List;


@Service
@Slf4j
public class VietjetAirServiceImpl implements VietjetAirService {

    public static void main(String[] args) throws InterruptedException {
        DateTime start = DateTime.now();
        log.info("=============== 开始抓取VJ航空 ==================" + start);

        String url = "https://www.vietjetair.com/zh-CN/";
        WebDriver driver = SeleniumUtil.getWebDriver();
        driver.get(url);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        Thread.sleep(5000);
        driver.findElement(By.xpath("//h5[text()='接受']")).click();

//        driver.findElement(By.xpath("//button[@id='NC_CTA_ONE' and text()='同意']"));

        Thread.sleep(2000);
        List<WebElement> dancheng = driver.findElements(By.cssSelector("[class = 'MuiTypography-root MuiFormControlLabel-label MuiTypography-body1']"));
        dancheng.forEach(webElement -> {
            if (webElement.getText().contains("单程")) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", webElement);
            }
        });

        Thread.sleep(2000);
        WebElement departureLocationLabel = driver.findElement(By.xpath("//label[text()='出发地点']"));
        String departureLocationText = departureLocationLabel.getText();
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", departureLocationLabel);

        // 胡志明市
        Thread.sleep(2000);
        driver.findElement(By.xpath("//*[@id=\"panel1a-content\"]/div/div[1]")).click();

        // 茱莱市
        Thread.sleep(2000);
        driver.findElement(By.xpath("//*[@id=\"panel1a-content\"]/div/div[3]")).click();

        // 日期，2024-02-09
        Thread.sleep(2000);
        driver.findElement(By.xpath("/html/body/div[9]/div[3]/div[2]/div[2]/div/div[1]/div/div[2]/div[2]/div/div/div/div[1]/div[2]/button[12]")).click();

        // 搜索按钮
        Thread.sleep(2000);
        driver.findElement(By.xpath("/html/body/div[9]/div[3]/div[2]/div/div[2]/button")).click();


        String slideText = driver.findElement(By.cssSelector("[class = 'slick-slide slick-active slick-center slick-current']")).getText();
        slideText.replaceAll("\n","");
        if (slideText.contains("从")) {
            slideText = slideText.substring(slideText.indexOf("从") + 1, slideText.indexOf("从") + 7);
        }
        log.info("============ 今日最低价是 =========== ："+slideText);

        driver.quit();
        log.info("=============== 抓取VJ航空结束 ==================");
    }

    @Override
    public SearchAirticketsPriceDetail searchAirticketsPriceDetail(SearchAirticketsInput searchAirticketsInput) {

        return null;
    }
}

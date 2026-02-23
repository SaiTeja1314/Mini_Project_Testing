package com.example;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class paymenttest {

    WebDriver driver;
    WebDriverWait wait;

    String baseUrl = "http://127.0.0.1:5500";
    String loginUrl = baseUrl + "/index.html";
    String paymentUrl = baseUrl + "/payment.html";
    String testUserEmail = "testuser@example.com";

    @BeforeClass
    public void setup() {
        initializeDriver();
    }

    private void initializeDriver() {
        driver = new EdgeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    private void ensureDriverSession() {
        if (driver == null) {
            initializeDriver();
            return;
        }
        try {
            driver.getTitle();
        } catch (WebDriverException e) {
            try {
                driver.quit();
            } catch (Exception ignored) {
                // Best effort cleanup.
            }
            initializeDriver();
        }
    }

    @BeforeMethod
    public void resetState() {
        ensureDriverSession();
        driver.get(loginUrl);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                "var u = arguments[0];" +
                "localStorage.setItem('mt_currentUser', JSON.stringify(u));" +
                "localStorage.setItem('mt_orders_' + u, JSON.stringify([]));" +
                "localStorage.setItem('mt_cart_' + u, JSON.stringify([]));",
                testUserEmail);
    }

    private void seedCartOneItem() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                "var u = arguments[0];" +
                "localStorage.setItem('mt_cart_' + u, JSON.stringify([{movieId:'1', title:'Inception', price:12.99, image:'x', quantity:2}]));",
                testUserEmail);
    }

    private void openPaymentWithCart() {
        seedCartOneItem();
        driver.get(paymentUrl);
    }

    private void submitPayButton() {
        WebElement payBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("pay-btn")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", payBtn);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(payBtn)).click();
        } catch (Exception ignored) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", payBtn);
        }
    }

    @Test(priority = 1)
    public void verifyRedirectToCartWhenCartIsEmpty() {
        driver.get(paymentUrl);
        wait.until(ExpectedConditions.urlContains("cart.html"));
        Assert.assertTrue(driver.getCurrentUrl().contains("cart.html"));
    }

    @Test(priority = 2)
    public void verifyPaymentPageTitle() {
        openPaymentWithCart();
        Assert.assertEquals(driver.getTitle(), "Payment | Movie Ticket Store");
    }

    @Test(priority = 3)
    public void verifyPaymentFormVisible() {
        openPaymentWithCart();
        WebElement form = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("payment-form")));
        Assert.assertTrue(form.isDisplayed());
    }

    @Test(priority = 4)
    public void verifyOrderSummaryVisible() {
        openPaymentWithCart();
        WebElement summary = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("order-summary")));
        Assert.assertTrue(summary.getText().contains("Amount to pay"));
    }

    @Test(priority = 5)
    public void verifyNavLinksVisible() {
        openPaymentWithCart();
        Assert.assertTrue(!driver.findElements(By.cssSelector(".nav-links a[href='home.html']")).isEmpty());
        Assert.assertTrue(!driver.findElements(By.cssSelector(".nav-links a[href='cart.html']")).isEmpty());
        Assert.assertTrue(!driver.findElements(By.cssSelector(".nav-links a[href='orders.html']")).isEmpty());
        Assert.assertTrue(!driver.findElements(By.id("nav-logout")).isEmpty());
    }

    @Test(priority = 6)
    public void verifyCardNumberAutoFormatting() {
        openPaymentWithCart();
        WebElement cardNumber = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("card-number")));
        cardNumber.sendKeys("1234567890123456");
        Assert.assertEquals(cardNumber.getAttribute("value"), "1234 5678 9012 3456");
    }

    @Test(priority = 7)
    public void verifyExpiryAutoFormatting() {
        openPaymentWithCart();
        WebElement expiry = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("card-expiry")));
        expiry.sendKeys("1228");
        Assert.assertEquals(expiry.getAttribute("value"), "12/28");
    }

    @Test(priority = 8)
    public void verifyCvvAllowsDigitsOnly() {
        openPaymentWithCart();
        WebElement cvv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("card-cvv")));
        cvv.sendKeys("12ab3");
        Assert.assertEquals(cvv.getAttribute("value"), "123");
    }

    @Test(priority = 9)
    public void verifyErrorForEmptyName() {
        openPaymentWithCart();
        driver.findElement(By.id("card-name")).sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        driver.findElement(By.id("card-number")).sendKeys("1234567890123");
        driver.findElement(By.id("card-expiry")).sendKeys("12/99");
        driver.findElement(By.id("card-cvv")).sendKeys("123");
        submitPayButton();

        WebElement nameInput = driver.findElement(By.id("card-name"));
        String validationMessage = nameInput.getAttribute("validationMessage");
        Assert.assertFalse(validationMessage == null || validationMessage.trim().isEmpty(),
                "Name field should show required validation message.");
        Assert.assertTrue(driver.getCurrentUrl().contains("payment.html"));
    }

    @Test(priority = 10)
    public void verifyErrorForInvalidCardNumber() {
        openPaymentWithCart();
        driver.findElement(By.id("card-name")).sendKeys("John Doe");
        driver.findElement(By.id("card-number")).sendKeys("1234");
        driver.findElement(By.id("card-expiry")).sendKeys("12/99");
        driver.findElement(By.id("card-cvv")).sendKeys("123");
        submitPayButton();

        WebElement err = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("payment-err")));
        Assert.assertTrue(err.getText().toLowerCase().contains("valid card number"));
    }

    @Test(priority = 11)
    public void verifyErrorForInvalidExpiry() {
        openPaymentWithCart();
        driver.findElement(By.id("card-name")).sendKeys("John Doe");
        driver.findElement(By.id("card-number")).sendKeys("1234567890123");
        driver.findElement(By.id("card-expiry")).sendKeys("13/20");
        driver.findElement(By.id("card-cvv")).sendKeys("123");
        submitPayButton();

        WebElement err = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("payment-err")));
        Assert.assertTrue(err.getText().toLowerCase().contains("valid expiry"));
    }

    @Test(priority = 12)
    public void verifySuccessfulPaymentRedirectsToOrdersAndClearsCart() {
        openPaymentWithCart();
        driver.findElement(By.id("card-name")).sendKeys("John Doe");
        driver.findElement(By.id("card-number")).sendKeys("1234567890123");
        driver.findElement(By.id("card-expiry")).sendKeys("12/99");
        driver.findElement(By.id("card-cvv")).sendKeys("123");
        submitPayButton();

        wait.until(ExpectedConditions.urlContains("orders.html"));
        Assert.assertTrue(driver.getCurrentUrl().contains("orders.html"));

        Object cartRaw = ((JavascriptExecutor) driver)
                .executeScript("return localStorage.getItem('mt_cart_' + arguments[0]);", testUserEmail);
        String cart = cartRaw == null ? "" : cartRaw.toString();
        Assert.assertTrue(cart.equals("[]"), "Cart should be cleared after successful payment.");

        Object ordersRaw = ((JavascriptExecutor) driver)
                .executeScript("return localStorage.getItem('mt_orders_' + arguments[0]);", testUserEmail);
        String orders = ordersRaw == null ? "" : ordersRaw.toString();
        Assert.assertFalse(orders.equals("[]") || orders.isEmpty(), "Orders should contain at least one entry after payment.");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

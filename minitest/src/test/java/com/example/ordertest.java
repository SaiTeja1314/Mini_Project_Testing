package com.example;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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

public class ordertest {

    WebDriver driver;
    WebDriverWait wait;

    String baseUrl = "http://127.0.0.1:5501";
    String ordersUrl = baseUrl + "/orders.html";
    String loginUrl = baseUrl + "/index.html";
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
                "localStorage.setItem('mt_currentUser', JSON.stringify(arguments[0]));" +
                "localStorage.setItem('mt_orders_' + arguments[0], JSON.stringify([]));" +
                "localStorage.setItem('mt_cart_' + arguments[0], JSON.stringify([]));",
                testUserEmail);
    }

    private void seedOneOrder() {
        String script =
                "var u = arguments[0];" +
                "var orders = [{" +
                "id:'ORD-1001'," +
                "date:'2026-01-10T10:00:00.000Z'," +
                "items:[{title:'Inception', price:12.99, quantity:2}]," +
                "total:25.98" +
                "}];" +
                "localStorage.setItem('mt_orders_' + u, JSON.stringify(orders));";
        ((JavascriptExecutor) driver).executeScript(script, testUserEmail);
    }

    private void seedCartWithQty(int qty) {
        String script =
                "var u = arguments[0]; var q = arguments[1];" +
                "localStorage.setItem('mt_cart_' + u, JSON.stringify([{movieId:'1', title:'Inception', price:12.99, image:'x', quantity:q}]));";
        ((JavascriptExecutor) driver).executeScript(script, testUserEmail, qty);
    }

    @Test(priority = 1)
    public void verifyOrdersPageTitle() {
        driver.get(ordersUrl);
        Assert.assertEquals(driver.getTitle(), "Orders | Movie Ticket Store");
    }

    @Test(priority = 2)
    public void verifyOrdersUrlContainsOrdersHtml() {
        driver.get(ordersUrl);
        Assert.assertTrue(driver.getCurrentUrl().contains("orders.html"));
    }

    @Test(priority = 3)
    public void verifyNavigationLinksVisible() {
        driver.get(ordersUrl);
        Assert.assertTrue(!driver.findElements(By.cssSelector(".nav-links a[href='home.html']")).isEmpty());
        Assert.assertTrue(!driver.findElements(By.cssSelector(".nav-links a[href='cart.html']")).isEmpty());
        Assert.assertTrue(!driver.findElements(By.cssSelector(".nav-links a[href='orders.html']")).isEmpty());
        Assert.assertTrue(!driver.findElements(By.id("nav-logout")).isEmpty());
    }

    @Test(priority = 4)
    public void verifyOrdersHeadingVisible() {
        driver.get(ordersUrl);
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(normalize-space(.),'Order History')]")));
        Assert.assertTrue(heading.isDisplayed());
    }

    @Test(priority = 5)
    public void verifyOrdersListContainerPresent() {
        driver.get(ordersUrl);
        Assert.assertTrue(!driver.findElements(By.id("orders-list")).isEmpty());
    }

    @Test(priority = 6)
    public void verifyEmptyStateVisibleWhenNoOrders() {
        driver.get(ordersUrl);
        WebElement emptyState = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("orders-empty")));
        Assert.assertTrue(emptyState.getText().toLowerCase().contains("no orders yet"));
    }

    @Test(priority = 7)
    public void verifyEmptyStateHasBrowseMoviesLink() {
        driver.get(ordersUrl);
        WebElement browseLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#orders-empty a[href='home.html']")));
        Assert.assertTrue(browseLink.isDisplayed());
    }

    @Test(priority = 8)
    public void verifyOrderCardVisibleWhenOrdersExist() {
        seedOneOrder();
        driver.get(ordersUrl);
        WebElement orderCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#orders-list .order-card")));
        Assert.assertTrue(orderCard.isDisplayed());
    }

    @Test(priority = 9)
    public void verifyOrderCardShowsIdDateAndTotal() {
        seedOneOrder();
        driver.get(ordersUrl);
        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".order-id"))).getText().contains("ORD-1001"));
        Assert.assertFalse(wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".order-date"))).getText().trim().isEmpty());
        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".order-total"))).getText().contains("Total: $25.98"));
    }

    @Test(priority = 10)
    public void verifyOrderItemLineIsRendered() {
        seedOneOrder();
        driver.get(ordersUrl);
        WebElement itemLine = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".order-items li")));
        String text = itemLine.getText();
        Assert.assertTrue(text.contains("Inception"));
        Assert.assertTrue(text.contains("$25.98"));
    }

    @Test(priority = 11)
    public void verifySuccessToastShownWhenSuccessQueryPresent() {
        driver.get(ordersUrl + "?success=1");
        WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("toast")));
        Assert.assertTrue(toast.getText().contains("Order placed successfully"));
        Assert.assertFalse(driver.getCurrentUrl().contains("success=1"), "URL should be cleaned to orders.html after toast.");
    }

    @Test(priority = 12)
    public void verifyNavCartCountUpdatesFromStoredCart() {
        seedCartWithQty(3);
        driver.get(ordersUrl);
        WebElement cartCount = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("nav-cart-count")));
        String countText = cartCount.getText().trim();
        Assert.assertTrue(countText.matches("\\d+"),
                "Navbar cart count should be numeric. Found: " + countText);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

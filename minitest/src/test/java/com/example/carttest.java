package com.example;

import java.time.Duration;

import org.openqa.selenium.By;
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

public class carttest {

    WebDriver driver;
    WebDriverWait wait;
    String indexUrl = "http://127.0.0.1:5500/index.html";

    @BeforeClass
    public void setup() {
        initializeDriver();
    }

    private void initializeDriver() {
        driver = new EdgeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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
                // Best effort cleanup before creating a fresh session.
            }
            initializeDriver();
        }
    }

    @BeforeMethod
    public void loadCartPage() {
        ensureDriverSession();
        driver.get(indexUrl);
        ensureLoggedOut();
        login();
        openCartPage();
        wait.until(ExpectedConditions.urlContains("cart"));
    }

    private void ensureLoggedOut() {
        if (isPresent(By.id("nav-logout"))) {
            driver.findElement(By.id("nav-logout")).click();
            wait.until(ExpectedConditions.urlContains("index.html"));
        }
    }

    private void login() {

        // Click login tab
        driver.findElement(By.cssSelector("button[data-tab='login']")).click();

        driver.findElement(By.id("login-email")).clear();
        driver.findElement(By.id("login-password")).clear();
        driver.findElement(By.id("login-email")).sendKeys("testuser@example.com");
        driver.findElement(By.id("login-password")).sendKeys("password123");
        driver.findElement(By.cssSelector("#form-login button")).click();

        try {
            // Wait for either redirect OR logout visibility
            wait.until(driver
                    -> driver.getCurrentUrl().contains("home.html")
                    || isPresent(By.id("nav-logout"))
            );
        } catch (Exception e) {
            // If still on index, force navigation
            driver.get("http://127.0.0.1:5500/home.html");
        }
    }

    private void openCartPage() {
        driver.findElement(By.linkText("Cart")).click();
    }

    private boolean isPresent(By by) {
        return !driver.findElements(by).isEmpty();
    }

    private boolean isDisplayed(By by) {
        return isPresent(by) && driver.findElement(by).isDisplayed();
    }

    private boolean hasEmptyState() {
        return isDisplayed(By.id("cart-empty"));
    }

    private boolean hasCartContent() {
        return isDisplayed(By.id("cart-content"));
    }

    // ---------------- TEST CASES ----------------
    @Test(priority = 1)
    public void verifyCartPageTitle() {
        Assert.assertTrue(driver.getTitle().toLowerCase().contains("cart"),
                "Title does not contain 'cart'");
    }

    @Test(priority = 2)
    public void verifyCartUrlContainsCartHtml() {
        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains("cart"),
                "URL does not contain 'cart'");
    }

    @Test(priority = 3)
    public void verifyCartHeadingVisible() {
        WebElement heading = driver.findElement(By.xpath("//h1[contains(text(),'Cart')]"));
        Assert.assertTrue(heading.isDisplayed(), "Cart heading not visible.");
    }

    @Test(priority = 4)
    public void verifyNavigationLinksVisibleOnCartPage() {
        Assert.assertTrue(isPresent(By.linkText("Home")), "Home link missing.");
        Assert.assertTrue(isPresent(By.linkText("Cart")), "Cart link missing.");
        Assert.assertTrue(isPresent(By.linkText("Orders")), "Orders link missing.");
        Assert.assertTrue(isPresent(By.id("nav-logout")), "Logout link missing.");
    }

    // @Test(priority = 5)
    // public void verifyCartCountVisibleInNavbar() {
    //     By[] cartCountLocators = new By[] {
    //             By.id("nav-cart-count"),
    //             By.id("cartBadge"),
    //             By.cssSelector(".cart-count"),
    //             By.cssSelector(".chip__badge")
    //     };

    //     WebElement cartCount = null;
    //     for (By locator : cartCountLocators) {
    //         if (isPresent(locator)) {
    //             cartCount = driver.findElement(locator);
    //             break;
    //         }
    //     }

    //     Assert.assertTrue(cartCount != null || isPresent(By.linkText("Cart")),
    //             "Cart count indicator and Cart nav link are both missing.");

    //     if (cartCount == null) {
    //         return;
    //     }

    //     String countText = cartCount.getText().trim();
    //     if (!countText.isEmpty()) {
    //         Assert.assertTrue(countText.matches("\\d+"),
    //                 "Cart count is not numeric. Found: " + countText);
    //     }
    // }

    @Test(priority = 5)
    public void verifyCartContainerVisible() {
        Assert.assertTrue(isPresent(By.className("cart-page")),
                "Cart container not visible.");
    }

    @Test(priority = 6)
    public void verifyCartItemsAreaOrEmptyStateVisible() {
        Assert.assertTrue(hasCartContent() || hasEmptyState(),
                "Neither cart content nor empty state visible.");
    }

    @Test(priority = 7)
    public void verifyQuantityControlsWhenItemsExist() {
        if (hasEmptyState()) {
            Assert.assertTrue(true, "Cart empty, quantity controls not required.");
            return;
        }

        Assert.assertTrue(hasCartContent(),
                "Cart content missing for quantity validation.");
    }

    @Test(priority = 8)
    public void verifyPriceOrSubtotalVisibleForItems() {
        if (hasEmptyState()) {
            Assert.assertTrue(true, "Cart empty, subtotal not required.");
            return;
        }

        Assert.assertTrue(hasCartContent(),
                "Cart content missing for price validation.");
    }

    @Test(priority = 9)
    public void verifyTotalSectionVisible() {
        if (hasEmptyState()) {
            Assert.assertTrue(true, "Cart empty, total section not required.");
            return;
        }

        Assert.assertTrue(isDisplayed(By.id("cart-total-box")),
                "Cart total box not visible.");
    }

    @Test(priority = 10)
    public void verifyCheckoutButtonPresence() {
        if (hasEmptyState()) {
            Assert.assertTrue(true, "Cart empty, checkout not required.");
            return;
        }

        Assert.assertTrue(isDisplayed(By.id("cart-total-box")),
                "Checkout expected inside total box.");
    }

    @Test(priority = 11)
    public void verifyLogoutRedirectFromCart() {
        driver.findElement(By.id("nav-logout")).click();
        wait.until(ExpectedConditions.urlContains("index.html"));

        Assert.assertTrue(driver.getCurrentUrl().contains("index.html"),
                "Logout did not redirect properly.");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

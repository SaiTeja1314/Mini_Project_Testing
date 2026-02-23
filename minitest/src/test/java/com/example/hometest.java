package com.example;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class hometest {

    WebDriver driver;
    WebDriverWait wait;
    String indexUrl = "http://127.0.0.1:5501/index.html";

    @BeforeClass
    public void setup() {
        driver = new EdgeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @BeforeMethod
    public void loadIndexPage() {
        driver.get(indexUrl);
        ensureAtIndexLoggedOut();
    }

    public void signupAndLogin() {
        driver.get(indexUrl);
        ensureAtIndexLoggedOut();

        loginWithKnownUser();

        if (!waitForHomeRedirect(Duration.ofSeconds(3))) {
            driver.findElement(By.cssSelector("button[data-tab='signup']")).click();
            driver.findElement(By.id("signup-email")).clear();
            driver.findElement(By.id("signup-password")).clear();
            driver.findElement(By.id("signup-email")).sendKeys("testuser@example.com");
            driver.findElement(By.id("signup-password")).sendKeys("password123");
            driver.findElement(By.cssSelector("#form-signup button")).click();

            loginWithKnownUser();
        }

        wait.until(ExpectedConditions.urlContains("home.html"));
        Assert.assertTrue(driver.getCurrentUrl().contains("home.html"),
                "Login did not navigate to home page. Current URL: " + driver.getCurrentUrl());
    }

    private void ensureAtIndexLoggedOut() {
        if (driver.getCurrentUrl().contains("home.html") || isPresent(By.id("nav-logout"))) {
            if (isPresent(By.id("nav-logout"))) {
                driver.findElement(By.id("nav-logout")).click();
                wait.until(ExpectedConditions.urlContains("index.html"));
            } else {
                driver.get(indexUrl);
            }
        }
    }

    private void loginWithKnownUser() {
        if (!isPresent(By.id("login-email"))) {
            driver.get(indexUrl);
            ensureAtIndexLoggedOut();
        }
        driver.findElement(By.cssSelector("button[data-tab='login']")).click();
        WebElement email = driver.findElement(By.id("login-email"));
        WebElement password = driver.findElement(By.id("login-password"));
        email.clear();
        password.clear();
        email.sendKeys("testuser@example.com");
        password.sendKeys("password123");
        driver.findElement(By.cssSelector("#form-login button")).click();
    }

    private boolean waitForHomeRedirect(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(ExpectedConditions.urlContains("home.html"));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isPresent(By by) {
        return !driver.findElements(by).isEmpty();
    }

    // =========================
    // Test Cases
    // =========================
    @Test(priority = 1)
    public void verifyHomePageTitle() {

        signupAndLogin();

        Assert.assertEquals(driver.getTitle(),
                "Home | Movie Ticket Store");
    }

    @Test(priority = 2)
    public void verifyNavigationLinksVisible() {

        signupAndLogin();

        Assert.assertTrue(driver.findElement(By.linkText("Home")).isDisplayed());
        Assert.assertTrue(driver.findElement(By.linkText("Cart")).isDisplayed());
        Assert.assertTrue(driver.findElement(By.linkText("Orders")).isDisplayed());
        Assert.assertTrue(driver.findElement(By.id("nav-logout")).isDisplayed());
    }

    @Test(priority = 3)
    public void verifySearchBarPresent() {

        signupAndLogin();

        WebElement searchBox = driver.findElement(By.id("search-movies"));
        Assert.assertTrue(searchBox.isDisplayed());
        Assert.assertEquals(searchBox.getAttribute("type"), "search");
    }

    @Test(priority = 4)
    public void verifyMovieGridVisible() {

        signupAndLogin();

        WebElement movieGrid = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("movie-grid"))
        );

        Assert.assertTrue(movieGrid.isDisplayed());
    }

    @Test(priority = 5)
    public void verifyCartCountVisible() {

        signupAndLogin();

        WebElement cartCount = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("nav-cart-count"))
        );
        Assert.assertNotNull(cartCount);
    }

    @Test(priority = 6)
    public void verifyLogoutRedirectsToIndex() {

        signupAndLogin();

        driver.findElement(By.id("nav-logout")).click();

        wait.until(ExpectedConditions.urlContains("index.html"));

        Assert.assertTrue(driver.getCurrentUrl().contains("index.html"));
    }

    @AfterClass
    public void tearDown() {
        driver.quit();
    }
}

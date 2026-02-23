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

public class moviedetailstest {

    WebDriver driver;
    WebDriverWait wait;

    String baseUrl = "http://127.0.0.1:5501";
    String detailsUrl = baseUrl + "/movie-details.html";
    String homeUrl = baseUrl + "/home.html";
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
                "localStorage.setItem('mt_cart_' + arguments[0], JSON.stringify([]));",
                testUserEmail);
    }

    private void openMovieDetailsWithId(String id) {
        if (id == null) {
            driver.get(detailsUrl);
        } else {
            driver.get(detailsUrl + "?id=" + id);
        }
    }

    private boolean isPresent(By by) {
        return !driver.findElements(by).isEmpty();
    }

    @Test(priority = 1)
    public void verifyMovieDetailsPageTitle() {
        openMovieDetailsWithId("1");
        Assert.assertEquals(driver.getTitle(), "Movie Details | Movie Ticket Store");
    }

    @Test(priority = 2)
    public void verifyNavigationLinksVisible() {
        openMovieDetailsWithId("1");
        Assert.assertTrue(isPresent(By.cssSelector(".nav-links a[href='home.html']")));
        Assert.assertTrue(isPresent(By.cssSelector(".nav-links a[href='cart.html']")));
        Assert.assertTrue(isPresent(By.cssSelector(".nav-links a[href='orders.html']")));
        Assert.assertTrue(isPresent(By.id("nav-logout")));
    }

    @Test(priority = 3)
    public void verifyDetailContentVisibleForValidMovieId() {
        openMovieDetailsWithId("1");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#detail-content .detail-layout")));
        Assert.assertTrue(driver.findElement(By.cssSelector("#detail-content .detail-layout")).isDisplayed());
    }

    @Test(priority = 4)
    public void verifyMovieTitleIsDisplayed() {
        openMovieDetailsWithId("1");
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#detail-content h1")));
        Assert.assertFalse(title.getText().trim().isEmpty(), "Movie title should not be empty.");
    }

    @Test(priority = 5)
    public void verifyMoviePosterImageLoaded() {
        openMovieDetailsWithId("1");
        WebElement image = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#detail-content .detail-poster img")));
        String src = image.getAttribute("src");
        Assert.assertNotNull(src);
        Assert.assertFalse(src.trim().isEmpty(), "Movie poster src should not be empty.");
    }

    @Test(priority = 6)
    public void verifyMoviePriceFormat() {
        openMovieDetailsWithId("1");
        WebElement price = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#detail-content .detail-price")));
        Assert.assertTrue(price.getText().trim().matches("^\\$\\d+\\.\\d{2}$"),
                "Price format should be like $12.99. Found: " + price.getText());
    }

    @Test(priority = 7)
    public void verifyAddToCartButtonVisibleAndEnabled() {
        openMovieDetailsWithId("1");
        WebElement addToCartBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btn-add-cart")));
        Assert.assertTrue(addToCartBtn.isEnabled(), "Add to Cart button should be enabled.");
    }

    @Test(priority = 8)
    public void verifyBackToHomeLinkPresent() {
        openMovieDetailsWithId("1");
        WebElement backLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Back to Home")));
        Assert.assertTrue(backLink.getAttribute("href").contains("home.html"));
    }

    @Test(priority = 9)
    public void verifyAddToCartUpdatesNavbarCountAndShowsToast() {
        openMovieDetailsWithId("1");
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-add-cart")));
        addToCartBtn.click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("nav-cart-count"), "1"));
        WebElement cartCount = driver.findElement(By.id("nav-cart-count"));
        Assert.assertEquals(cartCount.getText().trim(), "1");

        WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("toast")));
        Assert.assertTrue(toast.getText().contains("Added to cart"), "Toast should confirm add-to-cart.");
    }

    @Test(priority = 10)
    public void verifyAddToCartPersistsCartItemInLocalStorage() {
        openMovieDetailsWithId("1");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-add-cart"))).click();

        String script = "return localStorage.getItem('mt_cart_' + arguments[0]);";
        Object raw = ((JavascriptExecutor) driver).executeScript(script, testUserEmail);
        String value = raw == null ? "" : raw.toString();

        Assert.assertTrue(value.contains("\"movieId\":\"1\""), "Cart storage should include movie id 1.");
        Assert.assertTrue(value.contains("\"quantity\":1"), "Cart storage should include quantity 1.");
    }

    @Test(priority = 11)
    public void verifyInvalidMovieIdShowsNotFoundMessage() {
        openMovieDetailsWithId("999");
        WebElement notFound = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("detail-not-found")));
        Assert.assertTrue(notFound.getText().toLowerCase().contains("movie not found"));
    }

    @Test(priority = 12)
    public void verifyMissingMovieIdShowsNotFoundMessage() {
        openMovieDetailsWithId(null);
        WebElement notFound = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("detail-not-found")));
        Assert.assertTrue(notFound.getText().toLowerCase().contains("movie not found"));
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

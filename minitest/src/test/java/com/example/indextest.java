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

public class indextest {

    WebDriver driver;
    WebDriverWait wait;
    String baseUrl = "http://127.0.0.1:5501/index.html";

    @BeforeClass
    public void setup() {
        driver = new EdgeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @BeforeMethod
    public void loadPage() {
        driver.get(baseUrl);
        ensureAtIndexLoggedOut();
        System.out.println("Loaded URL: " + driver.getCurrentUrl());
        System.out.println("Title: " + driver.getTitle());
    }

    private void ensureAtIndexLoggedOut() {
        if (driver.getCurrentUrl().contains("home.html") || !isPresent(By.id("form-login"))) {
            if (isPresent(By.id("nav-logout"))) {
                driver.findElement(By.id("nav-logout")).click();
                wait.until(ExpectedConditions.urlContains("index.html"));
            } else {
                driver.get(baseUrl);
            }
        }
    }

    private boolean isPresent(By by) {
        return !driver.findElements(by).isEmpty();
    }

    @Test(priority = 1)
    public void verifyLoginTabDefault() {
        WebElement loginForm = driver.findElement(By.id("form-login"));
        Assert.assertTrue(loginForm.getAttribute("class").contains("active"));
    }

    @Test(priority = 2)
    public void verifySignupTabSwitch() {
        driver.findElement(By.xpath("//button[@data-tab='signup']")).click();

        WebElement signupForm = driver.findElement(By.id("form-signup"));
        Assert.assertTrue(signupForm.getAttribute("class").contains("active"));
    }

    @Test(priority = 3)
    public void verifyLoginEmptyFields() {
        driver.findElement(By.cssSelector("button[data-tab='login']")).click();

        WebElement email = driver.findElement(By.id("login-email"));
        WebElement password = driver.findElement(By.id("login-password"));

        email.clear();
        password.clear();

        driver.findElement(By.cssSelector("#form-login button")).click();

        Assert.assertTrue(email.getAttribute("required") != null);

    }

    @Test(priority = 4)
    public void verifySuccessfulSignup() {

        driver.findElement(By.xpath("//button[@data-tab='signup']")).click();

        driver.findElement(By.id("signup-email")).sendKeys("testuser@example.com");
        driver.findElement(By.id("signup-password")).sendKeys("password123");

        driver.findElement(By.xpath("//form[@id='form-signup']//button")).click();

        WebElement successMsg = driver.findElement(By.id("signup-success"));

        Assert.assertTrue(successMsg.isDisplayed());
    }

    @Test(priority = 5)
    public void verifySuccessfulLogin() {

        driver.findElement(By.xpath("//button[@data-tab='login']")).click();

        driver.findElement(By.id("login-email")).sendKeys("testuser@example.com");
        driver.findElement(By.id("login-password")).sendKeys("password123");

        driver.findElement(By.xpath("//form[@id='form-login']//button")).click();
        wait.until(ExpectedConditions.urlContains("home.html"));
        Assert.assertTrue(driver.getCurrentUrl().contains("home.html"));
    }

    @Test(priority = 6)
    public void verifyIndexPageTitle() {
        Assert.assertEquals(driver.getTitle(), "Login | Movie Ticket Store");
    }

    @Test(priority = 7)
    public void verifyLoginFieldsTypeAndRequired() {
        WebElement email = driver.findElement(By.id("login-email"));
        WebElement password = driver.findElement(By.id("login-password"));

        Assert.assertEquals(email.getAttribute("type"), "email");
        Assert.assertEquals(password.getAttribute("type"), "password");
        Assert.assertNotNull(email.getAttribute("required"));
        Assert.assertNotNull(password.getAttribute("required"));
    }

    @Test(priority = 8)
    public void verifySignupFieldsTypeAndRequired() {
        driver.findElement(By.cssSelector("button[data-tab='signup']")).click();

        WebElement email = driver.findElement(By.id("signup-email"));
        WebElement password = driver.findElement(By.id("signup-password"));

        Assert.assertEquals(email.getAttribute("type"), "email");
        Assert.assertEquals(password.getAttribute("type"), "password");
        Assert.assertNotNull(email.getAttribute("required"));
        Assert.assertNotNull(password.getAttribute("required"));
    }

    @Test(priority = 9)
    public void verifySwitchBackToLoginTab() {
        driver.findElement(By.cssSelector("button[data-tab='signup']")).click();
        driver.findElement(By.cssSelector("button[data-tab='login']")).click();

        WebElement loginForm = driver.findElement(By.id("form-login"));
        WebElement signupForm = driver.findElement(By.id("form-signup"));

        Assert.assertTrue(loginForm.getAttribute("class").contains("active"));
        Assert.assertFalse(signupForm.getAttribute("class").contains("active"));
    }

    @Test(priority = 10)
    public void verifyLoginSubmitButtonPresent() {
        WebElement submit = driver.findElement(By.cssSelector("#form-login button"));
        Assert.assertTrue(submit.isDisplayed());
        Assert.assertTrue(submit.isEnabled());
    }

    @Test(priority = 11)
    public void verifySignupSubmitButtonPresent() {
        driver.findElement(By.cssSelector("button[data-tab='signup']")).click();
        WebElement submit = driver.findElement(By.cssSelector("#form-signup button"));
        Assert.assertTrue(submit.isDisplayed());
        Assert.assertTrue(submit.isEnabled());
    }

    @Test(priority = 12)
    public void verifyLoginTabButtonIsDisplayed() {
        WebElement loginTabButton = driver.findElement(By.cssSelector("button[data-tab='login']"));
        Assert.assertTrue(loginTabButton.isDisplayed());
        Assert.assertTrue(loginTabButton.isEnabled());
    }
        
    @AfterClass
    public void tearDown() {
        driver.quit();
    }
}

package chrome;

import TestHelpers.TestStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class cart {
    WebDriver driver;
    WebDriverWait wait;
    By dismiss = By.cssSelector(".woocommerce-store-notice__dismiss-link");

    @RegisterExtension
    TestStatus status = new TestStatus();

    @BeforeEach
    public void driverSetup() {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, 7);
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        driver.navigate().to("https://fakestore.testelka.pl/");
        driver.findElement(dismiss).click();


    }

    @AfterEach
    public void driverQuit(TestInfo info) throws IOException {
        if (status.isFailed) {
            System.out.println("Test screenshot is available at: " + takeScreenshot(info));
        }
        driver.quit();
    }

    @Test
    public void addTripFromTripSiteTest() {
        goToEgyptTripSite();
        addToCartAndGoToCartFromTripSite();
        String cartName = driver.findElement(By.cssSelector(".cart_item>td.product-name>a:first-of-type")).getText();
        Assertions.assertEquals("Egipt - El Gouna", cartName, "The name does not match expected name.");
    }

    @Test
    public void addTripFromCategorySiteTest() {
        driver.findElement
                (By.cssSelector("[class='product-category product first']")).click();
        driver.findElement(By.cssSelector("[data-product_id='393']")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated
                (By.cssSelector("[class='added_to_cart wc-forward']"))).click();
        String cartName = driver.findElement(By.cssSelector(".cart_item>td.product-name>a:first-of-type")).getText();
        Assertions.assertEquals("Fuerteventura - Sotavento", cartName, "The name does not match expected name.");
    }

    @Test
    public void add10TripsTest() {
        for(int i=0; i<6; i++){
            driver.findElement(By.cssSelector(".storefront-recent-products [data-product_id='4116']")).click();
            wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".loading"), 0));
        }
        for(int i=0; i<4; i++){
            driver.findElement(By.cssSelector(".storefront-recent-products [data-product_id='4114']")).click();
            wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".loading"), 0));
        }
        goToCartFromMainPage();
        Assertions.assertEquals(10, countItemsInCart(), "Number does not match expected number.");
    }

    @Test
    public void addMultipleTripToCartTest(){
        goToEgyptTripSite();
        WebElement quantityElement = driver.findElement(By.cssSelector("[id*='quantity']"));
        quantityElement.clear();
        quantityElement.sendKeys("5");
        addToCartAndGoToCartFromTripSite();
        Assertions.assertEquals(5, countItemsInCart(), "Items in cart does not match expected number.");
    }

    @Test
    public void changeQuantityInCartTest() {
        driver.findElement(By.cssSelector("[data-product_id='46']")).click();
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".loading"), 0));
        goToCartFromMainPage();
        WebElement quantityElement = driver.findElement(By.cssSelector("[id*='quantity']"));
        quantityElement.clear();
        quantityElement.sendKeys("5");
        driver.findElement(By.cssSelector("[name='update_cart']")).click();
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".blockUI"), 0));
        Assertions.assertEquals(5, countItemsInCart(), "Quantity does not match expected value.");
    }

    @Test
    public void add10DifferentTripsTest() {
        driver.findElement(By.cssSelector("[data-product_id='4116']")).click();
        driver.findElement(By.cssSelector("[data-product_id='4114']")).click();
        driver.findElement(By.cssSelector("[data-product_id='393']")).click();
        driver.findElement(By.cssSelector("[data-product_id='391']")).click();
        driver.findElement(By.cssSelector("[data-product_id='61']")).click();
        driver.findElement(By.cssSelector("[data-product_id='53']")).click();
        driver.findElement(By.cssSelector("[data-product_id='46']")).click();
        driver.findElement(By.cssSelector("[data-product_id='40']")).click();
        driver.findElement(By.cssSelector("[data-product_id='386']")).click();
        driver.findElement(By.cssSelector("[data-product_id='64']")).click();
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".loading"), 0));
        goToCartFromMainPage();
        Assertions.assertEquals(10, countItemsInCart(), "Quantity does not match expected value.");
    }

    @Test
    public void deleteItemFromCart() {
        for(int i=0; i<4; i++){
            driver.findElement(By.cssSelector(".storefront-recent-products [data-product_id='4114']")).click();
            wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".loading"), 0));
        }
        goToCartFromMainPage();
        driver.findElement(By.cssSelector(".remove")).click();
        WebElement emptyCart = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".cart-empty")));
        Assertions.assertNotNull(emptyCart, "Cart is not empty.");
    }

    private void goToEgyptTripSite() {
        driver.findElement
                (By.cssSelector("[class='product-category product first']")).click();
        driver.findElement
                (By.cssSelector(".post-386")).click();
    }

    private void addToCartAndGoToCartFromTripSite() {
        driver.findElement(By.cssSelector("[name='add-to-cart']")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[role='alert']")));
        goToCartFromMainPage();
    }

    private int countItemsInCart() {
        List<WebElement> elements = driver.findElements(By.cssSelector("td.product-quantity input"));
        int count = 0;
        for(WebElement element:elements){
            int quantity = Integer.parseInt(element.getAttribute("value"));
            count += quantity;
        }
        return count;
    }

    private void goToCartFromMainPage() {
        driver.findElement(By.cssSelector(".cart-contents")).click();
    }

    private String takeScreenshot(TestInfo info) throws IOException {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        LocalDateTime timeNow = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        String path = "C:\\screenshots\\" + info.getDisplayName() + " " + formatter.format(timeNow) + ".png";
        FileHandler.copy(screenshot, new File(path));
        return path;
    }
}

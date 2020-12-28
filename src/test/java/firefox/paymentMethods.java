package firefox;

import TestHelpers.TestStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class paymentMethods {
    WebDriver driver;
    WebDriverWait wait;
    By dismiss = By.cssSelector(".woocommerce-store-notice__dismiss-link");
    By product = By.cssSelector("[data-product_id='53']");
    By loading = By.cssSelector(".loading");
    By order = By.cssSelector("#menu-item-199");
    By cardNumberField = By.cssSelector("[name='cardnumber']");
    By cardNumberFrame = By.cssSelector("#stripe-card-element iframe");
    By cardExpirationDateField = By.cssSelector("[name='exp-date']");
    By cardExpirationDateFrame = By.cssSelector("#stripe-exp-element iframe");
    By cvcField = By.cssSelector("[name='cvc']");
    By cvcFrame = By.cssSelector("#stripe-cvc-element iframe");
    By loadingBlock = By.cssSelector(".blockOverlay");
    By terms = By.cssSelector("#terms");
    By orderButton = By.cssSelector("#place_order");
    By secureAuthorizeFrame = By.cssSelector("iframe[style*='absolute']");
    By secondStepSecure = By.cssSelector("iframe#challengeFrame");
    By fullscreenFrame = By.cssSelector("iframe.FullscreenFrame");
    By authorizeButton = By.cssSelector("button#test-source-authorize-3ds");
    By failButton = By.cssSelector("button#test-source-fail-3ds");
    By errorMessage = By.cssSelector(".woocommerce_error");

    @RegisterExtension
    TestStatus status = new TestStatus();

    @BeforeEach
    public void driverSetup() {
        System.setProperty("webdriver.gecko.driver", "src/main/resources/geckodriver.exe");
        driver = new FirefoxDriver();
        wait = new WebDriverWait(driver, 15);
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        driver.navigate().to("https://fakestore.testelka.pl/");
        driver.findElement(dismiss).click();
        driver.findElement(product).click();
        wait.until(ExpectedConditions.numberOfElementsToBe(loading, 0));
        driver.findElement(order).click();
    }

    @AfterEach
    public void driverQuit(TestInfo info) throws IOException {
        if (status.isFailed) {
            System.out.println("Test screenshot is available at: " + takeScreenshot(info));
        }
        driver.quit();
    }

    @Test
    public void correctPaymentWith3DSecure2Test() {
        fillFormFields();
        fillCardFields("4000000000003220", "1222", "432");
        placeOrder();
        switchToFrame(secureAuthorizeFrame);
        switchToFrame(secondStepSecure);
        wait.until(ExpectedConditions.elementToBeClickable(authorizeButton)).submit();
        driver.switchTo().defaultContent();
        Assertions.assertNotNull(waitAfterCorrectOrder(), "Order number was not found.");
    }

    @Test
    public void rejectedPaymentWith3DSecure2Test() {
        fillFormFields();
        fillCardFields("4000000000003220", "1222", "432");
        placeOrder();
        switchToFrame(secureAuthorizeFrame);
        switchToFrame(secondStepSecure);
        wait.until(ExpectedConditions.elementToBeClickable(failButton)).submit();
        driver.switchTo().defaultContent();
        String expectedError = "Nie można przetworzyć tej płatności, spróbuj ponownie lub użyj alternatywnej metody.";
        Assertions.assertEquals(expectedError, getErrorMessage(), "Error message does not match expected error");
    }

    @Test
    public void incorrectExpirationDateTest() {
        fillFormFields();
        fillCardFields("4000000000003220", "0808", "432");
        placeOrder();
        String expectedError = "Rok ważności karty upłynął w przeszłości";
        Assertions.assertEquals(expectedError, getErrorMessage(), "Error message does not match expected error");
    }

    @Test
    public void incompleteExpirationDateTest() {
        fillFormFields();
        fillCardFields("4000000000003220", "", "432");
        placeOrder();
        String expectedError = "Data ważności karty jest niekompletna.";
        Assertions.assertEquals(expectedError, getErrorMessage(), "Error message does not match expected error");
    }

    @Test
    public void incorrectCVCTest() {
        fillFormFields();
        fillCardFields("4000000000003220", "1222", "");
        placeOrder();
        String expectedError = "Kod bezpieczeństwa karty jest niekompletny.";
        Assertions.assertEquals(expectedError, getErrorMessage(), "Error message does not match expected error");
    }

    @Test
    public void incompleteCardNumberTest() {
        fillFormFields();
        fillCardFields("400000000000322", "1222", "123");
        placeOrder();
        String expectedError = "Numer karty jest niekompletny.";
        Assertions.assertEquals(expectedError, getErrorMessage(), "Error message does not match expected error");
    }

    @Test
    public void incorrectCardNumberTest() {
        fillFormFields();
        fillCardFields("1234 5678 9012 3456", "1222", "123");
        placeOrder();
        String expectedError = "Numer karty nie jest prawidłowym numerem karty kredytowej.";
        Assertions.assertEquals(expectedError, getErrorMessage(), "Error message does not match expected error");
    }

    @Test
    public void completePaymentWith3DSecureTest() {
        fillFormFields();
        fillCardFields("4000008400001629", "1222", "432");
        placeOrder();
        switchToFrame(secureAuthorizeFrame);
        switchToFrame(secondStepSecure);
        switchToFrame(fullscreenFrame);
        wait.until(ExpectedConditions.elementToBeClickable(authorizeButton)).submit();
        driver.switchTo().defaultContent();
        String expectedError = "Karta została odrzucona.";
        Assertions.assertEquals(expectedError, getErrorMessage(), "Error message does not match expected error");
    }

    @Test
    public void completeNormalPayment() {
        fillFormFields();
        fillCardFields("378282246310005", "1222", "432");
        placeOrder();
        Assertions.assertNotNull(waitAfterCorrectOrder(), "Order number was not found.");
    }

    private void placeOrder() {
        wait.until(ExpectedConditions.numberOfElementsToBe(loadingBlock, 0));
        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(orderButton))).click();
    }

    private String getErrorMessage() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(errorMessage)).getText();
    }

    private void fillFormFields() {
        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.cssSelector("#billing_first_name")))).sendKeys("Gandalf");
        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.cssSelector("#billing_last_name")))).sendKeys("Rudy");
        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.cssSelector("#billing_address_1")))).sendKeys("Ulica Sezamkowa 4");
        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.cssSelector("#billing_postcode")))).sendKeys("55-555");
        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.cssSelector("#billing_city")))).sendKeys("Nowy Jork");
        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.cssSelector("#billing_phone")))).sendKeys("555444333");
        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.cssSelector("#billing_email")))).sendKeys("testaccount123test@testtest.com");
        wait.until(ExpectedConditions.numberOfElementsToBe(loadingBlock, 0));
        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(terms))).click();
    }

    private void fillCardFields(String cardNumber, String cardDate, String cardCVC) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingBlock));
        WebElement cardNumberElement = findElementInFrame(cardNumberFrame, cardNumberField);
        cardNumberElement.sendKeys(cardNumber);
        WebElement cardExpDate = findElementInFrame(cardExpirationDateFrame, cardExpirationDateField);
        cardExpDate.sendKeys(cardDate);
        WebElement cvcElement = findElementInFrame(cvcFrame, cvcField);
        cvcElement.sendKeys(cardCVC);
        driver.switchTo().defaultContent();
    }

    private String waitAfterCorrectOrder() {
        By orderNumberSummary = By.cssSelector(".order>strong");
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(ExpectedConditions.numberOfElementsToBe(loadingBlock, 0));
        wait.until(ExpectedConditions.urlContains("/zamowienie/zamowienie-otrzymane/"));
        return wait.until(ExpectedConditions.presenceOfElementLocated(orderNumberSummary)).getText();
    }

    private void switchToFrame(By frameLocator){
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
        wait.until(d->((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete"));
    }

    private WebElement findElementInFrame(By frameLocator, By inputLocator) {
        driver.switchTo().defaultContent();
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
        return wait.until(ExpectedConditions.elementToBeClickable(inputLocator));
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

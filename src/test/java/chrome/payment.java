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
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class payment {
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
    By orderButton = By.cssSelector("#place_order");
    By emptyForm = By.cssSelector(".woocommerce-error");
    By loginForm = By.cssSelector(".showlogin");
    By loginUser = By.cssSelector("#username");
    By loginPassword = By.cssSelector("#password");
    By loginButton = By.cssSelector("[name='login']");
    By terms = By.cssSelector("#terms");
    By orderNumberSummary = By.cssSelector(".order>strong");
    By myAccount = By.cssSelector("[class*='my-account menu-item']");
    By myOrders = By.cssSelector(".woocommerce-MyAccount-navigation-link--orders");
    By orderDateSummary = By.cssSelector(".woocommerce-order-overview__date>strong");
    By orderTotalSummary = By.cssSelector(".woocommerce-order-overview__total bdi");
    By orderPaymentSummary = By.cssSelector(".woocommerce-order-overview__payment-method>strong");
    By orderProductNameSummary = By.cssSelector(".woocommerce-table__product-name>a");
    By orderProductQuantitySummary = By.cssSelector(".woocommerce-table__product-name>strong");


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
    public void emptyFillCardFilledTest() {
        fillCardFields(false);
        placeOrder();
        String errorMessage = getErrorMessage();
        Assertions.assertAll(
                () -> Assertions.assertTrue(errorMessage.contains("Imię płatnika jest wymaganym polem."),
                        "Alert does not contain required message."),
                () -> Assertions.assertTrue(errorMessage.contains("Nazwisko płatnika jest wymaganym polem."),
                        "Alert does not contain required message."),
                () -> Assertions.assertTrue(errorMessage.contains("Ulica płatnika jest wymaganym polem."),
                        "Alert does not contain required message."),
                () -> Assertions.assertTrue(errorMessage.contains("Kod pocztowy płatnika nie jest prawidłowym kodem pocztowym."),
                        "Alert does not contain required message."),
                () -> Assertions.assertTrue(errorMessage.contains("Miasto płatnika jest wymaganym polem."),
                        "Alert does not contain required message."),
                () -> Assertions.assertTrue(errorMessage.contains("Telefon płatnika jest wymaganym polem."),
                        "Alert does not contain required message."),
                () -> Assertions.assertTrue(errorMessage.contains("Adres email płatnika jest wymaganym polem."),
                        "Alert does not contain required message."),
                () -> Assertions.assertTrue(errorMessage.contains("Proszę przeczytać i zaakceptować regulamin sklepu aby móc sfinalizować zamówienie."),
                        "Alert does not contain required message.")
        );
    }

    @Test
    public void incorrectPhoneNumberTest() {
        fillCardFields(false);
        fillFormFields("abc", "correctmail@correctmail.com");
        placeOrder();
        String errorMessage = getErrorMessage();
        Assertions.assertTrue(errorMessage.contains("Telefon płatnika nie jest poprawnym numerem telefonu."));
    }

    @Test
    public void incorrectEmailTest() {
        fillCardFields(false);
        fillFormFields("555444333", "abcd");
        placeOrder();
        String errorMessage = getErrorMessage();
        Assertions.assertTrue(errorMessage.contains("Invalid email address, please correct and try again."));
    }

    @Test
    @Order(1)
    public void buyAndCreateAccountTest() {
        String email = "testaccount12345@testaccount.com";
        fillFormFields("555 444 333", email);
        driver.findElement(By.cssSelector("#createaccount")).click();
        By accountPassword = By.cssSelector("#account_password");
        String password = "testpassword!123";
        wait.until(ExpectedConditions.elementToBeClickable(accountPassword)).sendKeys(password);
        fillCardFields(true);
        placeOrder();
        String orderNumberSummary = waitAfterCorrectOrder();
        String orderEmailSummary = driver.findElement(By.cssSelector(".woocommerce-order-overview__email>strong")).getText();
        goToAccountOrders();
        int numberOfOrders = getNumberOfOrdersWithGivenNumber(orderNumberSummary);
        Assertions.assertAll(
                () -> Assertions.assertEquals(email, orderEmailSummary, "The e-mail does not match expected e-mail."),
                () -> Assertions.assertEquals(1, numberOfOrders, "Order with given number was not found.")
        );
    }

    @Test
    @Order(2)
    public void getOrdersTest() {
        // Requires an existing account with an order made, uses the account made in the buyAndCreateAccountTest method
        login("testaccount12345@testaccount.com", "testpassword!123");
        goToAccountOrders();
        Assertions.assertNotNull(driver.findElements(By.cssSelector("woocommerce-orders-table__cell-order-number")),
                "Orders were not found");
    }

    @Test
    @Order(3)
    public void loginAndBuyTripTest() {
        // Requires an existing account with an order made, uses the account made in the buyAndCreateAccountTest method
        login("testaccount12345@testaccount.com", "testpassword!123");
        fillCardFields(true);
        placeOrder();
        String orderNumberSummary = waitAfterCorrectOrder();
        goToAccountOrders();
        int numberOfOrders = getNumberOfOrdersWithGivenNumber(orderNumberSummary);
        Assertions.assertEquals(1, numberOfOrders, "Order with given number was not found.");
    }

    @Test
    @Order(4)
    public void deleteAccountTest() {
        login("testaccount12345@testaccount.com", "testpassword!123");
        driver.findElement(myAccount).click();
        driver.findElement(By.cssSelector(".delete-me")).click();
        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.titleIs("FakeStore – Sklep do nauki testowania"));
        String expectedURL = "https://fakestore.testelka.pl/";
        Assertions.assertEquals(expectedURL, driver.getCurrentUrl(), "Current URL does not match expected URL");
    }

    @Test
    public void buyWithoutAccountTest() {
        fillFormFields("555 444 223", "test123account@testtest.com");
        fillCardFields(true);
        placeOrder();
        Assertions.assertNotNull(waitAfterCorrectOrder(), "Order number was not found.");
    }

    @Test
    public void orderSummaryTest() {
        fillFormFields("555 444 333", "test123account@testtest.com");
        fillCardFields(true);
        placeOrder();
        waitAfterCorrectOrder();
        String orderDate = driver.findElement(orderDateSummary).getText();
        String expectedDate = getCurrentDate();
        String orderTotal = driver.findElement(orderTotalSummary).getText();
        String expectedTotal = "2 599,00 zł";
        String orderPayment = driver.findElement(orderPaymentSummary).getText();
        String expectedPayment = "Karta debetowa/kredytowa (Stripe)";
        String orderProductName = driver.findElement(orderProductNameSummary).getText();
        String expectedProductName = "Yoga i pilates w Portugalii";
        String orderProductQuantity = driver.findElement(orderProductQuantitySummary).getText();
        String expectedProductQuantity = "× 1";
        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedDate, orderDate,
                        "The date does not match expected date"),
                () -> Assertions.assertEquals(expectedTotal, orderTotal,
                        "Total price does not match expected value"),
                () -> Assertions.assertEquals(expectedPayment, orderPayment,
                        "Payment method does not match expected value"),
                () -> Assertions.assertEquals(expectedProductName, orderProductName,
                        "Product name does not match expected value"),
                () -> Assertions.assertEquals(expectedProductQuantity, orderProductQuantity,
                        "Product quantity does not match expected value")
        );
    }

    private void placeOrder() {
        wait.until(ExpectedConditions.numberOfElementsToBe(loadingBlock, 0));
        wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(orderButton))).click();
    }

    private String waitAfterCorrectOrder() {
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(ExpectedConditions.numberOfElementsToBe(loadingBlock, 0));
        wait.until(ExpectedConditions.urlContains("/zamowienie/zamowienie-otrzymane/"));
        return wait.until(ExpectedConditions.presenceOfElementLocated(orderNumberSummary)).getText();
    }

    private void goToAccountOrders() {
        driver.findElement(myAccount).click();
        wait.until(ExpectedConditions.elementToBeClickable(myOrders)).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".woocommerce-MyAccount-orders")));
    }

    private int getNumberOfOrdersWithGivenNumber(String orderNumber) {
        return driver.findElements(By.xpath("//a[contains(text(), '#" + orderNumber + "')]")).size();
    }

    private String getErrorMessage() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(emptyForm)).getText();
    }

    private void fillFormFields(String phoneNumber, String eMail) {
        driver.findElement(By.cssSelector("#billing_first_name")).sendKeys("Gandalf");
        driver.findElement(By.cssSelector("#billing_last_name")).sendKeys("Rudy");
        driver.findElement(By.cssSelector("#billing_address_1")).sendKeys("Ulica Sezamkowa 4");
        driver.findElement(By.cssSelector("#billing_postcode")).sendKeys("55-555");
        driver.findElement(By.cssSelector("#billing_city")).sendKeys("Nowy Jork");
        driver.findElement(By.cssSelector("#billing_phone")).sendKeys(phoneNumber);
        driver.findElement(By.cssSelector("#billing_email")).sendKeys(eMail);
    }

    private void fillCardFields(Boolean acceptTerms) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingBlock));
        WebElement cardNumberElement = findElementInFrame(cardNumberFrame, cardNumberField);
        cardNumberElement.sendKeys("4242424242424242");

        WebElement cardExpDate = findElementInFrame(cardExpirationDateFrame, cardExpirationDateField);
        cardExpDate.sendKeys("1121");
        WebElement cvcElement = findElementInFrame(cvcFrame, cvcField);
        cvcElement.sendKeys("666");
        driver.switchTo().defaultContent();
        if (acceptTerms) {
            driver.findElement(terms).click();
        }

    }

    private WebElement findElementInFrame(By frameLocator, By inputLocator) {
        driver.switchTo().defaultContent();
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
        return wait.until(ExpectedConditions.elementToBeClickable(inputLocator));
    }

    private void login(String username, String password) {
        driver.findElement(loginForm).click();
        wait.until(ExpectedConditions.elementToBeClickable(loginUser)).sendKeys(username);
        wait.until(ExpectedConditions.elementToBeClickable(loginPassword)).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
    }

    private String getCurrentDate() {
        Calendar date = Calendar.getInstance();
        return  date.get(Calendar.DAY_OF_MONTH) + " " +
                getPolishMonth(date.get(Calendar.MONTH)) + ", " + date.get(Calendar.YEAR);
    }

    private String getPolishMonth(int numberOfMonth) {
        String[] monthNames = {"stycznia", "lutego", "marca", "kwietnia", "maja", "czerwca",
                "lipca", "sieprnia", "września", "października", "listopada", "grudnia"};
        return monthNames[numberOfMonth];
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

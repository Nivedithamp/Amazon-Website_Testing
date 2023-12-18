import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.openqa.selenium.interactions.Actions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestAmazonSearchJunit {
    private static final Logger logger = LogManager.getLogger(TestAmazonSearchJunit.class);
    public static WebDriver driver;

    public static String myWebBrowserDriver = "webdriver.chrome.driver";
    public static String myDriverPath = "C:/Drivers/Selenium/chrome/chromedriver.exe";
    static String screenshotPath = "C:/Users/TEMP/Desktop";
    public static String url = "https://www.amazon.com/";
    int sleepTime = 5000;

    public static String item1 = "iPhone 13";
    public static String price1a = "$496.40";
    public static String price1b = "$530.38";
    public static String price1c = "$444.90";

    @BeforeClass
    public static void setup() {
        System.setProperty(myWebBrowserDriver, myDriverPath);
        driver = new ChromeDriver();
        driver.get(url);
        driver.manage().window().maximize();
    }

    @Test
    public void verifyBrowserMaximized(){
        Point position = driver.manage().window().getPosition();

        if (position.getX() == 0 && position.getY() == 0) {
            logger.info("Browser is maximized");
        } else {
            logger.info("Browser is not maximized");
        }

    }

    @Test
    public void verifyZeroItemsInCart() {

        // Find and click on the cart icon to view the cart
        WebElement cartIcon = driver.findElement(By.id("nav-cart-count"));
        cartIcon.click();

        // Wait for the cart page to load and get the count of items in the cart
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement cartItemCountElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".sc-your-amazon-cart-is-empty")));
        String cartItemCountText = cartItemCountElement.getText();

        // Assert that the item count in the cart is zero
        Assert.assertEquals(cartItemCountText,"Your Amazon Cart is empty");
        System.out.println("Amazon Cart: " + cartItemCountText);
    }

    @AfterClass
    public static void tearDown(){
        driver.quit();
    }

    @Test
    public void searchItemsFromExcel() throws Exception {
        // Read data from Excel
        String excelFilePath = "C:/Users/TEMP/Documents/PriceData.xlsx";
        List<Map<String, String>> testData = ExcelReader.readTestData(excelFilePath);

        for (Map<String, String> data : testData) {
            for(String key: data.keySet()) {
                System.out.print(key+" : ");
                System.out.println(data.get(key));
            }
        }

        for (Map<String, String> data : testData) {
            String item = data.get("Item");
            String expectedPriceData = data.get("ExpectedPrices");

            if (expectedPriceData != null) {
                String[] expectedPrices = expectedPriceData.split(",");
                searchAndAssert(item, expectedPrices);
                takeSnapShot(driver, screenshotPath + "/searchItemsFromExcel" + item + ".png");
            } else {

            }
        }
    }

    @Test
    public void searchItem1() throws InterruptedException, Exception {
        searchAndAssert(item1, price1a, price1b, price1c);
        takeSnapShot(driver, screenshotPath + "//test1_searchItem.png");
    }

    @Test
    public void comparePrice() throws InterruptedException, Exception {
//        WebElement priceElement = driver.findElement(By.cssSelector("span.a-offscreen"));
        WebElement priceElement = driver.findElement(By.xpath("//div[@id='sw-subtotal']//span[contains(@class, 'a-price-whole')]"));
        String priceText = priceElement.getText();
        String expectedPrice = "112";

        if (priceText.equals(expectedPrice)) {
            System.out.println("Prices match! Actual price: " + priceText);
        } else {
            System.out.println("Prices do not match. Expected price: " + expectedPrice + ", Actual price: " + priceText);
        }
    }

    @Test
    public void compareColor() throws InterruptedException, Exception {
        WebElement colorElement = driver.findElement(By.xpath("//span[@class='a-list-item']/span[contains(text(), 'Color')]/following-sibling::span[@class='a-size-base']"));
        String colorValue = colorElement.getText();
        String expectedColor = "Light Pink";
        System.out.println("Color: " + colorValue);
    }

    @Test
    public void verifyCartCount() throws InterruptedException, Exception {
        // Find the element by ID
        WebElement cartCountElement = driver.findElement(By.id("nav-cart-count"));
        String cartCountValue = cartCountElement.getText();
        int itemCount = Integer.parseInt(cartCountValue);
        Assert.assertEquals( itemCount, 3);
        System.out.println("Items in the cart: " + itemCount);
    }

    private void searchAndAssert(String item, String... expectedPrices) {
        // Clear the search box, enter the item, and submit the search
        driver.findElement(By.id("twotabsearchtextbox")).clear();
        driver.findElement(By.id("twotabsearchtextbox")).sendKeys(item);
        driver.findElement(By.id("nav-search-submit-button")).click();

        // Wait for the search results to load
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".s-main-slot .s-result-item")));

        // Click on the first search result to view the product details
        WebElement firstSearchResult = driver.findElement(By.cssSelector(".s-main-slot .s-result-item"));
        firstSearchResult.click();
        // Find the hidden input element by its name attribute


        // Click on the "Add to Cart" button with retry mechanism for StaleElementReferenceException
        boolean found = false;
        for (String expectedPrice : expectedPrices) {
            try {
                wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement quantityInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("items[0.base][quantity]")));

// Use JavaScript to set the value of the hidden input field
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].setAttribute('value', 3)", quantityInput); // Set the quantity to 2
                WebElement element = driver.findElement(By.id("inline-twister-expanded-dimension-text-color_name"));

// Use JavaScript Executor to change the color of the product
                String color = "Light Pink";
                String script = "arguments[0].style.color = '" + color + "';";
                ((JavascriptExecutor) driver).executeScript(script, element);
                WebElement addtocart = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='add-to-cart-button']")));
                addtocart.click();
                found = true;
                break;
            } catch (StaleElementReferenceException e) {
                // Retry finding and clicking the element
                System.out.println("StaleElementReferenceException occurred. Retrying...");
            }
        }

        // Additional verifications/assertions if needed
        Assert.assertTrue("Expected price not found for item: " + item, found);
    }

    public void takeSnapShot(WebDriver webdriver, String fileWithPath) throws Exception {
        // Convert web driver object to TakeScreenshot
        TakesScreenshot scrShot = ((TakesScreenshot) webdriver);

        // Call getScreenshotAs method to create an image file
        File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);

        // Move the image file to a new destination
        File DestFile = new File(fileWithPath);

        // Copy the file to the destination
        FileUtils.copyFile(SrcFile, DestFile);
    }

}



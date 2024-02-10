package support;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.http.ClientConfig;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

public class Browser {
    public static ChromeDriver driver;
    public static Dotenv env;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");

    public static String url;
    public static Date startedAt;
    public static Date finishedAt;
    public static String fromWallet = "";
    public static String toWallet = "";
    public static String fromNetwork = "";
    public static String toNetwork = "";
    public static String fromAmount = "";
    public static String toAmount = "";
    public static String fromAsset = "";
    public static String toAsset = "";
    public static String route = "";
    public static String txFrom = "";
    public static String txTo = "";
    public static String fromBalance = "";
    public static String toBalance = "";
    public static String toFinalBalance = "";

    public static int waitSeconds = 0;
    public static String toNativeBalance = "";
    public static String toFinalNativeBalance = "";
    public static boolean metaMaskWasUnlocked = false;
    public static boolean phantomWasUnlocked = false;

    public static void main(String[] args) {
        launch();

        Browser.driver.get(Browser.env.get("URL_WORMHOLE_CONNECT_TESTNET"));
        Browser.driver.findElement(By.cssSelector("form [type='password']")).sendKeys(Browser.env.get("WORMHOLE_PAGE_PASSWORD"));
        Browser.driver.findElement(By.cssSelector("form button.button")).click();
    }

    public static void launch() {
        System.out.println("Browser.launch");

        env = Dotenv.load();

        ChromeOptions opt = new ChromeOptions();
        if (System.getProperty("os.name").startsWith("Windows")) {
            opt.setBinary("C:/Program Files/chrome-win64-114.0.5735.90/chrome.exe");
        } else {
            opt.setBinary("/Applications/Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing");
        }
        opt.addArguments("user-data-dir=" + Paths.get("chrome_profile").toAbsolutePath());
        opt.addArguments("profile-directory=Default");
        ClientConfig config = ClientConfig.defaultConfig().readTimeout(Duration.ofHours(2));
        driver = new ChromeDriver(ChromeDriverService.createDefaultService(), opt, config);
        implicitlyWait();
    }

    public static void quit() {
        driver.quit();
    }

    public static void noImplicitWait() {
        implicitlyWait(0);
    }

    public static void implicitlyWait() {
        implicitlyWait(10);
    }

    public static void implicitlyWait(int seconds) {
        System.out.println("Implicit wait set to " + seconds + "s");
        Browser.waitSeconds = seconds;
    }

    public static boolean extensionWindowIsOpened() {
        return driver.getWindowHandles().toArray().length > 1;
    }

    public static void waitForExtensionWindowToAppear() {
        waitForExtensionWindowToAppear(60);
    }

    public static void waitForExtensionWindowToAppear(int seconds) {
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
        wait.until(d -> extensionWindowIsOpened());
        switchToExtensionWindow();
    }

    public static void waitForExtensionWindowToDisappear() {
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(d -> !extensionWindowIsOpened());
        switchToMainWindow();
    }

    public static void switchToExtensionWindow() {
        System.out.println("Switching to extension window...");
        driver.switchTo().window((String) driver.getWindowHandles().toArray()[1]);
    }

    public static void switchToMainWindow() {
        driver.switchTo().window((String) driver.getWindowHandles().toArray()[0]);
    }

    public static void scrollToElement(WebElement element) {
        Actions actions = new Actions(Browser.driver);
        actions.moveToElement(element);
        actions.perform();
    }

    public static void takeScreenshot() {
        try {
            File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File destination = new File("screenshots/" + date + "_test_failed.png");

            FileUtils.copyFile(screenshotFile, destination);

            System.out.println("Saved screenshot to: " + destination.getAbsolutePath());
        } catch (WebDriverException e) {
            System.err.println("Could not save screenshot");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveResults(String status) {
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String s = Browser.url + ";" +
                status + ";" +
                dt.format(Browser.startedAt) + ";" + dt.format(Browser.finishedAt) + ";" +
                Browser.route + ";" +
                Browser.fromNetwork + ";" + Browser.fromWallet + ";" +
                Browser.toNetwork + ";" + Browser.toWallet + ";" +
                Browser.fromAmount + ";" + Browser.fromAsset + ";" +
                Browser.toAmount + ";" + Browser.toAsset + ";" +
                Browser.txFrom + ";" + Browser.txTo + ";" +
                Browser.fromBalance + ";" +
                Browser.toBalance + ";" +
                Browser.toFinalBalance + ";" +
                Browser.toNativeBalance + ";" +
                Browser.toFinalNativeBalance + "\n";
        try {
            File f = new File("results/results.csv");
            f.getParentFile().mkdirs();
            f.createNewFile();
            Files.write(f.toPath(), s.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static WebElement findElementAndWait(By locator) throws NoSuchElementException {
        WebDriverWait webDriverWait = new WebDriverWait(Browser.driver, Duration.ofSeconds(Browser.waitSeconds));
        try {
            WebElement el = webDriverWait
                    .until((webDriver) -> {
                        return Browser.driver.findElement(locator);
                    });
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignore) {
            }
            return el;
        } catch (TimeoutException ex) {
            throw new NoSuchElementException("Element was not found.", ex);
        }
    }

    public static void waitToBeClickable(WebElement el) throws NoSuchElementException {
        WebDriverWait webDriverWait = new WebDriverWait(Browser.driver, Duration.ofSeconds(Browser.waitSeconds));
        try {
            webDriverWait
                    .until(ExpectedConditions.elementToBeClickable(el));
        } catch (TimeoutException ex) {
            throw new NoSuchElementException("Element cannot be clicked.", ex);
        }
    }

    public static String findElementAndWaitToHaveNumber(By locator) throws NoSuchElementException {
        WebDriverWait webDriverWait = new WebDriverWait(Browser.driver, Duration.ofSeconds(60));
        try {
            return webDriverWait.
                    ignoring(NumberFormatException.class)
                    .until(webDriver -> {
                        WebElement found = webDriver.findElement(locator);
                        String text = found.getText().replaceAll("\n.*", "");
                        Double.parseDouble(text);
                        return text;
                    });
        } catch (TimeoutException ex) {
            throw new NoSuchElementException("Element does not contain a number.", ex);
        }
    }

    public static void confirmTransactionInMetaMask() throws InterruptedException {
        Browser.waitForExtensionWindowToAppear();

        Browser.implicitlyWait(2);
        System.out.println("Going to Approve adding new network (if MetaMask requires it)...");
        try {
            Browser.findElementAndWait(By.xpath("//*[text()='Approve']")).click();
            Thread.sleep(2000);
        } catch (NoSuchElementException ignore) {
        }
        System.out.println("Going to confirm warning on Moonbase network (if MetaMask requires it)...");
        try {
            Browser.findElementAndWait(By.xpath("//*[text()='Got it']")).click();
            Thread.sleep(2000);
        } catch (NoSuchElementException ignore) {
        }
        System.out.println("Going to Switch network (if MetaMask requires it)...");
        try {
            Browser.findElementAndWait(By.xpath("//*[text()='Switch network']")).click();
            Thread.sleep(2000);
        } catch (NoSuchElementException ignore) {
        }
        Browser.implicitlyWait();

        System.out.println("Confirming transaction in MetaMask...");

        WebDriverWait webDriverWait = new WebDriverWait(Browser.driver, Duration.ofSeconds(900));
        webDriverWait
                .until(webDriver -> {
                    if (Browser.extensionWindowIsOpened()) {
                        Browser.switchToExtensionWindow();
                        WebElement metamaskFooterButton = Browser.findElementAndWait(By.cssSelector("[data-testid='page-container-footer-next']"));
                        String buttonText = metamaskFooterButton.getText();
                        if (buttonText.equals("Next")) {
                            metamaskFooterButton.click();
                        } else if (buttonText.equals("Approve")) {
                            metamaskFooterButton.click();
                            Browser.waitForExtensionWindowToDisappear();
                        } else if (buttonText.equals("Confirm")) {
                            Browser.waitToBeClickable(metamaskFooterButton);
                            metamaskFooterButton.click();
                            Browser.waitForExtensionWindowToDisappear();
                            return metamaskFooterButton;
                        }

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ignore) {
                        }
                    }
                    return null;
                });

        System.out.println("Transaction was confirmed in MetaMask");
        Browser.switchToMainWindow();
    }

    public static String getScanLinkTextByNetworkName(String network) {
        switch (network) {
            case "Goerli":
            case "Ethereum":
                return "Etherscan";
            case "Mumbai":
            case "Polygon":
                return "Polygonscan";
            case "BSC":
                return "BscScan";
            case "Fuji":
            case "Avalanche":
                return "Avascan";
            case "Fantom":
                return "FtmScan";
            case "Alfajores":
            case "Celo":
                return "Celo Explorer";
            case "Moonbase":
            case "Moonbeam":
                return "Moonscan";
            case "Base Goerli":
            case "Base":
                return "BaseScan";
            case "Arbitrum Goerli":
            case "Arbitrum":
                return "Arbitrum Goerli Explorer";
            case "Optimism Goerli":
            case "Optimism":
                return "Optimism Goerli";
            case "Solana":
                return "Solana Explorer";
        }
        throw new RuntimeException("Unsupported network: " + network);
    }


    public static String getNativeAssetByNetworkName(String network) {
        switch (network) {
            case "Goerli":
                return "ETH";
            case "Mumbai":
                return "MATIC";
            case "BSC":
                return "BNB";
            case "Fuji":
                return "AVAX";
            case "Fantom":
                return "FTM";
            case "Alfajores":
                return "CELO";
            case "Moonbase":
                return "GLMR";
            case "Base Goerli":
                return "ETH";
            case "Arbitrum Goerli":
                return "ETH";
            case "Optimism Goerli":
                return "ETH";
        }
        throw new RuntimeException("Unsupported network: " + network);
    }

    public static void selectAssetInFromSection(String wallet, String network, String asset) throws InterruptedException {
        Browser.findElementAndWait(By.xpath("//*[text()='Connect wallet']")).click();
        Browser.findElementAndWait(By.xpath("//*[text()='" + wallet + "']")).click();

        if (wallet.equals("MetaMask") && !Browser.metaMaskWasUnlocked) {
            Browser.waitForExtensionWindowToAppear();

            Browser.findElementAndWait(By.cssSelector("[data-testid='unlock-password']")).sendKeys(Browser.env.get("WALLET_PASSWORD_METAMASK"));
            Browser.findElementAndWait(By.cssSelector("[data-testid='unlock-submit']")).click();

            try {
                System.out.println("Going to Reject a pending transaction (if it exists)...");
                Browser.implicitlyWait(3);
                Browser.findElementAndWait(By.cssSelector("[data-testid='page-container-footer-cancel']")).click();
                Browser.implicitlyWait();
            } catch (NoSuchElementException ignore) {
            }

            Browser.waitForExtensionWindowToDisappear();
            Thread.sleep(1000);

            Browser.metaMaskWasUnlocked = true;
        }

        Browser.findElementAndWait(By.xpath("//*[text()='Select network']")).click();
        Thread.sleep(1000);
        Browser.findElementAndWait(By.xpath("//*[text()='" + network + "']")).click();
        Thread.sleep(1000);
        Browser.findElementAndWait(By.xpath("//*[text()='Select']")).click();
        Thread.sleep(1000);
        Browser.findElementAndWait(By.xpath("//*[text()='" + asset + "']")).findElement(By.xpath("../../..")).click();
        Thread.sleep(1000);
    }

    public static void moveSliderByOffset(int xOffset) throws InterruptedException {
        WebElement slider = Browser.findElementAndWait(By.cssSelector(".MuiSlider-thumb"));
        Browser.scrollToElement(slider);

        (new Actions(Browser.driver))
                .clickAndHold(slider)
                .moveByOffset(xOffset, 0)
                .release()
                .build()
                .perform();
    }
}

package steps;

import pages.WormholePage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.ExtensionPage;
import pages.PasswordPage;
import support.Browser;
import support.BrowserMainnet;

import java.time.Duration;

import static junit.framework.TestCase.assertTrue;

public class WormholeConnectSteps {

    @Given("I launch testnet browser")
    public void iLaunchBrowser() {
        Browser.launch();
    }

    @Given("I launch mainnet browser")
    public void iLaunchMainnetBrowser() {
        Browser.isMainnet = true;
        BrowserMainnet.launch();
    }

    @Given("I open wormhole-connect testnet")
    public void iOpenWormholeConnectTestnetPage() {
        Browser.url = Browser.env.get("URL_WORMHOLE_CONNECT_TESTNET");
        Browser.driver.get(Browser.url);
    }

    @Given("I enter page password")
    public void iEnterPassword() {
        Browser.findElement(PasswordPage.passwordInput).sendKeys(Browser.env.get("WORMHOLE_PAGE_PASSWORD"));
        Browser.findElement(PasswordPage.button).click();
    }

    @Given("I open wormhole-connect mainnet")
    public void iOpenWormholeConnectMainnetPageAndEnterPassword() {
        Browser.url = Browser.env.get("URL_WORMHOLE_CONNECT_MAINNET");
        Browser.driver.get(Browser.url);
    }

    @Given("I open portal bridge mainnet")
    public void iOpenPortalBridgeMainnet() {
        Browser.url = Browser.env.get("URL_PORTAL_BRIDGE_MAINNET");
        Browser.driver.get(Browser.url);
    }

    @And("I prepare to send {string} {string} from {string}\\({string}) to {string}\\({string}) with {string} route")
    public void iFillInTransactionDetails(String amount, String asset, String fromNetwork, String fromWallet, String toNetwork, String toWallet, String route) throws InterruptedException {
        Browser.fromWallet = fromWallet;
        Browser.toWallet = toWallet;
        Browser.fromNetwork = fromNetwork;
        Browser.toNetwork = toNetwork;
        Browser.fromAmount = amount;
        Browser.fromAsset = asset;
        Browser.route = route;
        Browser.txFrom = "";
        Browser.txTo = "";

        System.out.println("I prepare to send " + Browser.fromAmount + " " + Browser.fromAsset + " from " + Browser.fromNetwork + " to " + Browser.toWallet);

        if (Browser.route.equals("automatic")) {
            Assert.assertNotEquals("Native balance was not checked", "", Browser.toNativeBalance);
        }

        Browser.selectAssetInFromSection(Browser.fromWallet, Browser.fromNetwork, Browser.fromAsset);

        Browser.findElement(WormholePage.DESTINATION_SELECT_NETWORK_BUTTON).click();
        Thread.sleep(1000);
        Browser.findElement(WormholePage.CHOOSE_NETWORK(Browser.toNetwork)).click();
        Thread.sleep(1000);

        Browser.findElement(WormholePage.DESTINATION_CONNECT_WALLET_BUTTON).click();
        Browser.findElement(WormholePage.CHOOSE_WALLET(Browser.toWallet)).click();

        if (Browser.toWallet.equals("MetaMask") && !Browser.metaMaskWasUnlocked) {
            Browser.waitForExtensionWindowToAppear();

            Browser.findElement(ExtensionPage.METAMASK_PASSWORD_INPUT).sendKeys(Browser.env.get("WALLET_PASSWORD_METAMASK"));
            Browser.findElement(ExtensionPage.METAMASK_UNLOCK_BUTTON).click();

            Browser.waitForExtensionWindowToDisappear();
            Browser.metaMaskWasUnlocked = true;
        }

        Browser.findElement(WormholePage.SOURCE_AMOUNT_INPUT).sendKeys(amount);
        Thread.sleep(1000);

        Browser.fromBalance = Browser.findElementAndWaitToHaveNumber(WormholePage.SOURCE_BALANCE_TEXT);

        try {
            Browser.findElement(WormholePage.POPUP_CLOSE_BUTTON).click();
        } catch (Exception ignore) {
        }

        Browser.toAsset = Browser.findElement(WormholePage.DESTINATION_ASSET_BUTTON).getText();
        Browser.toAsset = Browser.toAsset.split("\n")[0]; // "CELO\n(Alfajores)" -> "CELO"
        if (Browser.route.equals("eth-bridge-automatic") || Browser.route.equals("wst-eth-bridge-automatic")) {
            Browser.toAmount = Browser.findElement(WormholePage.DESTINATION_AMOUNT_INPUT_ETH_BRIDGE).getAttribute("value");
        } else {
            Browser.toAmount = Browser.findElement(WormholePage.DESTINATION_AMOUNT_INPUT).getAttribute("value");
        }
        Browser.toBalance = Browser.findElementAndWaitToHaveNumber(WormholePage.DESTINATION_BALANCE_TEXT);

        switch (route) {
            case "xlabs-bridge-automatic":
                // choose Manual and then again Automatic to enable native gas section
                Browser.findElement(WormholePage.AUTOMATIC_BRIDGE_OPTION).click();
                Thread.sleep(1000);
                Browser.findElement(WormholePage.MANUAL_BRIDGE_OPTION).click();
                Thread.sleep(1000);
                Browser.findElement(WormholePage.AUTOMATIC_BRIDGE_OPTION).click();
                Thread.sleep(1000);
                break;
            case "wormhole-bridge-manual":
                Browser.findElement(WormholePage.MANUAL_BRIDGE_OPTION).click();
                break;
            case "cosmos-manual":
                Browser.findElement(WormholePage.COSMOS_MANUAL_GATEWAY_OPTION).click();
                break;
            case "cosmos-automatic":
                Browser.findElement(WormholePage.COSMOS_AUTOMATIC_GATEWAY_OPTION).click();
                break;
            case "circle-manual":
                Browser.findElement(WormholePage.CIRCLE_MANUAL_OPTION).click();
                break;
            case "circle-automatic":
                // choose Manual and then again Automatic to enable native gas section
                Browser.findElement(WormholePage.CIRCLE_AUTOMATIC_OPTION).click();
                Thread.sleep(1000);
                Browser.findElement(WormholePage.CIRCLE_MANUAL_OPTION).click();
                Thread.sleep(1000);
                Browser.findElement(WormholePage.CIRCLE_AUTOMATIC_OPTION).click();
                Thread.sleep(1000);
                break;
            case "eth-bridge-automatic":
                Browser.findElement(WormholePage.ETH_BRIDGE_AUTOMATIC_OPTION).click();
                break;
            case "wst-eth-bridge-automatic":
                Browser.findElement(WormholePage.ETH_BRIDGE_AUTOMATIC_OPTION).click();
                break;
        }

        Thread.sleep(3000); // wait UI to settle
    }

    @Then("I check balance has increased on destination chain")
    public void iCheckFinalBalance() throws InterruptedException {
        Browser.driver.get(Browser.url);
        if (Browser.toNetwork.equals("Solana")) {
            System.out.println("Waiting 20 seconds to receive asset on Solana");
            Thread.sleep(20000);
        }
        System.out.println("Checking " + Browser.toAsset + " balance on " + Browser.toNetwork + " (" + Browser.toWallet + ")");
        Browser.selectAssetInFromSection(Browser.toWallet, Browser.toNetwork, Browser.toAsset);

        Browser.toFinalBalance = Browser.findElementAndWaitToHaveNumber(WormholePage.SOURCE_BALANCE_TEXT);
        System.out.println(Browser.toFinalBalance + " " + Browser.toAsset);

        if (Browser.route.equals("automatic") || Browser.route.equals("circle-automatic")) {
            String nativeAsset = Browser.getNativeAssetByNetworkName(Browser.toNetwork);

            System.out.println("Checking native asset (" + nativeAsset + ") balance on " + Browser.toNetwork + " (" + Browser.toWallet + ")");
            Browser.findElement(WormholePage.OPEN_ASSET_LIST()).click();
            Browser.findElement(WormholePage.CHOOSE_ASSET(nativeAsset)).click();

            Browser.toFinalNativeBalance = Browser.findElementAndWaitToHaveNumber(WormholePage.SOURCE_BALANCE_TEXT);
            System.out.println(Browser.toFinalNativeBalance + " " + nativeAsset);

            Assert.assertTrue("Native balance should have increased", Double.parseDouble(Browser.toFinalNativeBalance) > Double.parseDouble(Browser.toNativeBalance));
        }
        Assert.assertTrue("Balance should have increased", Double.parseDouble(Browser.toFinalBalance) > Double.parseDouble(Browser.toBalance));
    }

    @And("I check native balance on {string} using {string}")
    public void iCheckNativeBalanceOnToNetworkUsingToWallet(String toNetwork, String toWallet) throws InterruptedException {
        String nativeAsset = Browser.getNativeAssetByNetworkName(toNetwork);

        System.out.println("Checking native asset (" + nativeAsset + ") balance on " + toNetwork + " (" + toWallet + ")");
        Browser.selectAssetInFromSection(toWallet, toNetwork, nativeAsset);

        Browser.toNativeBalance = Browser.findElementAndWaitToHaveNumber(WormholePage.SOURCE_BALANCE_TEXT);
        System.out.println(Browser.toNativeBalance + " " + nativeAsset);
    }

    @When("I click on Approve button")
    public void iApproveTransfer() throws InterruptedException {
        WebElement approveButton = Browser.findElement(WormholePage.APPROVE_BUTTON);
        Browser.scrollToElement(approveButton);
        Thread.sleep(5000);
        approveButton.click();
        Thread.sleep(2000);
    }

    @When("I approve wallet notifications")
    public void iApproveWalletNotification() throws InterruptedException {
        System.out.println("Going to confirm transaction...");

        switch (Browser.fromWallet) {
            case "MetaMask":
                Browser.confirmTransactionInMetaMask(false);

                break;
            case "Phantom":
                Browser.waitForExtensionWindowToAppear();

                Browser.findElement(ExtensionPage.PHANTOM_PASSWORD_INPUT).sendKeys(Browser.env.get("WALLET_PASSWORD_PHANTOM"));
                Browser.findElement(ExtensionPage.PHANTOM_SUBMIT_BUTTON).click();
                Thread.sleep(1000);

                WebElement link = Browser.findElementIgnoreIfMissing(10, ExtensionPage.IGNORE_WARNING_PROCEED_ANYWAY_LINK);
                if (link != null ) {
                    link.click();
                }
                Browser.findElement(ExtensionPage.PHANTOM_PRIMARY_BUTTON).click(); // Confirm


                Browser.waitForExtensionWindowToDisappear();
                break;
            case "Sui":
                Browser.waitForExtensionWindowToAppear();
                Browser.findElement(ExtensionPage.SUI_UNLOCK_TO_APPROVE_BUTTON).click();

                Browser.findElement(ExtensionPage.SUI_PASSWORD_INPUT).sendKeys(Browser.env.get("WALLET_PASSWORD_SUI"));
                Browser.findElement(ExtensionPage.SUI_UNLOCK_BUTTON).click();
                Thread.sleep(1000);

                Browser.findElement(ExtensionPage.SUI_APPROVE_BUTTON).click();
                Thread.sleep(1000);

                try {
                    Browser.findElement(ExtensionPage.SUI_DIALOG_APPROVE_BUTTON).click();
                } catch (NoSuchElementException ignore) {
                }
                Browser.waitForExtensionWindowToDisappear();
                break;
            case "Leap":
                Browser.waitForExtensionWindowToAppear();
                Browser.findElement(ExtensionPage.LEAP_APPROVE_BUTTON).click();
                Thread.sleep(1000);

                Browser.waitForExtensionWindowToDisappear();
                break;
        }

        WebDriverWait webDriverWait = new WebDriverWait(Browser.driver, Duration.ofSeconds(120));
        webDriverWait.until(webDriver -> {
            if (Browser.findElementIgnoreIfMissing(0, WormholePage.REDEEM_SCREEN_HEADER) != null) {
                return true;
            }
            WebElement errorMessage = Browser.findElementIgnoreIfMissing(0, WormholePage.APPROVE_ERROR_MESSAGE);
            if (errorMessage != null) {
                Assert.fail("Transaction failed: " + errorMessage.getText());
            }
            return null;
        });
    }

    @Then("I should see Send From link")
    public void iShouldSeeSendFromLink() {
        Browser.findElement(120, WormholePage.REDEEM_SCREEN_HEADER);

        System.out.println("Waiting for the send from link...");
        WebElement sendFromLink = Browser.findElement(3600, WormholePage.SOURCE_SCAN_LINK());

        Browser.txFrom = sendFromLink.getAttribute("href");
    }

    @Then("I should claim assets")
    public void iShouldClaimAssets() throws InterruptedException {
        if (Browser.route.equals("wormhole-bridge-manual") || Browser.route.equals("circle-manual") || Browser.route.equals("cosmos-manual") ) {
            System.out.println("Waiting for the Claim button...");
            Browser.findElement(3600, WormholePage.CLAIM_BUTTON);
            Thread.sleep(5000);
            Browser.findElement(WormholePage.CLAIM_BUTTON).click();
            Thread.sleep(2000);

            if (Browser.toWallet.equals("Phantom")) {
                Browser.waitForExtensionWindowToAppear();

                Browser.findElement(ExtensionPage.PHANTOM_PASSWORD_INPUT).sendKeys(Browser.env.get("WALLET_PASSWORD_PHANTOM"));
                Browser.findElement(ExtensionPage.PHANTOM_SUBMIT_BUTTON).click();

                WebDriverWait webDriverWait = new WebDriverWait(Browser.driver, Duration.ofSeconds(900));
                webDriverWait
                        .until(webDriver -> {
                            if (Browser.extensionWindowIsOpened()) {
                                Browser.switchToExtensionWindow();
                                WebElement link = Browser.findElementIgnoreIfMissing(1, ExtensionPage.IGNORE_WARNING_PROCEED_ANYWAY_LINK);
                                if (link != null ) {
                                    link.click();
                                    return null;
                                }
                                Browser.findElement(ExtensionPage.PHANTOM_PRIMARY_BUTTON).click(); // Confirm
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException ignore) {
                                }
                                Browser.switchToMainWindow();
                                return null;
                            }
                            return Browser.driver.findElement(WormholePage.TRANSACTION_COMPLETE_MESSAGE);
                        });

                Browser.waitForExtensionWindowToDisappear();
            } else if (Browser.toWallet.equals("Sui")) {
                Browser.waitForExtensionWindowToAppear();
                Browser.findElement(ExtensionPage.SUI_UNLOCK_TO_APPROVE_BUTTON).click();

                Browser.findElement(ExtensionPage.SUI_PASSWORD_INPUT).sendKeys(Browser.env.get("WALLET_PASSWORD_SUI"));
                Browser.findElement(ExtensionPage.SUI_UNLOCK_BUTTON).click();
                Thread.sleep(1000);

                Browser.findElement(ExtensionPage.SUI_APPROVE_BUTTON).click();
                Thread.sleep(1000);

                try {
                    Browser.findElement(ExtensionPage.SUI_DIALOG_APPROVE_BUTTON).click();
                } catch (NoSuchElementException ignore) {
                }
                Browser.waitForExtensionWindowToDisappear();
            } else {
                Browser.confirmTransactionInMetaMask(true);
            }
        }
    }

    @Then("I should see Send To link")
    public void iShouldSeeSendToLink() {
        int waitSeconds;
        if (Browser.route.equals("automatic") || Browser.route.equals("circle-automatic")) {
            waitSeconds = 60 * 30;
        } else {
            if (Browser.toWallet.equals("Phantom")) {
                waitSeconds = 60;
            } else {
                waitSeconds = 60 * 30;
            }
        }

        System.out.println("Waiting for the send to link...");
        WebElement sendToLink = Browser.findElement(waitSeconds, WormholePage.DESTINATION_SCAN_LINK());

        assertTrue(sendToLink.isDisplayed());

        Browser.txTo = sendToLink.getAttribute("href");

        System.out.println("Finished");
    }

    @And("I move slider")
    public void iMoveSlider() throws InterruptedException {
        Browser.moveSliderByOffset(220);
    }
}

import com.applitools.eyes.*;
import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.selenium.Configuration;
import com.applitools.eyes.selenium.StitchMode;
import com.applitools.eyes.selenium.fluent.SeleniumCheckSettings;
import com.applitools.eyes.selenium.fluent.Target;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.visualgrid.services.VisualGridRunner;
import org.testng.annotations.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class BrowserTester {

    RemoteWebDriver driver;
    Eyes eyes;
    VisualGridRunner runner;
    static BatchInfo batch = new BatchInfo("Bad Resolution Test");

    String[][] urls = {
           // {"Capital One - Homepage", "https://www.capitalone.com", ""},
            //{"Applitools - Homepage", "https://applitools.com/", ""},
            {"PNC - Homepage", "https://www.pnc.com/en/personal-banking.html", "//*[@id='hero-button']"},
           // {"NYL - Homepage", "https://www.newyorklife.com/", ""},
            //{"NYL - Term Life", "https://www.newyorklife.com/learn-and-compare/compare-products/term-whole-life-insurance", ""},
            //{"NYL - Claims", "https://www.newyorklife.com/claims", ""},
           // {"NYL - Mutual Funds", "https://www.newyorklife.com/products/investments/mutual-funds", ""},
           // {"BoFA - Homepage", "https://www.bankofamerica.com", ""},
            //{"Hello World", "https://applitools.com/helloworld?diff1", "/html/body/div/div[2]/p[4]/span[2]"}
    };

    @BeforeMethod
    void beforeMethod(ITestContext context) {
        Map<String, String> params = context.getCurrentXmlTest().getAllParameters();

        try {
            createDriver(context);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @BeforeSuite
    void beforeSuite(){
        System.out.println("Browser Tester Started");
    }

    @AfterClass(alwaysRun = true)
    void afterClass() {
        eyes.abortIfNotClosed();
    }

    @AfterSuite
    void afterSuite() {}

    public void createDriver(ITestContext context) throws MalformedURLException {
        Map<String, String> params = context.getCurrentXmlTest().getAllParameters();

        System.out.println("Creating driver: " + params.get("targetEnvironment"));

        runner = new VisualGridRunner(100);

        if (Boolean.parseBoolean(params.get("visualgrid"))) {
            eyes = new Eyes(runner);
            Configuration conf = new Configuration();
            conf.setTestName("Browser Tester - VG");
            conf.setAppName("Browser Tester - New");
            conf.addBrowser(800, 600, BrowserType.CHROME);
            conf.addBrowser(700, 500, BrowserType.CHROME);
            conf.addBrowser(1200, 800, BrowserType.CHROME);
            conf.addBrowser(1600, 1200, BrowserType.CHROME);
            conf.addBrowser(800, 600, BrowserType.FIREFOX);
            conf.addBrowser(700, 500, BrowserType.FIREFOX);
            conf.addBrowser(1200, 800, BrowserType.FIREFOX);
            conf.addBrowser(1600, 1200, BrowserType.FIREFOX);
            conf.addBrowser(800, 600, BrowserType.EDGE);
            conf.addBrowser(700, 500, BrowserType.EDGE);
            conf.addBrowser(1200, 800, BrowserType.EDGE);
            conf.addBrowser(1600, 1200, BrowserType.EDGE);
            conf.addBrowser(800, 600, BrowserType.IE_11);
            conf.addBrowser(700, 500, BrowserType.IE_11);
            conf.addBrowser(1200, 800, BrowserType.IE_11);
            conf.addBrowser(1600, 1200, BrowserType.IE_11);

            eyes.setConfiguration(conf);
        } else {
            eyes = new Eyes();
        }


        eyes.setLogHandler(new FileLogger("target/logging/" + params.get("targetEnvironment") + ".log",false,true));
        eyes.setApiKey(System.getProperty("APIKey"));
        eyes.setBatch(batch);
        eyes.setHideScrollbars(true);
        eyes.setForceFullPageScreenshot(true);
        eyes.setStitchMode(StitchMode.CSS);
       // eyes.setBaselineBranchName("BrowserTesterMain");
        //eyes.setBaselineEnvName("Browser Tester - Win 10");
        eyes.setMatchLevel(MatchLevel.STRICT);

        ChromeOptions options = new ChromeOptions();

        options.setCapability("version", params.get("version"));
        options.setCapability("platform", params.get("platform"));

        long startTime = System.nanoTime();
        if(Boolean.parseBoolean(params.get("useSauce"))){
            System.out.println("Environment: Sauce Labs");
            options.setCapability("screenResolution", "1280x960");
            driver = new RemoteWebDriver(new URL("http://matan:ec79e940-078b-41d4-91a6-d7d6008cf1ea@ondemand.saucelabs.com/wd/hub"), options);
            System.out.println(params.get("targetEnvironment") + ": " + (System.nanoTime() - startTime) / 1000000);
        } else {
            System.out.println("Environment: Local");
            driver = new RemoteWebDriver(new URL("http://192.168.1.101:32777/wd/hub"), new ChromeOptions());
            System.out.println(params.get("targetEnvironment") + ": " + (System.nanoTime() - startTime) / 1000000);
        }

        eyes.open(driver, "BrowserTester - New", "Bad Resolution - 2", new RectangleSize(1200, 800));

        System.out.println("_______________________________________________________________");

    }

    @Test
    public void BrowserTester() throws InterruptedException {
        for (int i = 0; i < urls.length; i++) {
            driver.get(urls[i][1]);
            eyes.checkWindow(urls[i][0]);


            if(!urls[i][2].equals("")){
                //SeleniumCheckSettings s = Target.window();
                //s.fully();
                //s.strict();
                //s.layout(By.xpath(urls[i][2]));
                //eyes.check(urls[i][0], s);

                //String locators[] = urls[i][2].split(";");

                eyes.check(urls[i][0],
                        Target.window()
                        .fully()
                        .strict()
                        .layout(By.xpath(urls[i][2]))
                        );
            } else {
                eyes.checkWindow(urls[i][0]);
            }





        }
    }

    @AfterMethod(alwaysRun = true)
    void afterMethod(ITestContext context){
        Map<String, String> params = context.getCurrentXmlTest().getAllParameters();
        if(eyes.getIsOpen()){

            eyes.close();

            if(Boolean.parseBoolean(params.get("visualgrid"))) {
                runner.getAllTestResults(true);
            }
        }

        driver.close();
        driver.quit();

    }
}


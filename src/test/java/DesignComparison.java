import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.EyesRunner;
import com.applitools.eyes.TestResultsSummary;
import com.applitools.eyes.exceptions.DiffsFoundException;
import com.applitools.eyes.selenium.ClassicRunner;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.fluent.Target;
import com.applitools.eyes.visualgrid.services.RunnerOptions;
import com.applitools.eyes.visualgrid.services.VisualGridRunner;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class DesignComparison {
    RemoteWebDriver driver;
    Eyes eyes;
    protected static EyesRunner runner;
    protected static BatchInfo batch;
    Map<String, String> params;
    Utility util;

    Mode mode;

    @BeforeClass
    private void beforeClass(ITestContext context) {
        System.out.println("--- Before Class ---");

        context.getCurrentXmlTest().getSuite().setDataProviderThreadCount(1);
        context.getCurrentXmlTest().getSuite().setPreserveOrder(false);
    }

    @BeforeSuite
    private void beforeSuite(ITestContext context) {
        System.out.println("--- Before Suite ---");

        params = context.getCurrentXmlTest().getAllParameters();
        util = new Utility(params);
        System.setProperty("webdriver.chrome.driver", util.getParam("ChromeDriverLocation"));
    
        switch (util.getParam("Mode").toUpperCase()){
            case "SETBASELINES":
                mode = Mode.SET_BASELINES;
                break;
            case "VALIDATEDESIGNS":
                mode = Mode.VALIDATE_DESIGN;
                break;
            case "RUNCHECKS":
                mode = Mode.RUN_CHECKS;
                break;
        }

        batch =  new BatchInfo(util.getParam("BatchName"));

    }

    private void openTest(String testName, RunnerType runType, CheckType checkType) throws IOException {
        EyesConfiguration config = new EyesConfiguration(params, batch);


        switch (mode){
            case SET_BASELINES:
                runner = new ClassicRunner();
                config.buildConfig(testName, runType, true);
                break;

            case VALIDATE_DESIGN:
                if(util.getConfigBool("CrossBrowser")){
                    runner = new VisualGridRunner(new RunnerOptions().testConcurrency(20));
                    config.buildConfig(testName, runType, true);
                } else {
                    runner = new ClassicRunner();
                    config.buildConfig(testName, runType, true);
                }

                break;

            case RUN_CHECKS:
                runner = new VisualGridRunner(new RunnerOptions().testConcurrency(20));
                config.buildConfig(testName, runType, false);
                break;
        }

        runner.setDontCloseBatches(true);
        eyes = new Eyes(runner);

        eyes.setConfiguration(config.getConfiguration());
        eyes.addProperty("Type", checkType.toString());

        switch (util.getParam("LocalBrowserToUse").toUpperCase()){
            case "FIREFOX":
                FirefoxOptions fOptions = new FirefoxOptions();
                driver = new FirefoxDriver(fOptions);
                break;
            case "CHROME": default:
                ChromeOptions cOptions = new ChromeOptions();
                driver = new ChromeDriver(cOptions);
                break;
        }

        eyes.open(driver);
    }

    @Test(dataProvider = "data", threadPoolSize = 1)
    public void ComparisonTester(String testName, String localImageName, String autURL, String executeString) throws InterruptedException, IOException {
        System.out.println("--- Test ---");
        System.out.println("---" + testName + "|" + localImageName + "|" + autURL + "|" + executeString + "|");

        Boolean execute = Boolean.parseBoolean(executeString);

        if(execute){
            switch(mode){
                case SET_BASELINES:
                    String imageLoaderURL = util.getParam("ImageLoaderBaseURL") + "?src=" + localImageName;
                    openTest(testName, RunnerType.CLASSIC, CheckType.BASELINE);
                    driver.get(imageLoaderURL);
                    eyes.check(Target.window().fully(true));
                    closeTest();
                    break;

                case VALIDATE_DESIGN:
                    RunnerType runnerType;
                    if(util.getConfigBool("CrossBrowser")){
                        runnerType = RunnerType.UFG;
                    } else {
                        runnerType = RunnerType.CLASSIC;
                    }

                    openTest(testName, runnerType, CheckType.CHECKPOINT);
                    driver.get(autURL);
                    eyes.check(Target.window().fully(true));
                    closeTest();
                    break;

                case RUN_CHECKS:
                    openTest(testName, RunnerType.UFG, CheckType.CHECKPOINT);
                    driver.get(autURL);
                    eyes.check(Target.window().fully(true));
                    closeTest();
                    break;
            }
        } else {
            System.out.println("Skipping Test: " + testName);
        }
    }

    private void closeTest(){
        if(eyes.getIsOpen()){
            try{
                eyes.closeAsync();
                TestResultsSummary trs = runner.getAllTestResults(false);
                System.out.println(trs.getAllResults()[0].getTestResults().getUrl());
                System.out.println(trs);

            } catch (DiffsFoundException dfe) {
                System.out.println(dfe.getMessage());
            }
        }

        try{driver.quit();}
        catch (NoSuchSessionException ex) {}
        
    }

    @DataProvider(name = "data", parallel = false)
    private Object[][] getData() throws FileNotFoundException {
        Object[][] arrayObject = getExcelData(System.getProperty("user.dir") + "//src//data//DesignComparisonConfig.xls","Data");
        return arrayObject;
    }

    private String[][] getExcelData(String fileName, String sheetName) throws FileNotFoundException {
        String[][] arrayExcelData = null;
        try {
            FileInputStream fs = new FileInputStream(fileName);
            Workbook wb = Workbook.getWorkbook(fs);
            Sheet sh = wb.getSheet(sheetName);

            int totalNoOfCols = sh.getColumns();
            int totalNoOfRows = sh.getRows();

            arrayExcelData = new String[totalNoOfRows-1][totalNoOfCols];

            for (int i= 1 ; i < totalNoOfRows; i++) {

                for (int j=0; j < totalNoOfCols; j++) {
                    arrayExcelData[i-1][j] = sh.getCell(j, i).getContents();
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        return arrayExcelData;
    }
}

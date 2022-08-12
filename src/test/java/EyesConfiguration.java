import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.selenium.Configuration;
import com.applitools.eyes.selenium.StitchMode;
import com.applitools.eyes.visualgrid.model.DesktopBrowserInfo;

import java.util.ArrayList;
import java.util.Map;

public class EyesConfiguration {
    Configuration conf;
    Map<String, String> params;
    BatchInfo batch;


    public EyesConfiguration(){
        conf = new Configuration();
    }

    public EyesConfiguration(Map<String, String> params, BatchInfo batch){
        conf = new Configuration();
        setParams(params);
        setBatch(batch);
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void setBatch(BatchInfo batch) {
        this.batch = batch;
    }

    public void buildConfig(String testName, RunnerType runnerType, Boolean setEnvName){
        Utility util = new Utility(params);

        conf
                .setTestName(testName)
                .setAppName(util.getParam("AppName"))
                .setApiKey(util.getParam("APIKey"))
                .setServerUrl(util.getParam("EyesServerUrl"))
                .setBatch(batch)
                .setHideScrollbars(true)
                .setForceFullPageScreenshot(true)
                .setStitchMode(StitchMode.CSS)
                .setMatchLevel(MatchLevel.STRICT)
                .setWaitBeforeScreenshots(Integer.parseInt(util.getParam("WaitBeforeScreenshots")))
                .setLayoutBreakpoints(true)
                .setViewportSize(util.translateViewport(util.getParam("LocalViewportSize")))
                .setSendDom(true)
                .setIsDisabled(util.getConfigBool("DisableEyes") == null ? false: util.getConfigBool("DisableEyes"));

        if(runnerType.equals(RunnerType.UFG)){
            String[] browsers = util.deserializeParam("Browsers");
            String[] viewports = util.deserializeParam("Viewports");

            ArrayList<BrowserType> browserList = new ArrayList<>();
            for (String browser:browsers) {
                switch (browser.toUpperCase()) {
                    case "CHROME":
                        browserList.add(BrowserType.CHROME);
                        break;
                    case "CHROME-1":
                        browserList.add(BrowserType.CHROME_ONE_VERSION_BACK);
                        break;
                    case "CHROME-2":
                        browserList.add(BrowserType.CHROME_TWO_VERSIONS_BACK);
                        break;
                    case "FIREFOX":
                        browserList.add(BrowserType.FIREFOX);
                        break;
                    case "FIREFOX-1":
                        browserList.add(BrowserType.FIREFOX_ONE_VERSION_BACK);
                        break;
                    case "FIREFOX-2":
                        browserList.add(BrowserType.FIREFOX_TWO_VERSIONS_BACK);
                        break;
                    case "SAFARI":
                        browserList.add(BrowserType.SAFARI);
                        break;
                    case "SAFARI-1":
                        browserList.add(BrowserType.SAFARI_ONE_VERSION_BACK);
                        break;
                    case "SAFARI-2":
                        browserList.add(BrowserType.SAFARI_TWO_VERSIONS_BACK);
                        break;
                    case "EDGE":
                        browserList.add(BrowserType.EDGE_CHROMIUM);
                        break;
                    case "EDGE-1":
                        browserList.add(BrowserType.EDGE_CHROMIUM_ONE_VERSION_BACK);
                        break;
                }
            }

            ArrayList<RectangleSize> viewportList = new ArrayList<>();
            for(String viewport:viewports){
                viewportList.add(util.translateViewport(viewport));
            }

            for (BrowserType b:browserList) {
                for(RectangleSize viewport:viewportList){
                    conf.addBrowser(new DesktopBrowserInfo(viewport, b));
                }
            }
        }

        if(setEnvName){
            conf.setBaselineEnvName(testName);
        }
    }

    public Configuration getConfiguration(){
        return conf;
    }
}

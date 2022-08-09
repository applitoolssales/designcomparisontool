
# Applitools Design Comparison Tool
This project provides the following capabilities:

- Importing images as baselines
- Comparing a delivered product (based on a direct URL) to the established baselines
- Running visual tests across all browsers/viewport sizes at scale

## Basic Flow
This utility is meant to facilitate importing designs as baselines, comparing the designs against the delivered product, then once the delivered product has been deemed correct, running tests at scale within a CI process.

There are 3 main components of this project:

1. The project code
2. Excel document containing the filenames of the designs, and the URLs of the delivered product screens
3. The image loader HTML utility.

Once the project has been configured, start by importing the designs into Eyes using the SetBaselines run mode. This will import the images specified in the spreadsheet into Eyes using the Test Names in the spreadsheet. Next, enter the URLs of the delivered product screens into the spreadsheet and run using the ValidateDesigns run mode. This will compare the designs to the delivered product screens. Note that this can use a cross-browser comparison mode.

Finally, once all issues with the delivered designs have been resolved, you can change to RunChecks mode to first establish unique baselines for all the browser/viewport combinations, then execute these checks within a CI process.

## Project Configuration
All configurations for the project are done within the `testng.xml` file, located at the root directory of the project.

|Parameter|Options|Description|  
|--|--|--|
|ServerURL||URL of your Applitools Eyes server. Make sure to use the API endpoint. <br/><br/>Example: `https://acmeeyesapi.applitools.com`|
|APIKey||Your Applitools Execute API key|  
|Mode|SetBaselines; ValidateDesigns; RunChecks|Sets the mode of operation for the project.<br/><br/> SetBaselines - Saves |  
|BatchName||The name of the batch being created during execution|  
|AppName||The name of the application under test|  
|ImageLoaderBaseURL||URL of the image loader HTML utility. This utility typically needs to be run from a small web server. <br/><br/>This can be IIS on Windows, [jwebserver](https://blogs.oracle.com/javamagazine/post/java-18-simple-web-server) on Java 18+, or [http-server](https://www.npmjs.com/package/http-server) on Node.js|  
|ChromeDriverLocation||Full path to Chromedriver executable|
|GeckoDriverLocation||Full path to Geckodriver executable|
|LocalBrowserToUse|Chrome; Firefox;|The local browser used for execution|
|LocalViewportSize||Viewport size of the local browser in the following format: `width`x`height` <br/><br/>Example: `1200x800`|
|CrossBrowser|true; false|Enables/disables cross-browser testing in the ValidateDesigns mode.|
|Browsers|CHROME; CHROME-1; CHROME-2; FIREFOX; FIREFOX-1; FIREFOX-2; SAFARI, SAFARI-1; SAFARI-2; EDGE, EDGE-1; EDGE-2|A semi-colon delimited list of browsers to render on during the ValidateDesigns (if cross-browser testing is enabled) and RunChecks modes. <br/><br/>Example: `CHROME;CHROME-1;FIREFOX`|
|Viewports||A semi-colon delimited list of viewport sizes for rendering during the ValidateDesigns (if cross-browser testing is enabled) and RunChecks modes.<br/><br/>Example: `1200x600;1080x768`|

## Excel Document
This project contains a single Excel document located at: /src/data/DesignComparisonConfig.xls

This document contains all the images/links to be executed on.
|Column|Description  |
|--|--|
|Test Name|The test name as it will appear in Eyes|
|Image Filename|File name of the image to be used for setting baselines. Keep in mind if this file is in a subdirectory lower than the `loadimage.html` file, you will need to provide the full location here.|
|AUT URL|The application under test URL. This is the delivered product URL for each screen you are comparing|
|Execute|True/False; Flags whether or not to run this line of the spreadsheet.|

## Image Loader Utility
The Image Loader Utility is located at: `/src/imageloader/loadimage.html`

This file needs to be served from a web server. Many options exist for this for example:

- [IIS on Windows](https://www.iis.net/)
- [jwebserver](https://blogs.oracle.com/javamagazine/post/java-18-simple-web-server) on Java 18+
- [http-server](https://www.npmjs.com/package/http-server) on Node.js

`loadimage.html` takes one URL parameter, `src`. `src` is set to the filename of the image to be loaded into the browser.

For example: `loadimage.html?src=myimage.jpg`

Note that the images do not have to reside in the same location as this file, but if they do not, you will need to use relative directory notation in the URL. You will not be able to serve a file that is not accessible through the web server, so keep that in mind.





## Running From Jenkins
All configuration properties detailed above can be set by passing run time parameters in Jenkins. The syntax is to pass `-D<parameter name>` into the Build Goals and Options configuration.  For example, setting the value of the `APIKey` parameter: `-DAPIKey=123456`

Parameter values passed in through Jenkins (or, more accurately, the Java runtime), will supersede the values set via the configuration in the `testng.xml` file.

This can also be combined with Jenkins parameters: `-DAPIKey=$APPLITOOLS_API_KEY` where `APPLITOOLS_API_KEY` is the name of a build parameter. 
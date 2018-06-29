package com.textorio.habrahabr.smartapi.core.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.textorio.habrahabr.smartapi.core.api.pages.LoginPage;
import com.textorio.habrahabr.smartapi.core.api.pages.ProfilePage;
import com.textorio.habrahabr.smartapi.core.lang.Concurrent;
import com.textorio.habrahabr.smartapi.core.lang.SubimageSize;
import com.textorio.habrahabr.smartapi.core.lang.Thing;
import com.textorio.habrahabr.smartapi.core.webdriver.Web;
import com.textorio.habrahabr.smartapi.core.webdriver.WebSettings;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.textorio.habrahabr.smartapi.core.webdriver.Web.CONDITION_REACTION_TIMEOUT_SECONDS;

public class UserResource {
    private static Logger staticLogger = LoggerFactory.getLogger(UserResource.class);
    private SLogger logger;

    public static final boolean DEBUG_SIMULATE_AUTOMATIC_ERROR = false;

    private String email;
    private String password;
    private String username;
    public Web web;

    public String replaceSources(String textFile, HashMap<String, String> data) {
        boolean speakerSyncProtocol = true;

        List<String> lines = getResourceFileAsList(textFile);
        List<String> results = new ArrayList<>();

        boolean rawMode = false;
        Matcher matcher;
        for (String line: lines) {
            if (line.startsWith("//fullscreen") ||
                    line.startsWith("//left") ||
                    line.startsWith("//right") ||
                    StringUtils.isBlank(line)
            ) {
                if (!speakerSyncProtocol) {
                    continue;
                } else if (!StringUtils.isBlank(line)) {
                    results.add(rawMode ? line : renderParagraphHTML(line));
                    continue;
                }
            } else {
                String updatedLine = line;

                if (line.startsWith("//cut")) {
                    updatedLine = "<cut/>";
                } if (line.startsWith("//+raw")) {
                    rawMode = true;
                    continue;
                } else if (line.startsWith("//-raw")) {
                    rawMode = false;
                    continue;
                } else {

                    boolean isImage = false;
                    for (Pattern pattern: timeTagPatterns()) {
                        matcher = pattern.matcher(updatedLine);
                        while (matcher.find()) {
                            String found = matcher.group(0);
                            String foundWithoutTag = found.replaceAll("//", "");
                            String time = stringToTime(foundWithoutTag);
                            if (time != null) {
                                updatedLine = updatedLine.replaceAll(found, renderImageHTML(data.get(time), time));
                                isImage = true;
                            }
                        }
                    }

                    if (!isImage) {
                        updatedLine = rawMode ? updatedLine : renderParagraphHTML(updatedLine);
                    }
                }

                results.add(updatedLine);
            }
        }

        StringBuilder result = new StringBuilder();
        for (String line: results) {
            result.append(line).append("\n\n");
        }

        return result.toString();
    }

    public List<Pair<String, SubimageSize>> extractSources (String textFile) throws UnirestException {
        String slideMode = "fullscreen";
        HashMap<String, SubimageSize> slideModes = new HashMap<>();
        slideModes.put("fullscreen",  new SubimageSize(0,0, 1920, 1080));
        slideModes.put("fullscreen_dirty",  new SubimageSize(18,12, 1405, 784));
        slideModes.put("left",  new SubimageSize(20,152, 1400, 750));
        slideModes.put("right",  new SubimageSize(514,226, 1371, 681));


        List<String> inputs = getResourceFileAsList(textFile);
        List<Pair<String, SubimageSize>> result = new ArrayList<>();

        Matcher matcher;
        for (String input: inputs) {
            if (input.startsWith("//fullscreen")) {
                slideMode = "fullscreen";
                continue;
            } else if (input.startsWith("//left")) {
                slideMode = "left";
                continue;
            } else if (input.startsWith("//right")) {
                slideMode = "right";
                continue;
            }

            for (Pattern pattern: timePatterns()) {
                matcher = pattern.matcher(input);
                while (matcher.find()) {
                    String found = matcher.group(0);
                    String time = stringToTime(found);
                    if (time != null) {
                        result.add(Pair.of(time, slideModes.get(slideMode)));
                    }
                }
            }
        }
        return result;
    }

    public String renderParagraphHTML(String line) {
        if (line == null) {
            return null;
        }

        String template = "<p>%s</p>";
        String rendered = String.format(template, line);
        return rendered;
    }

    public String renderImageHTML(String src, String srcTime) {
        String rendered = "";
        if (srcTime != null) {
            String imageTemplate = "<p>//%s</p>\n<p><img src=\"%s\"></p>";
            rendered = String.format(imageTemplate, timeToString(srcTime), src);
        } else {
            String imageTemplate = "<p><img src=\"%s\"></p>";
            rendered = String.format(imageTemplate, src);
        }
        return rendered;
    }

    public List<Pattern>  timePatterns() {
        Pattern HHMMSS = Pattern.compile("\\d+:\\d+:\\d+");
        Pattern MMSS = Pattern.compile("\\d+:\\d+");
        //Pattern SS = Pattern.compile("\\d+");//Секунды обязательно в квадратных скобках!
        List<Pattern> TIME_PATTERNS = Arrays.asList(HHMMSS,MMSS);
        return TIME_PATTERNS;
    }

    public List<Pattern>  timeTagPatterns() {
        Pattern HHMMSS = Pattern.compile("//\\d+:\\d+:\\d+");
        Pattern MMSS = Pattern.compile("//\\d+:\\d+");
        //Pattern SS = Pattern.compile("//\\d+");//Секунды обязательно в квадратных скобках!
        List<Pattern> TIME_PATTERNS = Arrays.asList(HHMMSS,MMSS);
        return TIME_PATTERNS;
    }

    public static String timeToString(String time)
    {
        int itime = Integer.parseInt(time);

        int hours = itime / 3600;
        int secondsLeft = itime - hours * 3600;
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft - minutes * 60;

        String result = "";

//        if (hours < 10)
//            result += "0";
//        result += hours + ":";

        if (hours > 0) {
            result += hours;
        }

        if (minutes < 10 && hours > 0) {
            result += "0";
        }
        result += minutes + ":";

        if (seconds < 10 && minutes > 0)
            result += "0";
        result += seconds ;

        return result;
    }

    public String stringToTime(String found) {
        String[] components = found.split(":");
        String time = null;
        if (components.length == 3) {
            int hourSecs = 3600 * Integer.parseInt(components[0]);
            int minSecs = 60 * Integer.parseInt(components[1]);
            int secs = Integer.parseInt(components[2]);
            int sumSecs = hourSecs + minSecs + secs;
            time = Integer.toString(sumSecs);
        } else if (components.length == 2) {
            int minSecs = 60 * Integer.parseInt(components[0]);
            int secs = Integer.parseInt(components[1]);
            int sumSecs = minSecs + secs;
            time = Integer.toString(sumSecs);
        } else {
            time = null;
        }
        return time;
    }

    public static int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }

    public String processText(String ytid, String destDir, String textFile, String destTextFile) throws IOException, InterruptedException, UnirestException {
        String updatedText = processText(ytid, destDir, textFile);

        File destText = new File(destTextFile);
        if (destText.exists()) {
            destText.delete();
        }

        Files.write(Paths.get(destTextFile), updatedText.getBytes());
        return updatedText;
    }

    public String processText(String ytid, String destDir, String textFile) throws IOException, InterruptedException, UnirestException {
        HashMap<String, String> sources = downloadScreenshots(ytid, destDir, extractSources(textFile));
        String result = replaceSources(textFile, sources);
        return result;
    }

    public HashMap<String, String> downloadScreenshots(String ytid, String destDir, String textFile) throws IOException, InterruptedException, UnirestException {
        return downloadScreenshots(ytid, destDir, extractSources(textFile));
    }

    public HashMap<String, String> downloadScreenshots(String ytid, String destDir, List<Pair<String, SubimageSize>> sources) throws IOException, InterruptedException, UnirestException {
        HashMap<String, String> result = new HashMap<>();
        if (cacheExists()) {
            result = loadCache();
        }
        boolean firstTime = true;
        for (int i=0; i< sources.size(); i++) {
            Pair<String, SubimageSize> source = sources.get(i);
            String time = source.getLeft();
            String key = time;

            if (!result.containsKey(key)) {
                String destFile = destDir + File.separator + key + ".jpg";
                String backupDestFile = destDir + File.separator + "backups" + File.separator + key + ".jpg";
                downloadScreenshot(ytid, time, source.getRight(), destFile, backupDestFile, !firstTime);
                String imgurFile = uploadFile(destFile);
                result.put(key, imgurFile);
                saveCache(result);
                if (firstTime) {
                    firstTime = false;
                }
            }
        }
        saveCache(result);
        return result;
    }

    public static final String cacheFileName = "/Users/olegchir/tmp/img/imgcache.json";

    public boolean cacheExists() {
        return Files.exists(Paths.get(cacheFileName));
    }

    public HashMap<String, String> loadCache() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> items = mapper.readValue(new File(cacheFileName), new TypeReference<HashMap<String, String>>(){});
        return items;
    }

    public void saveCache(HashMap<String, String> items) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        File cacheFile = new File(cacheFileName);
        if (cacheExists()) {
            cacheFile.delete();
        }
        writer.writeValue(cacheFile, items);
    }

    public String uploadFile(String filename) throws UnirestException {
        String result = "";

        String endpoint = "https://api.imgur.com/3/image";
        String accessToken = "Bearer "+System.getProperty("IMGUR_ACCESS_TOKEN");

        HttpResponse<JsonNode> uploadResponse = Unirest.post(endpoint)
                .header("Authorization", accessToken)
                .field("image", new File(filename))
                .asJson();
        System.out.println(uploadResponse);

        if (uploadResponse.getStatus() / 100 != 2) {
            // redirects, server errors, lions and tigers and bears! Oh my!
            System.out.println("Server said: " + uploadResponse.getStatusText());
            try {
                String message = uploadResponse.getBody().getObject()
                        .getJSONObject("data")
                        .getJSONObject("error")
                        .getString("message");
                System.out.println("Server explained: "+ message);
                if (message.contains("You are uploading too fast. Please wait")) {
                    System.out.println("ОПЯТЬ ЭТОТ МЕРЗКИЙ ТАЙМАУТ! НАДО УЖЕ СЛЕЗАТЬ С ИМГУРА!");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            result = uploadResponse.getBody().getObject().getJSONObject("data").getString("link");
        }

        return result;
    }

    public String downloadScreenshot(String ytid, String time, SubimageSize size, String destFile, String backupDestFile, boolean followUp) throws InterruptedException, IOException {
        String ytUrl = String.format("https://www.youtube.com/watch?v=%s", ytid);
        if (!followUp) {
            web.debugShowBrowser(ytUrl);
            Thread.sleep(10000);
        }

        String screenshotMaker = getResourceFileAsString("screenshot-maker.js");
        web.driver().executeScript(screenshotMaker,Integer.parseInt(time));
        WebDriverWait wait3 = new WebDriverWait(web.driver(), 50);

        Thread.sleep(5000);

        wait3.until(driver -> ((JavascriptExecutor)driver).executeScript("return window.screenshotFired != undefined;").equals(true));

        Thread.sleep(3000);

        String summoner = getResourceFileAsString("lastresult-summoner.js");
        web.driver().executeScript(summoner);
        WebDriverWait wait = new WebDriverWait(web.driver(), 20);
        wait.until(driver -> ((JavascriptExecutor)driver).executeScript("return window.lastResult != undefined;").equals(true));

        Object result = driver().executeScript("return window.lastResult;");
        saveImageFromBase64String((String)result, size, destFile, backupDestFile);

        System.out.println(result);
        return "";
    }

    public void saveImageFromBase64String(String image, SubimageSize size, String destination, String backupDestination) throws IOException {
        byte[] data = Base64.decodeBase64(image);
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        BufferedImage bImage2 = ImageIO.read(bis);

        ImageIO.write(bImage2, "jpg", new File(backupDestination) );

        if (size.x + size.width > bImage2.getWidth()) {
            size.width = bImage2.getWidth() - size.x -2;
        }

        if (size.y + size.height > bImage2.getHeight()) {
            size.height = bImage2.getHeight() - size.y -2;
        }


        BufferedImage bImage3 = bImage2.getSubimage(size.x,size.y, size.width, size.height);
        ImageIO.write(bImage3, "jpg", new File(destination) );
    }

    public String getResourceFileAsString(String fileName) {
        try(InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getResourceFileAsList(String fileName) {
        try(InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                return reader.lines().collect(Collectors.toList());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void analyzeLog() {
        LogEntries logEntries = web.driver().manage().logs().get(LogType.BROWSER);
        for (LogEntry entry : logEntries) {
            System.out.println(new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage());
            //do something useful with the data
        }
    }

    public UserResource(String username, String email, String password, Web web) {
        this.logger = new SLogger(username, UserResource.class);

        this.username = username;
        this.email = email;
        this.password = password;
        this.web = web;
    }

    public static Thing<UserResource, ?> create(String username, String email, String password) {
        return create(Optional.empty(), username, email, password);
    }

    public static Thing<UserResource, ?> create(Optional<WebSettings> webSettings, String username, String email, String password) {
        WebSettings ws = webSettings.orElse(new WebSettings());
        ws.setId(username);
        ws.setProfileDirName(Optional.of(String.format("%s-%s", Web.CHROME_PROFILE_DIR_NAME, username)));

        Web web;
        try {
            web = new Web().create(ws);
        } catch (Exception e) {
            return Thing.ofError("Can't create Web Token for the user");
        }

        return Thing.of(new UserResource(username, email, password, web));
    }

    public ChromeDriver driver() {
        return web.driver();
    }

    public boolean requireLogin(boolean firstRun) {
        boolean result = false;
        ProfilePage.activateFast(web);
        if (!ProfilePage.activated(web) && LoginPage.activated(web)) {
            logger.info("We're logged out, trying to log in using automation");
            if (firstRun || !login(true)) {
                logger.info("Automatic logging failed, using manual mode");
                web.restartVisible();
                if (!login(false)) {
                    logger.error("Manual logging failed. No further attempts to call API result in valid data.");
                } else {
                    result = true;
                    logger.info("Manual logging in OK");
                    web.restartInvisible();
                }
            } else {
                result = true;
                logger.info("Logged in successfully. Let's celebrate, bitches!");
            }
        } else if (!ProfilePage.activated(web) && !LoginPage.activated(web)) {
            logger.error("Some strange unknown page encountered while processing a new request.");
        } else if (ProfilePage.activated(web)){
            result = true;
            logger.info("You don't need to log in. Lucky bitch!");
        }
        return result;
    }

    public boolean login(boolean automatic) {
        //This is not duplication of what's written in requireLogin!
        //This happens when automated log-in procedure was too fast to detect success
        LoginPage.activateSlow(web); //may cause redirect to settings page
        if (ProfilePage.activated(web)) {
            logger.info("Previous attempts to log in succeeded, no further actions required.");
            return true;
        }

        LoginPage loginPage = LoginPage.goSlow(web).raiseIfInvalid("We should be on the login page now").get();
        //web.debugShowBrowser(ProfilePage.HABR_SETTINGS_PAGE);
        loginPage.fill(email, password);

        if (automatic) {
            if (DEBUG_SIMULATE_AUTOMATIC_ERROR) {
                logger.info("Simulation of automatic logging error");
                return false;
            }

            logger.info("Trying to log in");
            loginPage.submit();
            //now we should be at this page: https://id.tmtm.ru/settings/?consumer=default

//            TODO: very ugly! I'll try to change it to a result detection later
            Concurrent.sleep(web.getSettings().getPageTimeout());

            if (ProfilePage.activated(web)) {
                logger.info("Logged in");
                return true;
            } else {
                logger.info("Logging in failed");
                return false;
            }
        } else {
            final AtomicBoolean success = new AtomicBoolean(true);
            web.closeWindowAndContinueWhen("Browser should switch to a settings page",
                    condition -> {

                        // Window should not be closed until user get to the settings page
                        try {
                            driver().getTitle();
                        } catch (Exception ex) {
                            logger.info("Browser closed, and that's very bad.");
                            success.set(false);
                            return true;
                        }

                        return ProfilePage.activated(web);
                    });
            return success.get();
        }

    }

    public Web getWeb() {
        return web;
    }

    public void setWeb(Web web) {
        this.web = web;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

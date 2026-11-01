package practical13;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FileUploadDownload {
    public static void main(String[] args) throws Exception {
        boolean robotMode = Boolean.getBoolean("robot");
        if (robotMode && Boolean.getBoolean("headless")) throw new IllegalArgumentException("Robot requires a visible desktop");
        Path source = Path.of("data", "testfile.txt").toAbsolutePath();
        if (!Files.isRegularFile(source)) throw new IllegalStateException("Missing data/testfile.txt");
        Path downloads = Path.of("downloads").toAbsolutePath();
        Files.createDirectories(downloads);
        Path runFolder = Files.createTempDirectory(downloads, "run-");
        ChromeOptions options = new ChromeOptions();
        if (Boolean.getBoolean("headless")) options.addArguments("--headless=new");
        options.setExperimentalOption("prefs", Map.of("download.default_directory", runFolder.toString(),
                "download.prompt_for_download", false));
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get(Path.of("web", "upload_download.html").toUri().toString());
            if (robotMode) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(source.toString()), null);
                driver.findElement(By.id("choose")).click();
                Robot robot = new Robot();
                robot.delay(1200);
                robot.keyPress(KeyEvent.VK_CONTROL); robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_V); robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_ENTER); robot.keyRelease(KeyEvent.VK_ENTER);
            } else {
                driver.findElement(By.id("uploadFile")).sendKeys(source.toString());
            }
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.textToBe(By.id("uploadStatus"), "File uploaded successfully: testfile.txt"));
            driver.findElement(By.id("downloadButton")).click();
            Path downloaded = runFolder.resolve("testfile.txt");
            wait.until(d -> Files.isRegularFile(downloaded));
            if (Files.mismatch(source, downloaded) != -1) throw new IllegalStateException("Downloaded content differs");
            System.out.println("PASS - upload confirmed by the demo page");
            System.out.println("PASS - downloaded file exists and matches original bytes");
            System.out.println("Downloaded file: " + downloaded);
        } finally {
            driver.quit();
        }
    }
}

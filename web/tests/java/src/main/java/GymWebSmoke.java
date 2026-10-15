import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;

/** Read-only Java Selenium companion to the full Python regression and IDE suite. */
public class GymWebSmoke {
    static WebDriver browser;
    static WebDriverWait wait;
    static String base;
    static final List<String> results = new ArrayList<>();
    static int failed;
    interface Check { String run() throws Exception; }
    static WebElement element(String css) { return browser.findElement(By.cssSelector(css)); }
    static void click(String css) { wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(css))).click(); }
    static void require(boolean valid, String message) { if (!valid) throw new AssertionError(message); }
    static String json(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t") + "\"";
    }
    static void test(String id, String name, Check check) {
        long started = System.nanoTime(); String status = "PASS", actual;
        try { actual = check.run(); }
        catch (Throwable e) { status = "FAIL"; failed++; actual = e.toString(); }
        results.add("{\"id\":"+json(id)+",\"name\":"+json(name)+",\"status\":"+json(status)+",\"actual\":"+json(actual)+",\"seconds\":"+((System.nanoTime()-started)/1e9)+"}");
        System.out.println(id+" "+status+" "+name);
    }
    static void login(String username, boolean valid) {
        browser.manage().deleteAllCookies(); browser.get(base+"/");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login")));
        element("#login-username").sendKeys(username); element("#login-password").sendKeys("123"); click("#sign-in");
        if (valid) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("app")));
            wait.until(d -> !d.findElements(By.cssSelector(".stat-card")).isEmpty());
        } else { wait.until(d -> !element("#login-error").getText().isBlank()); }
    }
    static void page(String name) {
        click("[data-page=\""+name+"\"]");
        wait.until(d -> d.getCurrentUrl().endsWith("#"+name));
        wait.until(d -> d.findElements(By.cssSelector(".loading")).isEmpty());
        require(!element("#page-content").getText().contains("could not be loaded"), "Page failed to load");
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("Use base URL, checkout path, ChromeDriver path");
        base=args[0]; Path root=Path.of(args[1]);
        ChromeOptions options=new ChromeOptions(); options.addArguments("--headless=new", "--window-size=1440,1050");
        ChromeDriverService service=new ChromeDriverService.Builder().usingDriverExecutable(Path.of(args[2]).toFile()).build();
        browser=new ChromeDriver(service,options); wait=new WebDriverWait(browser,Duration.ofSeconds(18));
        String version=((HasCapabilities)browser).getCapabilities().getBrowserVersion();
        try {
            test("JAVA01","Unknown account rejected",()->{login("stqa_unknown_java",false); require(element("#login").isDisplayed(),"Login not visible"); return element("#login-error").getText();});
            test("JAVA02","Admin username sign-in",()->{login("admin",true); require(element("#nav-role").getText().equalsIgnoreCase("admin"),"Wrong role"); require(browser.findElements(By.cssSelector(".stat-card")).size()==5,"Expected five metrics"); return "Admin dashboard has five saved-data metrics.";});
            test("JAVA03","Admin mobile sign-in",()->{login("0770000001",true); require(element("#nav-role").getText().equalsIgnoreCase("admin"),"Wrong role"); return "Registered mobile authenticates Admin.";});
            test("JAVA04","Member table search",()->{login("admin",true);page("members");wait.until(d->!d.findElements(By.cssSelector("#table-body tr[data-record-id]")).isEmpty()); String name=element("#table-body tr[data-record-id] td:nth-child(2)").getText();element("#table-search").sendKeys(name);require(element("#table-body").getText().contains(name),"Search omitted member");return "Saved member found: "+name;});
            test("JAVA05","Six report choices",()->{login("admin",true);page("reports");require(browser.findElements(By.cssSelector("[data-report]")).size()==6,"Expected six report types");return "All six PDF report actions displayed.";});
            test("JAVA06","Trainer profile and scoped navigation",()->{login("trainer",true);require(element("#nav-role").getText().equalsIgnoreCase("trainer"),"Wrong role");require(browser.findElements(By.cssSelector("[data-page=users],[data-page=reports]")).isEmpty(),"Admin links exposed");page("profile");require(!element("#page-content").getText().isBlank(),"Empty profile");return "Trainer profile loads; Admin navigation absent.";});
            test("JAVA07","Member profile and scoped navigation",()->{login("user1",true);require(element("#nav-role").getText().equalsIgnoreCase("member"),"Wrong role");require(browser.findElements(By.cssSelector("[data-page=users],[data-page=reports]")).isEmpty(),"Admin links exposed");page("profile");require(!element("#page-content").getText().isBlank(),"Empty profile");return "Member profile loads; Admin navigation absent.";});
            test("JAVA08","Confirmed logout",()->{login("admin",true);click("#logout");click("#confirm-action");wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login")));require(!element("#app").isDisplayed(),"Workspace still visible");return "Sign-out confirmation returns to login.";});
        } finally { browser.quit(); }
        Path out=root.resolve("output/stqa");Files.createDirectories(out);
        String metadata="{\"timestamp\":"+json(OffsetDateTime.now().toString())+",\"base_url\":"+json(base)+",\"browser\":\"chrome\",\"browser_version\":"+json(version)+",\"selenium_version\":"+json(new BuildInfo().getReleaseLabel())+",\"total\":"+results.size()+",\"passed\":"+(results.size()-failed)+",\"failed\":"+failed+"}";
        Files.writeString(out.resolve("selenium_java_results.json"),"{\"metadata\":"+metadata+",\"cases\":["+String.join(",",results)+"]}",StandardCharsets.UTF_8);
        System.out.println("Java Selenium: "+(results.size()-failed)+"/"+results.size()+" passed; evidence saved.");
        if(failed>0)System.exit(1);
    }
}

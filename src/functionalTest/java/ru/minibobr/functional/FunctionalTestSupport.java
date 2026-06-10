package ru.minibobr.functional;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;

import java.net.HttpURLConnection;
import java.net.URI;

public abstract class FunctionalTestSupport {

    protected static Playwright playwright;
    protected static Browser browser;
    protected static String baseUrl;

    @BeforeAll
    static void setUp() {
        baseUrl = System.getProperty("app.base.url", "http://localhost:8080");
        Assumptions.assumeTrue(isAppReachable(baseUrl), "Application is not reachable at " + baseUrl);
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void tearDown() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    protected Page newPage() {
        return browser.newPage();
    }

    protected String uniqueUser() {
        return "user_" + System.currentTimeMillis();
    }

    protected void registerAndLogin(Page page, String username, String password) {
        page.navigate(baseUrl + "/");
        page.locator("#username").fill(username);
        page.locator("#password").fill(password);
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Зарегистрироваться")).click();
        page.locator("#username").fill(username);
        page.locator("#password").fill(password);
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Войти")).click();
        page.waitForURL("**/main");
    }

    private static boolean isAppReachable(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
            return code >= 200 && code < 500;
        } catch (Exception e) {
            return false;
        }
    }
}

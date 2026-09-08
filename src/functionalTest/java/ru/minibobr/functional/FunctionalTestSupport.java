package ru.minibobr.functional;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

abstract class FunctionalTestSupport {
    protected static Browser browser;
    protected static String baseUrl;
    private static Playwright playwright;
    private static Process application;
    private BrowserContext context;
    protected Page page;
    protected static final String PASSWORD = "password123";
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("messages");

    protected static String message(String key) {
        return MESSAGES.getString(key);
    }

    @BeforeAll
    static void start() throws Exception {
        baseUrl = System.getProperty("app.base.url");
        try {
            if (baseUrl == null || baseUrl.isBlank()) {
                String port = System.getProperty("app.port");
                baseUrl = "http://localhost:" + port;
                File log = new File(System.getProperty("app.log"));
                log.getParentFile().mkdirs();
                String java = new File(System.getProperty("java.home"), "bin/java").getPath();
                application = new ProcessBuilder(java, "-jar", System.getProperty("app.jar"),
                        "--server.port=" + port)
                        .redirectErrorStream(true).redirectOutput(log).start();
            }
            awaitApplication();
            playwright = Playwright.create();
            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(true);
            String channel = System.getProperty("playwright.channel");
            if ((channel == null || channel.isBlank())
                    && System.getProperty("os.name", "").toLowerCase().contains("mac")) {
                channel = "chrome";
            }
            if (channel != null && !channel.isBlank()) options.setChannel(channel);
            browser = playwright.chromium().launch(options);
        } catch (Exception | AssertionError error) {
            stop();
            throw error;
        }
    }

    private static void awaitApplication() throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(
                Long.parseLong(System.getProperty("app.timeout.seconds"))).toNanos();
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/messages"))
                .timeout(Duration.ofSeconds(2)).GET().build();
        while (System.nanoTime() < deadline) {
            if (application != null && !application.isAlive()) {
                throw new AssertionError("Application exited. See " + System.getProperty("app.log"));
            }
            try {
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 && response.body().contains("app.title")) return;
            } catch (java.io.IOException ignored) {
                // The server may still be starting; retry until the deadline.
            }
            Thread.sleep(250);
        }
        throw new AssertionError("Application did not become ready at " + baseUrl);
    }

    @BeforeEach
    void openPage() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closePage() {
        if (context != null) context.close();
    }

    @AfterAll
    static void stop() throws InterruptedException {
        try {
            if (browser != null) browser.close();
        } finally {
            try {
                if (playwright != null) playwright.close();
            } finally {
                if (application != null) {
                    application.destroy();
                    if (!application.waitFor(10, TimeUnit.SECONDS)) {
                        application.destroyForcibly().waitFor();
                    }
                }
            }
        }
    }

    protected String uniqueUser() {
        return "test_" + UUID.randomUUID();
    }

    protected Locator button(String key) {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(message(key)).setExact(true));
    }

    protected void credentials(String user, String password) {
        page.locator("#username").fill(user);
        page.locator("#password").fill(password);
    }

    protected void register(String user) {
        page.navigate(baseUrl + "/");
        credentials(user, PASSWORD);
        button("app.register.button").click();
        assertThat(page.locator(".error-message")).hasText(message("app.register.success"));
    }

    protected void login(String user) {
        credentials(user, PASSWORD);
        button("app.login.button").click();
        page.waitForURL(baseUrl + "/main");
        assertThat(page.locator("#svg")).isVisible();
    }

    protected String registerAndLogin() {
        String user = uniqueUser();
        register(user);
        login(user);
        return user;
    }

    protected void point(String x, String y, String r) {
        page.locator("select").nth(0).selectOption(x);
        page.locator("#y-input").fill(y);
        page.locator("select").nth(1).selectOption(r);
        var response = page.waitForResponse(rsp -> rsp.url().endsWith("/api/points")
                && rsp.request().method().equals("POST"), () -> button("app.check.button").click());
        Assertions.assertEquals(200, response.status());
        assertThat(button("app.check.button")).isEnabled();
    }

    protected Locator rows() {
        return page.locator(".result-table tbody tr");
    }
}

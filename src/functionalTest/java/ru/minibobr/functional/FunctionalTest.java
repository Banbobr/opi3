package ru.minibobr.functional;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FunctionalTest extends FunctionalTestSupport {
    @Test
    void case01_registration() {
        register(uniqueUser());
        assertThat(page.locator("#username")).isVisible();
    }

    @Test
    void case02_duplicateRegistration() {
        String user = uniqueUser();
        register(user);
        button("app.register.button").click();
        assertThat(page.locator(".error-message")).hasText(message("auth.register.duplicate"));
    }

    @Test
    void case03_invalidPassword() {
        String user = uniqueUser();
        register(user);
        credentials(user, "wrong-password");
        button("app.login.button").click();
        assertThat(page.locator(".error-message")).hasText(message("auth.login.invalid"));
        assertThat(page).hasURL(baseUrl + "/");
    }

    @Test
    void case04_login() {
        registerAndLogin();
        assertThat(page).hasURL(baseUrl + "/main");
    }

    @Test
    void case05_guestCannotOpenMain() {
        page.navigate(baseUrl + "/main");
        assertThat(page.locator("#username")).isVisible();
        assertThat(page).hasURL(baseUrl + "/");
        assertEquals(401, page.request().get(baseUrl + "/api/points/all").status());
    }

    @Test
    void case06_rectangleHitAndStatistics() {
        registerAndLogin();
        point("1", "1", "2");
        assertThat(rows()).hasCount(1);
        assertThat(rows().first().locator("td").nth(3)).hasText(message("app.hit"));
        assertThat(page.locator(".stats-container")).containsText(message("app.stats.hits") + " 1");
    }

    @Test
    void case07_miss() {
        registerAndLogin();
        point("1", "-1", "2");
        assertThat(rows().first().locator("td").nth(3)).hasText(message("app.miss"));
        assertThat(page.locator(".stats-container")).containsText(message("app.stats.misses") + " 1");
    }

    @Test
    void case08_triangleBoundary() {
        registerAndLogin();
        point("-1", "1", "4");
        assertThat(rows().first().locator("td").nth(3)).hasText(message("app.hit"));
    }

    @Test
    void case09_invalidY() {
        registerAndLogin();
        page.locator("#y-input").fill("6");
        assertThat(page.locator(".error-message")).hasText(message("app.y.error.range"));
        page.locator("#y-input").fill("1abc");
        assertThat(page.locator(".error-message")).hasText(message("app.y.error.number"));
        assertThat(rows()).hasCount(0);
    }

    @Test
    void case10_historySurvivesReload() {
        registerAndLogin();
        point("1", "1", "2");
        page.reload();
        assertThat(rows()).hasCount(1);
        assertThat(rows().first().locator("td").nth(0)).hasText("1");
        assertThat(rows().first().locator("td").nth(3)).hasText(message("app.hit"));
    }

    @Test
    void case11_clearHistory() {
        registerAndLogin();
        point("1", "1", "2");
        page.onceDialog(dialog -> dialog.accept());
        button("app.page.clear").click();
        assertThat(rows()).hasCount(0);
        page.reload();
        assertThat(page.locator("#svg")).isVisible();
        assertThat(rows()).hasCount(0);
        assertThat(page.locator(".stats-container")).containsText(message("app.stats.hits") + " 0");
    }

    @Test
    void case12_userIsolation() {
        String firstUser = registerAndLogin();
        point("1", "1", "2");
        button("app.logout").click();
        registerAndLogin();
        assertThat(rows()).hasCount(0);
        point("1", "-1", "2");
        button("app.logout").click();
        assertThat(page.locator("#username")).isVisible();
        login(firstUser);
        assertThat(rows()).hasCount(1);
        assertThat(rows().first().locator("td").nth(3)).hasText(message("app.hit"));
    }

    @Test
    void case13_logoutInvalidatesSession() {
        registerAndLogin();
        button("app.logout").click();
        assertThat(page.locator("#username")).isVisible();
        assertEquals(401, page.request().get(baseUrl + "/api/points/all").status());
        page.navigate(baseUrl + "/main");
        assertThat(page).hasURL(baseUrl + "/");
    }

    @Test
    void case14_graphClick() {
        registerAndLogin();
        page.locator("select").nth(1).selectOption("2");
        var location = (Map<?, ?>) page.locator("#svg").evaluate("svg => { "
                + "const p = new DOMPoint(300, 200).matrixTransform(svg.getScreenCTM()); "
                + "return {x: p.x, y: p.y}; }");
        page.mouse().click(((Number) location.get("x")).doubleValue(),
                ((Number) location.get("y")).doubleValue());
        assertThat(rows()).hasCount(1);
        assertThat(rows().first().locator("td").nth(0)).hasText("1");
        assertEquals(1.0, Double.parseDouble(rows().first().locator("td").nth(1).textContent()), 0.03);
        assertThat(rows().first().locator("td").nth(3)).hasText(message("app.hit"));
    }

    @Test
    void case15_pagination() {
        registerAndLogin();
        for (int i = 0; i < 11; i++) point("1", "1", "2");
        assertThat(rows()).hasCount(10);
        assertThat(button("app.page.next")).isEnabled();
        button("app.page.next").click();
        assertThat(rows()).hasCount(1);
        assertThat(button("app.page.next")).isDisabled();
        button("app.page.prev").click();
        assertThat(rows()).hasCount(10);
    }
}

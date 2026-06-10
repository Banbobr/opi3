package ru.minibobr.functional;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Web4FunctionalTest extends FunctionalTestSupport {

    @Test
    void case01_loginPageTitleVisible() {
        Page page = newPage();
        page.navigate(baseUrl + "/");
        assertThat(page.locator("h1")).containsText("Лабораторная работа");
    }

    @Test
    void case02_clockElementExists() {
        Page page = newPage();
        page.navigate(baseUrl + "/");
        assertThat(page.locator("#clock")).isVisible();
    }

    @Test
    void case03_usernameFieldVisible() {
        Page page = newPage();
        page.navigate(baseUrl + "/");
        assertThat(page.locator("#username")).isVisible();
    }

    @Test
    void case04_passwordFieldVisible() {
        Page page = newPage();
        page.navigate(baseUrl + "/");
        assertThat(page.locator("#password")).isVisible();
    }

    @Test
    void case05_loginButtonVisible() {
        Page page = newPage();
        page.navigate(baseUrl + "/");
        assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Войти"))).isVisible();
    }

    @Test
    void case06_registerButtonVisible() {
        Page page = newPage();
        page.navigate(baseUrl + "/");
        assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Зарегистрироваться"))).isVisible();
    }

    @Test
    void case07_registerShowsSuccessMessage() {
        Page page = newPage();
        String user = uniqueUser();
        page.navigate(baseUrl + "/");
        page.locator("#username").fill(user);
        page.locator("#password").fill("password123");
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Зарегистрироваться")).click();
        assertThat(page.locator(".error-message")).containsText("Регистрация успешна");
    }

    @Test
    void case08_loginRedirectsToMain() {
        Page page = newPage();
        String user = uniqueUser();
        registerAndLogin(page, user, "password123");
        assertTrue(page.url().contains("/main"));
    }

    @Test
    void case09_mainPageGraphVisible() {
        Page page = newPage();
        registerAndLogin(page, uniqueUser(), "password123");
        assertThat(page.locator("#svg")).isVisible();
    }

    @Test
    void case10_mainPageXSelectVisible() {
        Page page = newPage();
        registerAndLogin(page, uniqueUser(), "password123");
        assertThat(page.locator(".custom-select").first()).isVisible();
    }

    @Test
    void case11_mainPageYInputVisible() {
        Page page = newPage();
        registerAndLogin(page, uniqueUser(), "password123");
        assertThat(page.locator("#y-input")).isVisible();
    }

    @Test
    void case12_mainPageRSelectVisible() {
        Page page = newPage();
        registerAndLogin(page, uniqueUser(), "password123");
        Locator selects = page.locator(".custom-select");
        assertThat(selects.nth(1)).isVisible();
    }

    @Test
    void case13_checkButtonVisible() {
        Page page = newPage();
        registerAndLogin(page, uniqueUser(), "password123");
        assertThat(page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Проверить"))).isVisible();
    }

    @Test
    void case14_logoutReturnsToLogin() {
        Page page = newPage();
        registerAndLogin(page, uniqueUser(), "password123");
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Выйти")).click();
        page.waitForURL("**/");
        assertThat(page.locator("#username")).isVisible();
    }

    @Test
    void case15_unauthenticatedMainRedirectsToLogin() {
        Page page = newPage();
        page.navigate(baseUrl + "/main");
        page.waitForURL("**/");
        assertThat(page.locator("#username")).isVisible();
    }
}

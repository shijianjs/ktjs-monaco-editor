package demo.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.junit.UsePlaywright
import com.microsoft.playwright.options.WaitUntilState
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junitpioneer.jupiter.RetryingTest

/**
 * Smoke checks for the demo page: it must load the bundle, apply its stylesheet, render the live
 * monaco editor and answer every wrapper call the page offers — without console errors.
 */
@UsePlaywright(DemoOptions::class)
class DemoPageTest {
    @RetryingTest(maxAttempts = 2)
    fun `page renders the project heading and styles`(page: Page) {
        openPage(page)

        assertTrue(page.title().contains("ktjs-monaco-editor"), "title: ${page.title()}")
        assertEquals("ktjs-monaco-editor", page.locator("h1").textContent())
        assertTrue(page.locator("img.logo").isVisible(), "the demo serves the project logo")

        val display = page.locator(".page").evaluate("(node) => getComputedStyle(node).display") as String
        assertTrue(display != "contents", "the stylesheet applies to .page (display = $display)")
    }

    @RetryingTest(maxAttempts = 2)
    fun `editor renders and reports its text`(page: Page) {
        openPage(page)

        // The wrapper call `Monaco.editor.create` renders monaco's own DOM inside the host element.
        assertTrue(page.locator(".editor .monaco-editor").first().isVisible(), "monaco rendered an editor")

        page.getByText("Read the text").click()

        val output = page.locator("pre.output").textContent() ?: ""
        assertTrue(output.contains("fun main()"), "output: $output")
        assertTrue(output.contains("println(greeting)"), "output: $output")
    }

    @RetryingTest(maxAttempts = 2)
    fun `every wrapper call answers`(page: Page) {
        openPage(page)

        for (label in listOf("Show the cursor", "Show the selection", "Next theme", "Next language")) {
            page.getByText(label).click()
            val output = page.locator("pre.output").textContent() ?: ""
            assertTrue(output.isNotBlank(), "$label produced output: $output")
            assertTrue(output != "Run one of the calls to see its result here.", "$label updated the panel")
        }
    }

    @RetryingTest(maxAttempts = 2)
    fun `the language switch relabels the page without losing the editor`(page: Page) {
        openPage(page)

        page.locator(".switcher button", Page.LocatorOptions().setHasText("简体中文")).click()

        assertEquals("ktjs-monaco-editor 演示", page.title())
        assertTrue(page.getByText("读取文本").isVisible(), "the buttons are relabelled in Chinese")
        assertTrue(
            page.locator(".editor .monaco-editor").first().isVisible(),
            "switching the language keeps the same editor instance",
        )
    }

    @RetryingTest(maxAttempts = 2)
    fun `page logs no console errors or failed requests`(page: Page) {
        val consoleErrors = mutableListOf<String>()
        val failedRequests = mutableListOf<String>()
        page.onConsoleMessage { message ->
            if (message.type() == "error") consoleErrors += message.text()
        }
        page.onRequestFailed { request ->
            failedRequests += "${request.url()} ${request.failure()}"
        }

        openPage(page)
        page.getByText("Add a warning marker").click()

        assertTrue(consoleErrors.isEmpty(), "console errors: $consoleErrors")
        assertTrue(failedRequests.isEmpty(), "failed requests: $failedRequests")
    }

    private fun openPage(page: Page) {
        page.navigate("/", Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE))
    }
}

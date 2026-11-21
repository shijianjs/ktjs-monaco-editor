package demo.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.junit.UsePlaywright
import com.microsoft.playwright.options.WaitUntilState
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junitpioneer.jupiter.RetryingTest

/**
 * Smoke checks for the demo page: the shell comes from the kit, the live editor renders, every
 * wrapper call the playground offers answers, and the language switch relabels without losing the
 * editor.
 *
 * The shell lives in a shadow root; Playwright's CSS pierces shadow boundaries, so these selectors
 * are the ones a browser-side probe would use too.
 */
@UsePlaywright(DemoOptions::class)
class DemoPageTest {
    @RetryingTest(maxAttempts = 2)
    fun `page renders the shell, the logo and an overview`(page: Page) {
        openPage(page)

        assertTrue(page.title().contains("ktjs-monaco-editor"), "title: ${page.title()}")
        assertTrue(page.locator("img.layout__logo").isVisible(), "the shell shows the project logo")
        assertTrue(
            page.locator("vaadin-side-nav-item[data-route='overview']").isVisible(),
            "the sidebar lists the overview route",
        )
    }

    @RetryingTest(maxAttempts = 2)
    fun `editor renders on the playground and reports its text`(page: Page) {
        openPage(page)
        goTo(page, "playground")

        // `Monaco.editor.create` renders monaco's own DOM inside the host element.
        page.waitForSelector("[data-editor] .monaco-editor", Page.WaitForSelectorOptions().setTimeout(20_000.0))
        assertTrue(page.locator("[data-editor] .monaco-editor").first().isVisible(), "monaco rendered an editor")

        page.getByText("Read the text").click()

        val output = page.locator("[data-output]").textContent() ?: ""
        assertTrue(output.contains("fun main()"), "output: $output")
        assertTrue(output.contains("println(greeting)"), "output: $output")
    }

    @RetryingTest(maxAttempts = 2)
    fun `every wrapper call answers`(page: Page) {
        openPage(page)
        goTo(page, "playground")

        for (label in listOf("Show the cursor", "Show the selection", "Next theme", "Next language")) {
            page.getByText(label).click()
            val output = page.locator("[data-output]").textContent() ?: ""
            assertTrue(output.isNotBlank(), "$label produced output: $output")
            assertTrue(
                output != "Run one of the calls to see its result here.",
                "$label updated the panel",
            )
            assertTrue(page.locator("[data-call]").textContent()!!.isNotBlank(), "$label showed its Kotlin")
        }
    }

    @RetryingTest(maxAttempts = 2)
    fun `the language switch relabels the page without losing the editor`(page: Page) {
        openPage(page)
        goTo(page, "playground")

        page.locator("[data-menu='lang']").first().click()
        page.locator("[data-action='lang:zh-Hans']").click()

        assertEquals("zh-Hans", page.locator("html").getAttribute("lang"))
        assertTrue(page.getByText("读取文本").isVisible(), "the buttons are relabelled in Chinese")
        assertTrue(
            page.locator("[data-editor] .monaco-editor").first().isVisible(),
            "switching the language keeps the same editor instance",
        )
        assertEquals(
            "演练场",
            page.locator("vaadin-side-nav-item[data-route='playground']").textContent(),
        )
    }

    @RetryingTest(maxAttempts = 2)
    fun `page logs no console errors or failed requests`(page: Page) {
        val consoleErrors = mutableListOf<String>()
        val failedRequests = mutableListOf<String>()
        page.onConsoleMessage { message -> if (message.type() == "error") consoleErrors += message.text() }
        page.onRequestFailed { request -> failedRequests += "${request.url()} ${request.failure()}" }

        openPage(page)
        goTo(page, "playground")
        page.waitForSelector("[data-editor] .monaco-editor", Page.WaitForSelectorOptions().setTimeout(20_000.0))
        page.getByText("Add a warning marker").click()

        assertTrue(consoleErrors.isEmpty(), "console errors: $consoleErrors")
        assertTrue(failedRequests.isEmpty(), "failed requests: $failedRequests")
    }

    /** The shell routes on the hash; a click on the sidebar is how a visitor gets there. */
    private fun goTo(page: Page, route: String) {
        page.locator("vaadin-side-nav-item[data-route='$route']").click()
        page.waitForSelector("monaco-$route-page h1")
    }

    private fun openPage(page: Page) {
        page.navigate("/", Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE))
    }
}

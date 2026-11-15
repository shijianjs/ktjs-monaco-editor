package demo

import io.github.shijianjs.ktjs.monaco.Environment
import io.github.shijianjs.ktjs.monaco.Monaco
import js.objects.unsafeJso
import web.dom.document
import web.html.HTMLElement
import web.html.HtmlTagName

private const val PROJECT_NAME = "ktjs-monaco-editor"

/**
 * The worker bundle the page ships next to `demo.js`.
 *
 * monaco's own loader is a Vite-style dynamic import, which webpack cannot resolve, so the demo emits
 * the worker as an extra webpack entry (`demo/webpack.config.d/monaco-worker.js`) and tells monaco
 * where it is. A consumer of the wrapper does the same for their bundler.
 */
private const val WORKER_SCRIPT = "editor.worker.js"

/**
 * Entry point of the demo page.
 *
 * The layout is a two-column page: the live editor on the left, the wrapper calls and their output on
 * the right. The language switch re-labels every control in place — the editor is created once, so
 * switching between English and Chinese keeps the model, the cursor and the decorations.
 */
fun main() {
    val stringsByLanguage = listOf(English, Chinese)

    // Must be set before the first editor exists: monaco reads it when it starts a language service.
    Monaco.environment = unsafeJso<Environment> {
        getWorkerUrl = { _, _ -> WORKER_SCRIPT }
    }

    val output = pre("output", English.emptyOutput)
    val call = pre("call")
    val editorHost = div("editor")
    val playground = Playground(editorHost, output, call)

    val tagline = paragraph("tagline", English.tagline)
    val footer = paragraph("footer", English.footer)
    val editorHeading = heading(2, English.editorHeading)
    val controlsHeading = heading(2, English.controlsHeading)
    val outputHeading = heading(2, English.outputHeading)
    val callHeading = heading(2, English.callHeading)

    val controls = div("actions")
    playground.renderControls(controls, English)

    val switcher = div("switcher")
    val switchButtons = stringsByLanguage.map { strings ->
        button("switch", strings.switchLabel).also { switcher.append(it) }
    }

    fun applyLanguage(index: Int) {
        val strings = stringsByLanguage[index]
        document.title = strings.pageTitle
        tagline.textContent = strings.tagline
        footer.textContent = strings.footer
        editorHeading.textContent = strings.editorHeading
        controlsHeading.textContent = strings.controlsHeading
        outputHeading.textContent = strings.outputHeading
        callHeading.textContent = strings.callHeading
        switchButtons.forEachIndexed { buttonIndex, element ->
            element.className = web.cssom.ClassName(if (buttonIndex == index) "switch active" else "switch")
        }
        playground.apply(strings)
    }

    switchButtons.forEachIndexed { index, element ->
        element.onClick { applyLanguage(index) }
    }

    val header = document.createElement(HtmlTagName.header).apply {
        className = web.cssom.ClassName("header")
    }
    val brand = div("brand")
    brand.append(
        document.createElement(HtmlTagName.img).apply {
            className = web.cssom.ClassName("logo")
            setAttribute("src", "img/ktjs-monaco-editor-logo.png")
            setAttribute("alt", "$PROJECT_NAME logo")
        },
        div("brand-text").also {
            it.append(heading(1, PROJECT_NAME), tagline)
        },
    )
    header.append(brand, switcher)

    val main = div("layout")
    val left = div("panel editor-panel")
    left.append(editorHeading, editorHost)
    val right = div("panel side-panel")
    right.append(controlsHeading, controls, outputHeading, output, callHeading, call)

    main.append(left, right)

    val page: HTMLElement = div("page")
    page.append(header, main, footer)
    document.body.append(page)

    document.title = English.pageTitle
    // Labels every control once; the language switch re-runs the same function.
    applyLanguage(0)
}

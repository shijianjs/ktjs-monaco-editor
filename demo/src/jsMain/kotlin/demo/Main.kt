package demo

import io.github.shijianjs.ktjs.monaco.Environment
import io.github.shijianjs.ktjs.monaco.Monaco
import io.github.shijianjs.ktjs.demo.i18n.LanguageState
import io.github.shijianjs.ktjs.demo.layout.DemoLayout
import io.github.shijianjs.ktjs.demo.layout.LayoutConfig
import io.github.shijianjs.ktjs.demo.style.Theme
import js.objects.unsafeJso
import web.dom.ElementId
import web.dom.document

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
 * The shell comes from `ktjs-demo-kit`; this file only sets the one thing monaco needs before an
 * editor exists and mounts the menu.
 */
fun main() {
    // Must be set before the first editor exists: monaco reads it when it starts a language service.
    Monaco.environment = unsafeJso<Environment> {
        getWorkerUrl = { _, _ -> WORKER_SCRIPT }
    }

    val language = LanguageState(storageKey = "ktjs-monaco-editor-language")
    language.init()

    val theme = Theme()
    theme.applyStored()

    DemoLayout.define()
    val layout = document.getElementById(ElementId("demo")) as? DemoLayout
        ?: error("index.html has no <ktjs-demo-layout id=\"demo\">")

    layout.mount(
        LayoutConfig(
            menu = siteMenu(),
            language = language,
            theme = theme,
            texts = siteTexts,
            siteVersion = SITE_VERSION,
            docsUrl = DOCS_URL,
            apiUrl = API_URL,
            sourceUrl = SOURCE_URL,
            logoSrc = "img/ktjs-monaco-editor-logo.png",
        ),
    )
}

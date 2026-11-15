package demo

import web.cssom.ClassName
import web.dom.document
import web.events.EventHandler
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * DOM helpers for the demo.
 *
 * The page is built node by node — no HTML strings — so every element the demo shows is created by
 * Kotlin code the compiler can check. `kotlin-browser`'s factories are used directly rather than
 * `org.w3c.dom` or `kotlinx.browser` (both banned by `detekt.yml`).
 */
internal fun div(className: String? = null, block: HTMLElement.() -> Unit = {}): HTMLElement =
    document.createElement(HtmlTagName.div).apply {
        if (className != null) this.className = ClassName(className)
        block()
    }

internal fun heading(level: Int, text: String): HTMLElement {
    val tag = when (level) {
        1 -> HtmlTagName.h1
        2 -> HtmlTagName.h2
        else -> HtmlTagName.h3
    }
    return document.createElement(tag).apply { textContent = text }
}

internal fun paragraph(className: String? = null, text: String): HTMLElement =
    document.createElement(HtmlTagName.p).apply {
        if (className != null) this.className = ClassName(className)
        textContent = text
    }

internal fun HTMLElement.onClick(handler: () -> Unit) {
    onclick = EventHandler { handler() }
}

internal fun pre(className: String, text: String = ""): HTMLElement =
    document.createElement(HtmlTagName.pre).apply {
        this.className = ClassName(className)
        textContent = text
    }

internal fun button(className: String, text: String = ""): HTMLElement =
    document.createElement(HtmlTagName.button).apply {
        this.className = ClassName(className)
        textContent = text
    }

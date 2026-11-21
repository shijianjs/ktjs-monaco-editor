package demo.page.playground

import io.github.shijianjs.ktjs.demo.style.Palette
import io.github.shijianjs.ktjs.dom.element.inlineStyle
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementAdapter
import web.cssom.Display
import web.cssom.FontFamily
import web.cssom.Margin
import web.cssom.Overflow
import web.cssom.Padding
import web.cssom.WhiteSpace
import web.cssom.px

/**
 * `<monaco-code-block>`: a read-only block of source code — monospace on a raised surface, scrolling
 * inside a fixed height.
 *
 * The pages write the text into it (`textContent`), so the element only owns the shape: it is the `<pre>`
 * the source is shown in, with the whitespace and the scroll of one.
 */
internal class CodeBlock : CustomElementAdapter() {
    /** Painted on connect: the constructor of a custom element may not set the element's attributes. */
    override fun connectedCallback() {
        inlineStyle.apply {
            // A custom element is inline by default, and `<pre>` is the block this stands in for.
            display = Display.block
            margin = Margin(0.px, 0.px)
            padding = Padding(12.px, 12.px)
            fontFamily = FontFamily.monospace
            fontSize = 13.px
            whiteSpace = WhiteSpace.preWrap
            backgroundColor = Palette.surfaceRaised
            borderRadius = 8.px
            overflow = Overflow.scroll
            maxHeight = 240.px
        }
    }

    companion object : CodeBlockDefaultReference()
}

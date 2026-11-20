package demo.page.playground

import io.github.shijianjs.ktjs.demo.page.DemoPage
import io.github.shijianjs.ktjs.demo.page.Grid
import io.github.shijianjs.ktjs.demo.page.SectionBlock
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import web.components.CustomElementReference
import web.dom.TagName
import io.github.shijianjs.ktjs.demo.i18n.Binder
import io.github.shijianjs.ktjs.demo.i18n.Lang
import io.github.shijianjs.ktjs.demo.i18n.Langs
import io.github.shijianjs.ktjs.demo.i18n.Translations
import io.github.shijianjs.ktjs.demo.i18n.texts
import io.github.shijianjs.ktjs.demo.style.Palette
import io.github.shijianjs.ktjs.dom.element.inlineStyle
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.monaco.MarkerSeveritys
import io.github.shijianjs.ktjs.monaco.Monaco
import io.github.shijianjs.ktjs.monaco.editor.IIdentifiedSingleEditOperation
import io.github.shijianjs.ktjs.monaco.editor.IMarkerData
import io.github.shijianjs.ktjs.monaco.editor.IModelDecorationOptions
import io.github.shijianjs.ktjs.monaco.editor.IModelDeltaDecoration
import io.github.shijianjs.ktjs.monaco.editor.IStandaloneCodeEditor
import io.github.shijianjs.ktjs.monaco.editor.IStandaloneEditorConstructionOptions
import io.github.shijianjs.ktjs.vaadin.button.ButtonElement
import io.github.shijianjs.ktjs.vaadin.button.createButton
import js.objects.unsafeJso
import web.animations.requestAnimationFrame
import web.cssom.Border
import web.cssom.CSSStyleSheet
import web.cssom.FontSize
import web.cssom.LineStyle
import web.cssom.Overflow
import web.cssom.pct
import web.cssom.px
import web.dom.clickEvent
import web.dom.document
import web.events.addHandler
import web.html.HTMLElement
import web.html.HtmlTagName

/** Every string this page shows, in one language. */
private class PlaygroundTexts(
    val title: String,
    val summary: String,
    val editorHeading: String,
    val controlsHeading: String,
    val outputHeading: String,
    val callHeading: String,
    val emptyOutput: String,
    val readValue: String,
    val setValue: String,
    val insertLine: String,
    val highlightLine: String,
    val addMarker: String,
    val showSelection: String,
    val showCursor: String,
    val nextTheme: String,
    val nextLanguage: String,
    val themes: List<String>,
    val languages: List<String>,
    val sampleMarker: String,
    val selectionResult: String,
    val cursorResult: String,
    val insertedLine: String,
    val highlightedLine: String,
    val nowEditing: String,
)

private val playgroundEnglish: PlaygroundTexts = PlaygroundTexts(
    title = "Playground",
    summary = "One editor, one wrapper call per button, and the Kotlin that ran underneath — the " +
        "editor is created once, so switching language or theme never drops the model.",
    editorHeading = "A live editor",
    controlsHeading = "Wrapper calls",
    outputHeading = "Result",
    callHeading = "The Kotlin that ran",
    emptyOutput = "Run one of the calls to see its result here.",
    readValue = "Read the text",
    setValue = "Replace the text",
    insertLine = "Insert a line",
    highlightLine = "Highlight line 3",
    addMarker = "Add a warning marker",
    showSelection = "Show the selection",
    showCursor = "Show the cursor",
    nextTheme = "Next theme",
    nextLanguage = "Next language",
    themes = listOf("vs", "vs-dark", "hc-black"),
    languages = listOf("kotlin", "plaintext", "markdown"),
    sampleMarker = "This line is flagged by the demo.",
    selectionResult = "selection",
    cursorResult = "cursor",
    insertedLine = "line inserted at the end of the model",
    highlightedLine = "line 3 is highlighted",
    nowEditing = "the model now uses language",
)

private val playgroundChinese: PlaygroundTexts = PlaygroundTexts(
    title = "演练场",
    summary = "一个编辑器，每个按钮一次封装调用，下面是被执行的那段 Kotlin——编辑器只建一次，切换语言" +
        "或主题都不会丢掉模型。",
    editorHeading = "可交互的编辑器",
    controlsHeading = "封装调用",
    outputHeading = "结果",
    callHeading = "实际执行的 Kotlin 代码",
    emptyOutput = "点击任意一个调用，结果会显示在这里。",
    readValue = "读取文本",
    setValue = "替换文本",
    insertLine = "插入一行",
    highlightLine = "高亮第 3 行",
    addMarker = "添加警告标记",
    showSelection = "查看选区",
    showCursor = "查看光标",
    nextTheme = "切换主题",
    nextLanguage = "切换语言",
    themes = listOf("vs", "vs-dark", "hc-black"),
    languages = listOf("kotlin", "plaintext", "markdown"),
    sampleMarker = "这一行被 demo 标记了。",
    selectionResult = "选区",
    cursorResult = "光标",
    insertedLine = "已在模型末尾插入一行",
    highlightedLine = "第 3 行已高亮",
    nowEditing = "模型现在的语言是",
)

private val playgroundTexts: Translations<PlaygroundTexts> = texts(
    Langs.EN to playgroundEnglish,
    Langs.ZH_HANS to playgroundChinese,
)

/**
 * The playground page: the editor on the left, one button per wrapper call on the right.
 *
 * The editor is created once — the language switch only re-labels the buttons, so the model, the
 * cursor and the decorations survive it. Creation waits for the host to be in the document:
 * `automaticLayout` measures through it, and a page's field initialiser runs before the router
 * attaches the node.
 */
internal class PlaygroundPage : DemoPage() {

    private val texts = Binder(playgroundTexts)

    /** The strings the calls read when they run; kept in step with the language switch. */
    private var current: PlaygroundTexts = playgroundEnglish

    /** The editor, filled in once the host element is mounted. */
    private var editor: IStandaloneCodeEditor? = null

    /** The ids `deltaDecorations` returned last time; passing them back replaces the highlight. */
    private var decorations: Array<String> = emptyArray()

    private var themeIndex = 0
    private var languageIndex = 0

    private val editorHost: HTMLElement = HtmlTagName.div {
        setAttribute("data-editor", "")
        inlineStyle.apply {
            width = 100.pct
            height = 520.px
            border = Border(1.px, LineStyle.solid, Palette.border)
            borderRadius = 8.px
            overflow = Overflow.hidden
        }
    }

    private val output: CodeBlock = CodeBlock.NAME().apply {
        setAttribute("data-output", "")
        textContent = current.emptyOutput
    }

    private val call: CodeBlock = CodeBlock.NAME().apply { setAttribute("data-call", "") }

    /**
     * Every wrapper call the page offers, in the order its button is created.
     *
     * A call takes the current [PlaygroundTexts] as an argument instead of capturing one instance,
     * so both its label and the sentence it returns follow the language switch.
     */
    private val calls: List<Call> = listOf(
        Call({ it.readValue }, "editor.getValue()") { _ ->
            editor?.getValue().orEmpty()
        },
        Call({ it.setValue }, "editor.setValue(SAMPLE)") { _ ->
            editor?.setValue(SAMPLE)
            editor?.getValue().orEmpty()
        },
        Call(
            { it.insertLine },
            """
                editor.executeEdits("demo", arrayOf(unsafeJso<IIdentifiedSingleEditOperation> {
                    range = Monaco.range(lineCount + 1.0, 1.0, lineCount + 1.0, 1.0)
                    text = "// inserted by the demo"
                }))
            """.trimIndent(),
        ) { strings ->
            val model = editor?.getModel()
            val lineCount = model?.getLineCount() ?: 1.0
            editor?.executeEdits(
                "demo",
                arrayOf(
                    unsafeJso<IIdentifiedSingleEditOperation> {
                        range = Monaco.range(lineCount + 1.0, 1.0, lineCount + 1.0, 1.0)
                        text = "// inserted by the demo"
                    },
                ),
            )
            "${strings.insertedLine}: ${model?.getLineCount()}"
        },
        Call(
            { it.highlightLine },
            """
                decorations = editor.deltaDecorations(decorations, arrayOf(
                    unsafeJso<IModelDeltaDecoration> {
                        range = Monaco.range(3.0, 1.0, 3.0, 1.0)
                        options = unsafeJso<IModelDecorationOptions> {
                            isWholeLine = true
                            className = "demo-line-highlight"
                        }
                    },
                ))
            """.trimIndent(),
        ) { strings ->
            val target = editor
            if (target == null) {
                ""
            } else {
                decorations = target.deltaDecorations(
                    decorations,
                    arrayOf(
                        unsafeJso<IModelDeltaDecoration> {
                            range = Monaco.range(3.0, 1.0, 3.0, 1.0)
                            options = unsafeJso<IModelDecorationOptions> {
                                isWholeLine = true
                                className = "demo-line-highlight"
                            }
                        },
                    ),
                )
                target.revealLineInCenter(3.0)
                strings.highlightedLine
            }
        },
        Call(
            { it.addMarker },
            """
                Monaco.editor.setModelMarkers(editor.getModel()!!, "ktjs-demo", arrayOf(
                    unsafeJso<IMarkerData> {
                        severity = MarkerSeveritys.Warning
                        message = "…"
                        startLineNumber = 2.0
                        startColumn = 5.0
                        endLineNumber = 2.0
                        endColumn = 12.0
                    },
                ))
            """.trimIndent(),
        ) { strings ->
            val model = editor?.getModel()
            if (model == null) {
                "no model"
            } else {
                Monaco.editor.setModelMarkers(
                    model,
                    "ktjs-demo",
                    arrayOf(
                        unsafeJso<IMarkerData> {
                            severity = MarkerSeveritys.Warning
                            message = strings.sampleMarker
                            startLineNumber = 2.0
                            startColumn = 5.0
                            endLineNumber = 2.0
                            endColumn = 12.0
                        },
                    ),
                )
                val count = Monaco.editor.getModelMarkers(unsafeJso { owner = "ktjs-demo" }).size
                "owner \"ktjs-demo\": $count marker(s)"
            }
        },
        Call({ it.showSelection }, "editor.getSelection()") { strings ->
            val selection = editor?.getSelection()
            if (selection == null) {
                "no selection"
            } else {
                "${strings.selectionResult}: " +
                    "${selection.selectionStartLineNumber},${selection.selectionStartColumn} -> " +
                    "${selection.positionLineNumber},${selection.positionColumn}"
            }
        },
        Call({ it.showCursor }, "editor.getPosition()") { strings ->
            val position = editor?.getPosition()
            "${strings.cursorResult}: ${position?.lineNumber}, ${position?.column}"
        },
        Call({ it.nextTheme }, "Monaco.editor.setTheme(themes[index])") { strings ->
            themeIndex = (themeIndex + 1) % strings.themes.size
            val theme = strings.themes[themeIndex]
            Monaco.editor.setTheme(theme)
            "theme: $theme"
        },
        Call(
            { it.nextLanguage },
            "Monaco.editor.setModelLanguage(editor.getModel()!!, languages[index])",
        ) { strings ->
            languageIndex = (languageIndex + 1) % strings.languages.size
            val language = strings.languages[languageIndex]
            editor?.getModel()?.let { Monaco.editor.setModelLanguage(it, language) }
            "${strings.nowEditing}: $language"
        },
    )

    private val buttons: List<ButtonElement> = calls.map { createButton() }


    init {
        // The two columns carry their own headings, so the section block stays title-less.
        val section = SectionBlock.NAME { show() }
        val editorHeading = HtmlTagName.h2 { textContent = current.editorHeading }
        val controlsHeading = HtmlTagName.h2 { textContent = current.controlsHeading }
        val outputHeading = HtmlTagName.h2 { textContent = current.outputHeading }
        val callHeading = HtmlTagName.h2 { textContent = current.callHeading }

        val left = HtmlTagName.div { append(editorHeading, editorHost) }
        val right = HtmlTagName.div {
            append(
                controlsHeading,
                HtmlTagName.div { append(*buttons.toTypedArray()) },
                outputHeading,
                output,
                callHeading,
                call,
            )
        }

        section.body.append(Grid.NAME { show(360); add(left, right) })
        body.append(section.root)

        buttons.forEachIndexed { index, button ->
            button.clickEvent.addHandler {
                val call = calls[index]
                output.textContent = call.run(current)
                this.call.textContent = call.snippet
            }
        }

        createOnceMounted()
    }

    /** Creates the editor as soon as the host is in the document; see the class comment. */
    private fun createOnceMounted() {
        if (editor != null) return
        if (!editorHost.isConnected) {
            requestAnimationFrame { _ -> createOnceMounted() }
            return
        }
        adoptInjectedStyles()
        editor = Monaco.editor.create(
            editorHost,
            unsafeJso<IStandaloneEditorConstructionOptions> {
                value = SAMPLE
                language = "kotlin"
                theme = "vs"
                automaticLayout = true
                fontSize = 14.0
                minimap = unsafeJso { enabled = false }
                scrollBeyondLastLine = false
                tabSize = 2.0
            },
        )
    }

    /**
     * Copies the page-level styles monaco injects into the page's own shadow tree.
     *
     * monaco's CSS arrives through `style-loader`, which writes `<style>` nodes into `document.head`.
     * Styles outside a shadow root do not apply inside it, and the demo page is a shadow tree — so
     * without this the editor renders as a bare textarea with a scrollbar. Linked sheets (`href !=
     * null`, Lumo and PhotoSwipe-style packages) are skipped: their tokens are inherited custom
     * properties, not rules the editor needs.
     */
    private fun adoptInjectedStyles() {
        val css = StringBuilder()
        val sheets = document.styleSheets
        for (sheetIndex in 0 until sheets.length) {
            val sheet = sheets.item(sheetIndex) ?: continue
            if (sheet.href != null) continue
            val rules = runCatching { sheet.cssRules }.getOrNull() ?: continue
            for (ruleIndex in 0 until rules.length) {
                css.append(rules.item(ruleIndex)?.cssText ?: "").append('\n')
            }
        }
        if (css.isEmpty()) return
        addSheet(CSSStyleSheet().apply { replaceSync(css.toString()) })
    }

    override fun applyTranslation(lang: Lang) {
        current = playgroundTexts[lang]
        setTitle(current.title)
        setSummary(current.summary)
        texts.apply(lang)
        buttons.forEachIndexed { index, button -> button.textContent = calls[index].label(current) }
        output.textContent = current.emptyOutput
        call.textContent = ""
    }

    /** One wrapper call: its label, the Kotlin source shown under the result, and the call itself. */
    private class Call(
        val label: (PlaygroundTexts) -> String,
        val snippet: String,
        val run: (PlaygroundTexts) -> String,
    )

    companion object : CustomElementReference<PlaygroundPage>(PlaygroundPage::class.js) {
        val NAME: TagName<PlaygroundPage> = TagName("monaco-playground-page")

        fun define() {
            defineIfMissing(NAME, this)
        }

        /** Kotlin, so the built-in Monarch grammar highlights it without starting a language service. */
        val SAMPLE = """
            fun main() {
                val greeting = "Hello, Kotlin/JS"
                println(greeting)
            }
        """.trimIndent()
    }
}

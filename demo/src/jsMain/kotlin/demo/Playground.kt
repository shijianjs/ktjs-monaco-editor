package demo

import io.github.shijianjs.ktjs.monaco.MarkerSeveritys
import io.github.shijianjs.ktjs.monaco.Monaco
import io.github.shijianjs.ktjs.monaco.editor.IIdentifiedSingleEditOperation
import io.github.shijianjs.ktjs.monaco.editor.IMarkerData
import io.github.shijianjs.ktjs.monaco.editor.IModelDecorationOptions
import io.github.shijianjs.ktjs.monaco.editor.IModelDeltaDecoration
import io.github.shijianjs.ktjs.monaco.editor.IStandaloneCodeEditor
import io.github.shijianjs.ktjs.monaco.editor.IStandaloneEditorConstructionOptions
import js.objects.unsafeJso
import web.html.HTMLElement

/**
 * The demo's editor plus the calls the page makes on it.
 *
 * Every button is one wrapper call, and the panel underneath shows the Kotlin source of the call that
 * just ran — the page is a live example of the wrapper, not a screenshot of one. The editor is created
 * once and the language switch only re-labels the controls, so switching locales never destroys state.
 */
internal class Playground(private val editorHost: HTMLElement, private val output: HTMLElement, private val call: HTMLElement) {
    private val editor: IStandaloneCodeEditor = Monaco.editor.create(
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

    /** The ids `deltaDecorations` returned last time; passing them back replaces the highlight. */
    private var decorations: Array<String> = emptyArray()

    private var themeIndex = 0
    private var languageIndex = 0

    private var strings: Strings = English

    /** The actions in the order the page lists them; the label is looked up from [strings]. */
    private val actions: List<Action> = listOf(
        Action(
            label = { it.readValue },
            snippet = "editor.getValue()",
        ) { editor.getValue() },
        Action(
            label = { it.setValue },
            snippet = "editor.setValue(SAMPLE)",
        ) {
            editor.setValue(SAMPLE)
            editor.getValue()
        },
        Action(
            label = { it.insertLine },
            snippet = """
                editor.executeEdits("demo", arrayOf(unsafeJso<IIdentifiedSingleEditOperation> {
                    range = Monaco.range(lineCount + 1.0, 1.0, lineCount + 1.0, 1.0)
                    text = "// inserted by the demo"
                }))
            """.trimIndent(),
        ) {
            val model = editor.getModel()
            val lineCount = model?.getLineCount() ?: 1.0
            editor.executeEdits(
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
        Action(
            label = { it.highlightLine },
            snippet = """
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
        ) {
            decorations = editor.deltaDecorations(
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
            editor.revealLineInCenter(3.0)
            strings.highlightedLine
        },
        Action(
            label = { it.addMarker },
            snippet = """
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
        ) {
            val model = editor.getModel()
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
        Action(
            label = { it.showSelection },
            snippet = "editor.getSelection()",
        ) {
            val selection = editor.getSelection()
            if (selection == null) {
                "no selection"
            } else {
                "${strings.selectionResult}: " +
                    "${selection.selectionStartLineNumber},${selection.selectionStartColumn} -> " +
                    "${selection.positionLineNumber},${selection.positionColumn}"
            }
        },
        Action(
            label = { it.showCursor },
            snippet = "editor.getPosition()",
        ) {
            val position = editor.getPosition()
            "${strings.cursorResult}: ${position?.lineNumber}, ${position?.column}"
        },
        Action(
            label = { it.nextTheme },
            snippet = "Monaco.editor.setTheme(themes[index])",
        ) {
            themeIndex = (themeIndex + 1) % strings.themes.size
            val theme = strings.themes[themeIndex]
            Monaco.editor.setTheme(theme)
            "theme: $theme"
        },
        Action(
            label = { it.nextLanguage },
            snippet = "Monaco.editor.setModelLanguage(editor.getModel()!!, languages[index])",
        ) {
            languageIndex = (languageIndex + 1) % strings.languages.size
            val language = strings.languages[languageIndex]
            editor.getModel()?.let { Monaco.editor.setModelLanguage(it, language) }
            "${strings.nowEditing}: $language"
        },
    )

    /** Re-labels every control for [strings] and resets the panels, without touching the editor. */
    fun apply(strings: Strings) {
        this.strings = strings
        output.textContent = strings.emptyOutput
        call.textContent = ""
        buttons.forEachIndexed { index, button ->
            button.textContent = actions[index].label(strings)
        }
    }

    private val buttons: MutableList<HTMLElement> = mutableListOf()

    fun renderControls(into: HTMLElement, strings: Strings) {
        this.strings = strings
        actions.forEach { action ->
            val control = button("action")
            into.append(control)
            buttons += control
            control.onClick {
                val result = action.run()
                output.textContent = result
                call.textContent = action.snippet
            }
        }
    }

    /** One wrapper call the page offers. */
    private class Action(val label: (Strings) -> String, val snippet: String, val run: () -> String)

    private companion object {
        /** Kotlin, so the built-in Monarch grammar highlights it without starting a language service. */
        val SAMPLE = """
            fun main() {
                val greeting = "Hello, Kotlin/JS"
                println(greeting)
            }
        """.trimIndent()
    }
}


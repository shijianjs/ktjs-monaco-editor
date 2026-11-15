package io.github.shijianjs.ktjs.monaco

import io.github.shijianjs.ktjs.monaco.editor.BuiltinThemes
import io.github.shijianjs.ktjs.monaco.editor.IStandaloneCodeEditor
import io.github.shijianjs.ktjs.monaco.editor.IStandaloneEditorConstructionOptions
import io.github.shijianjs.ktjs.monaco.editor.IStandaloneThemeData
import js.objects.unsafeJso
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.js.unsafeCast
import web.dom.document
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * Browser tests: every case drives the real `monaco-editor` package, so a wrong `@JsName`, a wrong
 * value type or a dropped member fails here instead of at a consumer's runtime.
 *
 * No language service worker is started: the cases use `plaintext` and the built-in `kotlin`
 * Monarch grammar, both of which tokenize on the main thread.
 */
class MonacoTest {
    private fun container(): HTMLElement {
        val host = document.createElement(HtmlTagName.div)
        host.style.width = "800px"
        host.style.height = "600px"
        document.body.appendChild(host)
        return host
    }

    @Test
    fun uriExposesItsComponents() {
        val uri = Monaco.uri("file:///src/main.kt?x=1#frag")
        assertEquals("file", uri.scheme)
        assertEquals("/src/main.kt", uri.path)
        assertEquals("x=1", uri.query)
        assertEquals("frag", uri.fragment)
        // `toString()` percent-encodes the query separator (`x%3D1`); `toString(true)` keeps it raw.
        assertEquals("file:///src/main.kt?x%3D1#frag", uri.toString())
        assertEquals("file:///src/main.kt?x=1#frag", uri.toString(true))
        assertTrue(Monaco.Uri.isUri(uri))
        assertEquals(uri.path, Monaco.Uri.revive(UriComponentsOrUri(uri.toJSON())).path)
    }

    @Test
    fun positionRangeAndSelectionFollowTheDocumentedOrder() {
        val start = Monaco.position(1.0, 1.0)
        val end = Monaco.position(3.0, 5.0)
        val range = Monaco.range(1.0, 1.0, 3.0, 5.0)
        assertEquals(1.0, range.startLineNumber)
        assertEquals(3.0, range.endLineNumber)
        assertTrue(range.containsPosition(start))
        assertTrue(range.strictContainsRange(Monaco.range(1.0, 1.0, 1.0, 1.0)).not())
        assertEquals(range.endLineNumber, end.lineNumber)

        val selection = Monaco.selection(1.0, 1.0, 3.0, 5.0)
        assertEquals(SelectionDirections.LTR, selection.getDirection())
        // Two `Range` instances are distinct objects, so compare through monaco's own `equalsRange`.
        assertTrue(
            range.equalsRange(Monaco.Range.fromPositions(selection.getSelectionStart(), selection.getPosition())),
        )
    }

    @Test
    fun keyConstantsMatchTheUpstreamValues() {
        // Stable across monaco-editor releases: KeyMod.CtrlCmd is 2048, KeyCode.Enter is 3,
        // MarkerSeverity.Error is 8.
        assertEquals(2048.0, Monaco.KeyMod.CtrlCmd)
        assertEquals(3.0, KeyCodes.Enter.unsafeCast<Number>().toDouble())
        assertEquals(8.0, MarkerSeveritys.Error.unsafeCast<Number>().toDouble())
    }

    @Test
    fun editorCreatesAModelAndRoundTripsText() {
        val editor = createEditor("plaintext", "val answer = 42")
        try {
            assertEquals("val answer = 42", editor.getValue())
            editor.setValue("val answer = 43")
            assertEquals("val answer = 43", editor.getModel()?.getValue())
        } finally {
            editor.dispose()
        }
    }

    @Test
    fun namespaceKeepsTrackOfCreatedModels() {
        val model = Monaco.editor.createModel("hello", "plaintext", Monaco.uri("inmemory://model/1"))
        try {
            assertTrue(Monaco.editor.getModels().any { it === model })
        } finally {
            model.dispose()
        }
    }

    @Test
    fun markersAreStoredOnTheModel() {
        val model = Monaco.editor.createModel("oops", "plaintext")
        try {
            Monaco.editor.setModelMarkers(
                model,
                "ktjs-test",
                arrayOf(
                    unsafeJso {
                        startLineNumber = 1.0
                        startColumn = 1.0
                        endLineNumber = 1.0
                        endColumn = 5.0
                        message = "something is off"
                        severity = MarkerSeveritys.Error
                    },
                ),
            )
            val markers = Monaco.editor.getModelMarkers(unsafeJso { owner = "ktjs-test" })
            assertEquals(1, markers.size)
            assertEquals("something is off", markers[0].message)
        } finally {
            model.dispose()
        }
    }

    @Test
    fun builtInMonarchLanguageTokenizes() {
        // The package entry registers every built-in language, so `kotlin` tokenizes without a worker.
        val lines = Monaco.editor.tokenize("val answer = 42", "kotlin")
        assertEquals(1, lines.size)
        assertTrue(lines[0].isNotEmpty())
        // `Token.offset` is where the token starts, so the first token starts at 0 and the offsets grow.
        val offsets = lines[0].map { it.offset }
        assertEquals(0.0, offsets.first())
        assertEquals(offsets.sorted(), offsets)
        assertTrue(lines[0].all { it.language == "kotlin" })
    }

    @Test
    fun builtInLanguagesAreRegistered() {
        assertTrue(Monaco.languages.getLanguages().any { it.id == "kotlin" })
    }

    @Test
    fun themesCanBeDefinedAndSwitched() {
        Monaco.editor.defineTheme(
            "ktjs-test-theme",
            unsafeJso<IStandaloneThemeData> {
                base = BuiltinThemes.vs
                inherit = true
                rules = emptyArray()
                colors = unsafeJso()
            },
        )
        Monaco.editor.setTheme("ktjs-test-theme")
    }

    @Test
    fun environmentIsReadBackFromWindow() {
        val previous = Monaco.environment
        try {
            Monaco.environment = unsafeJso { globalAPI = true }
            assertEquals(true, Monaco.environment?.globalAPI)
        } finally {
            Monaco.environment = previous
        }
    }

    private fun createEditor(language: String, text: String): IStandaloneCodeEditor =
        Monaco.editor.create(
            container(),
            unsafeJso<IStandaloneEditorConstructionOptions> {
                value = text
                this.language = language
                automaticLayout = true
            },
        )
}

package demo.page.playground

import io.github.shijianjs.ktjs.dom.element.invoke

/** Every string this page shows, in one language. */
internal class PlaygroundTexts(
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

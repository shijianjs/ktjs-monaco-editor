package demo

/**
 * The demo's localisation table.
 *
 * Plain Kotlin rather than `external` declarations: the demo is this repository's own page, so its
 * strings are checked like any other Kotlin value. Each language is one object implementing [Strings],
 * and adding a language means adding one object — the compiler lists every property that is still
 * missing.
 */
internal interface Strings {
    /** The label on the language switch for this language. */
    val switchLabel: String

    val title: String
    val tagline: String
    val editorHeading: String
    val controlsHeading: String
    val outputHeading: String
    val callHeading: String

    val readValue: String
    val setValue: String
    val insertLine: String
    val highlightLine: String
    val addMarker: String
    val showSelection: String
    val showCursor: String
    val nextTheme: String
    val nextLanguage: String

    val emptyOutput: String
    val valueOf: String
    val themes: Array<String>
    val languages: Array<String>
    val sampleMarker: String
    val selectionResult: String
    val cursorResult: String
    val insertedLine: String
    val highlightedLine: String
    val nowEditing: String

    /** The sentence under the editor that names where the wrapper comes from. */
    val footer: String

    /** `demo/src/jsMain/resources/index.html` has a static `<title>`; the demo rewrites it. */
    val pageTitle: String
}

internal object English : Strings {
    override val switchLabel = "English"
    override val title = "ktjs-monaco-editor"
    override val tagline = "monaco-editor 0.57.0 wrapped for Kotlin/JS"
    override val editorHeading = "A live editor"
    override val controlsHeading = "Wrapper calls"
    override val outputHeading = "Result"
    override val callHeading = "The Kotlin that ran"
    override val readValue = "Read the text"
    override val setValue = "Replace the text"
    override val insertLine = "Insert a line"
    override val highlightLine = "Highlight line 3"
    override val addMarker = "Add a warning marker"
    override val showSelection = "Show the selection"
    override val showCursor = "Show the cursor"
    override val nextTheme = "Next theme"
    override val nextLanguage = "Next language"
    override val emptyOutput = "Run one of the calls to see its result here."
    override val valueOf = "editor.getValue()"
    override val themes = arrayOf("vs", "vs-dark", "hc-black")
    override val languages = arrayOf("kotlin", "plaintext", "markdown")
    override val sampleMarker = "This line is flagged by the demo."
    override val selectionResult = "selection"
    override val cursorResult = "cursor"
    override val insertedLine = "line inserted at the end of the model"
    override val highlightedLine = "line 3 is highlighted"
    override val nowEditing = "the model now uses language"
    override val footer =
        "Every call on this page goes through ktjs-monaco-editor: typed declarations over the " +
            "monaco-editor package, no hand-written JavaScript."
    override val pageTitle = "ktjs-monaco-editor demo"
}

internal object Chinese : Strings {
    override val switchLabel = "简体中文"
    override val title = "ktjs-monaco-editor"
    override val tagline = "monaco-editor 0.57.0 的 Kotlin/JS 封装"
    override val editorHeading = "可交互的编辑器"
    override val controlsHeading = "封装调用"
    override val outputHeading = "结果"
    override val callHeading = "实际执行的 Kotlin 代码"
    override val readValue = "读取文本"
    override val setValue = "替换文本"
    override val insertLine = "插入一行"
    override val highlightLine = "高亮第 3 行"
    override val addMarker = "添加警告标记"
    override val showSelection = "查看选区"
    override val showCursor = "查看光标"
    override val nextTheme = "切换主题"
    override val nextLanguage = "切换语言"
    override val emptyOutput = "点击左侧任意一个调用，结果会显示在这里。"
    override val valueOf = "editor.getValue()"
    override val themes = arrayOf("vs", "vs-dark", "hc-black")
    override val languages = arrayOf("kotlin", "plaintext", "markdown")
    override val sampleMarker = "这一行被 demo 标记了。"
    override val selectionResult = "选区"
    override val cursorResult = "光标"
    override val insertedLine = "已在模型末尾插入一行"
    override val highlightedLine = "第 3 行已高亮"
    override val nowEditing = "模型现在的语言是"
    override val footer = "本页每个调用都经过 ktjs-monaco-editor：对 monaco-editor 包的带类型声明，没有手写 JavaScript。"
    override val pageTitle = "ktjs-monaco-editor 演示"
}

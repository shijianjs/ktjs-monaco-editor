package io.github.shijianjs.ktjs.monaco

import io.github.shijianjs.ktjs.monaco.editor.EditorNamespace
import io.github.shijianjs.ktjs.monaco.languages.LanguagesNamespace
import web.window.window
import kotlin.js.JsAny
import kotlin.js.unsafeCast

/**
 * The entry point of the wrapper: the three namespaces `monaco-editor` exports, its value classes,
 * the factories that build them, and the `MonacoEnvironment` hook.
 *
 * ```kotlin
 * val editor = Monaco.editor.create(container, unsafeJso<IStandaloneEditorConstructionOptions> {
 *     value = "fun main() {}"
 *     language = "kotlin"
 * })
 * val range = Monaco.range(1.0, 1.0, 2.0, 5.0)
 * ```
 *
 * The namespaces are plain JS objects, so these are getters onto the module rather than copies.
 */
object Monaco {
    /**
     * `monaco.editor`: creating editors, models and themes.
     *
     * Upstream: `const editor` in `editor.api.d.ts`.
     */
    val editor: EditorNamespace
        get() = monacoEditorModule

    /**
     * `monaco.languages`: registering languages, providers and tokenizers.
     *
     * Upstream: `const languages` in `editor.api.d.ts`.
     */
    val languages: LanguagesNamespace
        get() = monacoLanguagesModule

    /**
     * The global `MonacoEnvironment`, the hook monaco reads before it starts a language service
     * worker.
     *
     * Upstream declares it as `declare global { interface Window { MonacoEnvironment?: Environment } }`,
     * i.e. a property on `window`, which is why this is a property of the wrapper object rather
     * than a module export. Setting it to `null` clears the hook.
     *
     * ```kotlin
     * Monaco.environment = unsafeJso<Environment> {
     *     getWorker = { _, label -> Worker("$label.worker.js") }
     * }
     * ```
     */
    var environment: Environment?
        get() = window.unsafeCast<MonacoEnvironmentWindow>().MonacoEnvironment
        set(value) {
            window.unsafeCast<MonacoEnvironmentWindow>().MonacoEnvironment = value
        }

    /**
     * The `Uri` export, whose statics build and parse URIs: `Monaco.Uri.parse("file:///a.kt")`.
     *
     * Upstream exports `Uri` as a value, and a Kotlin `external class` can only import a module's
     * *default* export, so the statics live here instead of on a companion object.
     */
    val Uri: UriStatics
        get() = uriStatics

    /** The `Position` export: its statics compare and lift positions. */
    val Position: PositionStatics
        get() = positionStatics

    /** The `Range` export: its statics build and compare ranges. */
    val Range: RangeStatics
        get() = rangeStatics

    /** The `Selection` export: its statics build and lift selections. */
    val Selection: SelectionStatics
        get() = selectionStatics

    /** The `KeyMod` export: the modifier bits monaco expects in keybindings. */
    val KeyMod: KeyModStatics
        get() = keyModStatics

    /**
     * A new `Uri` for `value`, parsed exactly like `monaco.Uri.parse`.
     *
     * @throws Throwable when `value` is not a valid URI, as the upstream parser does.
     */
    fun uri(value: String): Uri = Uri.parse(value)

    /**
     * A new `Position` at `lineNumber`/`column` (both start at 1), built the way monaco builds its
     * own: the package exports `Position` as a value, so the wrapper reaches its constructor through
     * `Reflect.construct`.
     */
    fun position(lineNumber: Double, column: Double): Position =
        construct(Position as JsAny, lineNumber, column)

    /**
     * A new `Range` spanning `startLineNumber`/`startColumn` to `endLineNumber`/`endColumn`.
     *
     * A range whose start is after its end is normalised by monaco, not rejected.
     */
    fun range(startLineNumber: Double, startColumn: Double, endLineNumber: Double, endColumn: Double): Range =
        construct(Range as JsAny, startLineNumber, startColumn, endLineNumber, endColumn)

    /**
     * A new `Selection` whose anchor is `selectionStartLineNumber`/`selectionStartColumn` and whose
     * active end is `positionLineNumber`/`positionColumn`.
     *
     * The anchor/active pair is what makes a selection directional: see [Selection.getDirection].
     */
    fun selection(
        selectionStartLineNumber: Double,
        selectionStartColumn: Double,
        positionLineNumber: Double,
        positionColumn: Double,
    ): Selection =
        construct(Selection as JsAny, selectionStartLineNumber, selectionStartColumn, positionLineNumber, positionColumn)
}

/**
 * `Reflect.construct`, used to call the upstream classes' constructors.
 *
 * The alternative is an `external class` per value type, but `@JsModule` on a class imports the
 * module's default export — monaco has none — and a bare external class would compile to a lookup of
 * a global that no consumer bundle defines. `Reflect` is a language global, so this works in every
 * bundler without extra configuration.
 */
internal external interface JsReflect {
    fun construct(target: JsAny, arguments: Array<JsAny?>): JsAny
}

/** The global `Reflect` object. */
@JsName("Reflect")
internal external val jsReflectGlobal: JsReflect

/** `Reflect.construct(target, arguments)` for a class the module exports as a value. */
private fun <T> construct(target: JsAny, vararg arguments: JsAny?): T =
    jsReflectGlobal.construct(target, arguments as Array<JsAny?>).unsafeCast<T>()

/**
 * The one property of `window` the wrapper reads: the global `MonacoEnvironment` hook.
 */
internal external interface MonacoEnvironmentWindow {
    var MonacoEnvironment: Environment?
}

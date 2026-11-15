@file:JsModule("monaco-editor")

package io.github.shijianjs.ktjs.monaco

import io.github.shijianjs.ktjs.monaco.editor.EditorNamespace
import io.github.shijianjs.ktjs.monaco.languages.LanguagesNamespace
import kotlin.js.JsName

/**
 * The `editor` named export of `monaco-editor`.
 *
 * The package is ESM: `import { editor } from "monaco-editor"`. A `@JsModule` file may only hold
 * external declarations, so the public accessors live in [Monaco].
 */
@JsName("editor")
internal external val monacoEditorModule: EditorNamespace

/**
 * The `languages` named export of `monaco-editor`.
 *
 * Upstream: `export { languages }`.
 */
@JsName("languages")
internal external val monacoLanguagesModule: LanguagesNamespace

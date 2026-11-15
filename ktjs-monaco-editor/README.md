# ktjs-monaco-editor

Kotlin/JS wrapper for [`monaco-editor`](https://www.npmjs.com/package/monaco-editor) `0.57.0`. One npm
package, one Gradle module, one Kotlin package, and one entry point: `Monaco`.

## Installation

```kotlin
dependencies {
    implementation("io.github.shijianjs:ktjs-monaco-editor:0.57.0-kt.1")
}
```

## Usage

```kotlin
import io.github.shijianjs.ktjs.monaco.Monaco
import io.github.shijianjs.ktjs.monaco.editor.IStandaloneEditorConstructionOptions
import js.objects.unsafeJso

val editor = Monaco.editor.create(
    host,
    unsafeJso<IStandaloneEditorConstructionOptions> {
        value = "fun main() = println(\"Hello\")"
        language = "kotlin"
        theme = "vs-dark"
        automaticLayout = true
        minimap = unsafeJso { enabled = false }
    },
)

editor.getModel()?.let { Monaco.editor.setModelLanguage(it, "javascript") }
Monaco.editor.setTheme("vs")
```

Construction options and every other object literal come from `js.objects.unsafeJso` — the wrapper
declares the shape, the caller fills in only what it needs.

`Monaco` is the whole surface: `Monaco.editor` and `Monaco.languages` are the upstream namespaces,
`Monaco.environment` is the editor environment (where a worker URL goes), and `Monaco.uri/position/
range/selection` build the module's value classes, which themselves hang off `Monaco.Uri`,
`Monaco.Position`, `Monaco.Range`, `Monaco.Selection` and `Monaco.KeyMod`.

## What is in the package

| Package | Contents |
| --- | --- |
| `io.github.shijianjs.ktjs.monaco` | `Monaco`, the module entry point, `MarkerSeverity` and friends |
| `…monaco.root` | `Uri`, `Position`, `Range`, `Selection`, `KeyCode`, `KeyMod`, `Token`, … |
| `…monaco.editor` | the `editor` namespace: models, editors, themes, markers, decorations, options |
| `…monaco.languages` | the `languages` namespace: registration, Monarch, providers |
| `…monaco.worker` | worker message types (types only — the module exports no `worker` value) |

Files are generated from `editor.api.d.ts` by `scripts/generate-api.py` and carry a header saying so;
edit the generator, not the output. Coverage and the reasons behind each shape are in
[`docs/development/`](../docs/development/coverage.md).

## Stylesheet

monaco imports its CSS and icon fonts as ESM. Your bundler has to handle both, or the editor renders
unstyled:

```js
// webpack.config.d/monaco-css.js
config.module.rules.push(
  { test: /\.css$/, use: ["style-loader", "css-loader"], include: /node_modules[\\/]monaco-editor[\\/]/ },
  { test: /\.(woff2?|ttf|eot|svg)$/, type: "asset/resource" },
);
```

## Build and test

```bash
./gradlew :ktjs-monaco-editor:jsBrowserTest    # Karma, headless Chrome
```

After adding or upgrading an npm dependency, refresh the shared lock file:

```bash
./gradlew kotlinUpgradePackageLock
```

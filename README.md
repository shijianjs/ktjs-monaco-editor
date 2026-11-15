# ktjs-monaco-editor

<p align="center"><img src="docs/src/jsMain/resources/static/img/ktjs-monaco-editor-logo.png" width="120" alt="ktjs-monaco-editor"></p>

Kotlin/JS wrapper for [`monaco-editor`](https://www.npmjs.com/package/monaco-editor) `0.57.0` — the editor
that powers VS Code — with a bilingual demo, a bilingual documentation site, and a Chromium test suite.

[English](README.md) | [简体中文](README.zh-CN.md)

**[Documentation](https://shijianjs.github.io/ktjs-monaco-editor/)** ·
**[Live demo](https://shijianjs.github.io/ktjs-monaco-editor/demo/)** ·
**[API reference](https://shijianjs.github.io/ktjs-monaco-editor/api/)**

## Usage

```kotlin
dependencies {
    implementation("io.github.shijianjs:ktjs-monaco-editor:0.57.0-kt.1")
}
```

```kotlin
val editor = Monaco.editor.create(
    host,
    unsafeJso<IStandaloneEditorConstructionOptions> {
        value = "fun main() = println(\"Hello\")"
        language = "kotlin"
        theme = "vs-dark"
        automaticLayout = true
    },
)

editor.getModel()?.let { Monaco.editor.setModelLanguage(it, "javascript") }
Monaco.editor.setTheme("vs")
```

The editor needs its stylesheet, so a consumer's bundler must handle monaco's CSS imports and its icon
fonts. The two webpack rules are in [Getting started](https://shijianjs.github.io/ktjs-monaco-editor/guide/getting-started)
and, as a working example, in this repository's `demo/webpack.config.d/monaco-css.js`. A language that
runs in a worker also needs the worker chunk — `demo/webpack.config.d/monaco-worker.js` shows how, and
`Monaco.environment` is where the URL is pointed at it.

## What is wrapped

Every declaration in `esm/vs/editor/editor.api.d.ts`, generated rather than transcribed:

| Scope | Contents |
| --- | --- |
| `root` | `Uri`, `Position`, `Range`, `Selection`, `KeyCode`, `KeyMod`, `Token`, `MarkerSeverity`, … |
| `editor` | `create`, `createModel`, `createDiffEditor`, themes, markers, decorations, commands, languages, … |
| `languages` | `register`, `setMonarchTokensProvider`, completion / hover / definition providers, … |
| `worker` | the worker message types |

`scripts/generate-api.py` reads the declaration file and writes the Kotlin; the two hand-written files
hold what a generator cannot express. Status per declaration, including the five deliberate exclusions
and their reasons: [`docs/development/coverage.md`](docs/development/coverage.md). Why the declarations
look the way they do — root classes bound by named imports, optional methods as function properties,
CSS left to the consumer — is in
[`docs/development/technical-decisions.md`](docs/development/technical-decisions.md).

## Modules

| Module | npm package | Wrapper version |
| --- | --- | --- |
| [ktjs-monaco-editor](ktjs-monaco-editor/README.md) | `monaco-editor` | `0.57.0-kt.1` |

- `ktjs-monaco-editor` is the published module; `demo` and `docs` are published nowhere and are not
  part of the Dokka output.
- `demo` is served at `/demo/` on the Pages site; `docs` is the site itself, with the Dokka output at
  `/api/`.

## Versioning

Each module is versioned as `{upstream npm version}-kt.{revision}`. The upstream npm version is declared
once in the root [`package.json`](package.json); the revision is read per module from
[`wrapper-revisions.properties`](wrapper-revisions.properties). Gradle derives the base version and the
`npm(...)` dependency from these two files:

- Only `-kt.N` versions are published; a version without the suffix never exists.
- A working tree always adds `-SNAPSHOT`: the derived number is the *next* release, which Central does
  not have yet, so development builds never impersonate it. Publishing passes `-Pktjs.release=true`,
  which drops the suffix.
- Upstream upgrade: change the version in `package.json`; the base version follows automatically while
  the revision stays, even when the wrapper code itself did not change.
- Wrapper-only fix: the revision increments (`kt.2`, `kt.3`, …) while the base version stays.

## Requirements

- JDK 21 or newer (the build uses Gradle 9.4.1).
- Node and npm are downloaded automatically by the Kotlin Gradle plugin; `kotlin.js.yarn=false` in
  `gradle.properties` keeps npm as the package manager.
- A Chrome or Chromium binary for the wrapper's browser tests (Karma runs them headless), and one for the
  demo's end-to-end suite, which `./gradlew :demo:installPlaywrightBrowsers` downloads.

## Build and test

```bash
./gradlew build                       # compile, Detekt, wrapper tests
./gradlew :ktjs-monaco-editor:jsBrowserTest

# End-to-end checks for the demo page (Playwright, against the bundled output)
./gradlew :demo:installPlaywrightBrowsers
./gradlew :demo:e2eTest

# Documentation site: Kotlin configuration, Markdown pages, English and Chinese
./gradlew :docs:docusaurusBuild

# GitHub Pages site (documentation at the root, demo under /demo/, Dokka output under /api/)
./gradlew pagesSite
```

After adding or upgrading an npm dependency, refresh the shared lock file:

```bash
./gradlew kotlinUpgradePackageLock
```

`docs` is a Kotlin/JS project too. All three ktjs-docusaurus versions live in `gradle.properties` —
the wrapper klib, the Gradle plugin and the kit whose home sections the landing page uses — because
each of those modules carries its own revision. A SNAPSHOT there is resolved from `mavenLocal`. The
kit's browser bundle is not a Maven artifact: it ships as a resource of the kit's klib, and
`extractKitBundle` copies it to the site's `static/ktjs-client/`.

## Publishing

Publishing is configured once in the root [`build.gradle.kts`](build.gradle.kts). Releases run through
`.github/workflows/release.yml`: it signs the artifacts, packages one archive per Maven coordinate and
registers them with the shared Maven Central hub, which merges the pending registrations into a single
deployment. The wrapper is not published yet — see [`TODO.md`](TODO.md).

## License

Released under the [MIT License](LICENSE).

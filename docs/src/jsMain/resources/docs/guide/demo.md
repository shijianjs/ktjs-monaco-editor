---
sidebar_position: 4
title: Demo
---

# Demo

The live page is at **[https://shijianjs.github.io/ktjs-monaco-editor/demo/](https://shijianjs.github.io/ktjs-monaco-editor/demo/)**.
It is this repository's `demo` module: a Kotlin/JS page that creates a real editor through the
wrapper and exposes a few of the wrapper's calls as buttons.

## What the page shows

The editor contains a small Kotlin file, highlighted by monaco's built-in Monarch grammar — no
language service worker is involved, so the page works in any bundler setup that follows
[Getting started](getting-started.md#workers).

Each button runs one wrapper call and prints its result and the Kotlin source of the call that ran:

| Button | Call | What it demonstrates |
| --- | --- | --- |
| Read the text | `editor.getValue()` | the model behind the editor |
| Replace the text | `editor.setValue(…)` | writing through the model |
| Insert a line | `editor.executeEdits("demo", arrayOf(unsafeJso<IIdentifiedSingleEditOperation> { … }))` | an edit operation, with a `Range` built by `Monaco.range` |
| Highlight line 3 | `editor.deltaDecorations(previous, arrayOf(unsafeJso<IModelDeltaDecoration> { … }))` | decorations and their ids |
| Add a warning marker | `Monaco.editor.setModelMarkers(model, "ktjs-demo", arrayOf(unsafeJso<IMarkerData> { … }))` | a marker with `MarkerSeveritys.Warning`, read back with `getModelMarkers` |
| Show the selection | `editor.getSelection()` | a `Selection` and its anchor/active pair |
| Show the cursor | `editor.getPosition()` | a `Position` |
| Next theme | `Monaco.editor.setTheme(…)` | switching between `BuiltinThemes` |
| Next language | `Monaco.editor.setModelLanguage(model, …)` | re-tokenising an existing model |

The language switch in the top-right corner relabels every control — English and Chinese — while
keeping the editor instance, its model and the cursor. It is the demo's own localisation, not the
wrapper's: strings are plain Kotlin objects implementing one `Strings` interface, so a missing
translation is a compile error.

## Running it locally

```bash
./gradlew :demo:jsBrowserDevelopmentRun   # dev server with hot reload on http://localhost:8080
./gradlew :demo:jsBrowserDistribution     # production bundle under demo/build/dist/js/productionExecutable
```

The dev server and the production bundle both read `demo/webpack.config.d/`, which is where the two
monaco rules live: the stylesheet rule and the worker entry. Without them the page renders an
unstyled editor and logs the worker error described in
[Getting started](getting-started.md#bundler).

## The end-to-end suite

```bash
./gradlew :demo:e2eTest
```

The suite is Playwright against the production bundle, so it checks what Pages serves: the editor
renders, each wrapper call answers, the language switch relabels the page without rebuilding the
editor, and the page logs no console errors and issues no failed requests. The last case is what
catches a missing CSS rule or a worker chunk that was not emitted.

`./gradlew :demo:installPlaywrightBrowsers` downloads the Chromium the suite drives; CI runs it
before the suite.

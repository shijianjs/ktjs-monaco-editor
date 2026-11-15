// monaco-editor 0.57 loads its language service worker through
// `new URL('.../editor.worker.js?esm', import.meta.url)`, a Vite-style pattern webpack cannot resolve
// — the build then fails at runtime with "Failed to fetch dynamically imported module".
//
// So the worker is emitted as its own entry chunk instead, and the demo points
// `MonacoEnvironment.getWorkerUrl` at it (`demo/src/jsMain/kotlin/demo/Main.kt`). monaco creates the
// worker itself, with `{type: "module"}`, which is why the chunk is a plain bundled script.
//
// Keep this file identical wherever a bundle pulls monaco in; see
// docs/development/technical-decisions.md.
//
// The specifier goes through the package's `exports` map (`"./*": "./esm/vs/*.js"`), so it is written
// relative to that root — `monaco-editor/editor/editor.worker.js` — and not with the `esm/vs/`
// prefix the file has on disk, which would resolve to `esm/vs/esm/vs/...`.
config.entry["editorWorker"] = {
    import: require.resolve("monaco-editor/editor/editor.worker.js"),
    // The filename function in the generated config prefixes every non-`main` chunk with the module
    // name, so the entry names its own file.
    filename: "editor.worker.js",
}

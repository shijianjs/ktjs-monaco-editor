# Technical decisions

The choices that shaped the wrapper, in the order they had to be made. Each one has the reason it
was made and, where it matters, the alternative that was rejected.

## 1. The declarations are generated, not hand-written

`editor.api.d.ts` is 8370 lines and covers 455 declarations. Writing them by hand would be a
transcription exercise with no way to tell drift from intent, and an upstream release would mean
re-reading the whole file.

`scripts/generate-api.py` parses the declaration file and emits the Kotlin. It is a
character-level parser rather than a TypeScript AST: the file is regular enough (declarations,
members, one level of braces) that a full parser would be more machinery than value, and the
parser's own failures are loud (`Unsupported`, with the offending text).

Consequence: **generated files are never edited**. Each carries a header saying so. `Monaco.kt` and
`MonacoModule.kt` are the only hand-written sources, and they hold exactly what the generator
cannot express (see 3).

## 2. One Kotlin package per upstream namespace

`root`, `editor`, `languages` and `worker` each get a Kotlin package, and each is split by
declaration kind (`…Types`, `…Classes`, `…Enums`, `…Unions`, `…Literals`, `…Namespace`).

A single 8000-line file would be unreadable and would make every edit invalidate the whole file's
compile. Per-kind files also give a reader a rule: if you are looking for the type of a value, it
is in `…Types`; if you are looking for a constant object, it is in `…Enums` or `…Literals`.

## 3. Root-level classes: interface + statics, bound by named imports

`Uri`, `Position`, `Range`, `Selection` and `KeyMod` are exported by the module as **values**: they
have static members and constructors. The obvious modelling — `@JsModule("monaco-editor") external
class Uri` — compiles, but Kotlin's `@JsModule` on a class generates a **default import**
(`import Uri from "monaco-editor"`). The package has no default export, so the generated code fails
at runtime with `Uri is not defined`.

Measured, not assumed: the compiled `.mjs` contained
`import io_github_…_Uri_10xdd9 from 'monaco-editor'`, and `@JsModule` + `@JsName` on the class
still produced a default import.

The model that works:

- `external interface Uri` — instance members only;
- `external interface UriStatics` — the static members;
- `root/RootModule.kt`, `@file:JsModule("monaco-editor")` with one
  `@JsName("Uri") external val uriStatics: UriStatics` per class. `@JsModule` + `@JsName` on an
  **external val** is a *named* import, which is what the package actually offers.

Construction then needs a call to the real constructor. `Monaco.kt` reaches it through
`Reflect.construct`: `Reflect` is a language global, so no bundle configuration is involved, and it
is the only way to call `new` on a class the wrapper cannot import. `Monaco.uri/position/range/
selection` are the four factories; `KeyMod` has no instance members upstream, so it only gets statics.

## 4. Classes cannot extend classes in Kotlin/JS

`external class` types are final in Kotlin; `Selection extends Range` upstream cannot become a
Kotlin inheritance. Such a class therefore **flattens** the inherited members: the generator copies
them into the subclass and records the note in
[coverage](coverage.md#members-and-shapes-not-emitted). `Range.fromPositions` accepts a `Selection`
because both declare `IRange`.

The same shape applies to interfaces Kotlin can express: `Position : IPosition` is added explicitly
(`STRUCTURAL_SUPERTYPES`), because upstream relies on structural typing there — a `Position` is
accepted where `IPosition` is expected without an `implements` clause, and Kotlin needs the
inheritance spelled out for the same call to compile.

## 5. Subtypes keep the narrow type; the base declaration is dropped

`IGlobalEditorOptions` declares `theme?: string` and its subtypes re-declare it with
`BuiltinTheme`. Kotlin cannot narrow an inherited member's type, and emitting both is a
"hides member of supertype" error.

`compute_shadowed()` pre-scans the declaration graph: if a subtype re-declares a member, the **base**
declaration is dropped and the subtype keeps its precise one. That is why `ICodeEditor.getModel()`
returns `ITextModel?` and not a union of every editor's model, and why `theme` on a construction
options object is the literal union.

## 6. Optional methods become nullable function properties

`getWorkerUrl?(workerId, label): string` is an *optional member of a JS object*, not a callable
method: a consumer that provides it assigns a function, a consumer that does not leaves the slot
out. A Kotlin `fun` would force every object literal to carry a body, and `unsafeJso<Environment> {
getWorkerUrl = … }` would not compile.

So an optional method is emitted as `var getWorkerUrl: ((String, String) -> String)?`. Required
methods stay `fun`, so a wrong argument list is still a compile error. `toString?` is the one
exception: it stays a method, because Kotlin's `Any.toString` owns that name.

## 7. CSS is the consumer's bundler problem

monaco imports its stylesheets as ESM (`import "./editor.css"`) and its icons through `url(...)`.
webpack has no rule for either, so a bundle that pulls the package in renders an unstyled editor
unless it declares them.

The wrapper cannot fix this: which loader runs is consumer configuration, and a library that
injected its own rules would still not reach the consumer's build. So the repository keeps
`webpack.config.d/monaco-css.js` — identical in the wrapper module and in the demo — and the
documentation's [Getting started](../docs/guide/getting-started.md#bundler) hands the two rules to
consumers.

The Kotlin/JS webpack plugin reads `webpack.config.d` from the **module** directory
(`KotlinWebpack.configDirectory`), so each Gradle module that bundles monaco needs its own copy.
Playwright's test task, notably, does not pass a config directory at all — see 8.

## 8. Browser tests run under Karma, not Playwright

The wrapper's tests must exercise the real package in a real browser. Kotlin/JS offers two runners:

- `useKarma { useChromeHeadless() }` — the Karma test bundle reads the module's
  `webpack.config.d`, so monaco's CSS rules apply;
- `usePlaywright()` — the generated `WebpackBundleKotlinJsTests` config is built **without** a
  `configDirectory` (`KotlinKarma` passes one, `KotlinPlaywright` does not, sources of
  `kotlin-gradle-plugin` 2.4.21), so a Playwright-run test bundle cannot see `webpack.config.d` and
  fails on monaco's CSS imports.

Hence Karma + `useChromeHeadless()` for the module, and Playwright only for the demo's end-to-end
suite, which loads the production bundle over HTTP and needs no bundle of its own. That suite's
Chromium comes from `:demo:installPlaywrightBrowsers`, which the build registers itself because the
wrapper module no longer generates the Kotlin task.

## 9. The worker is an extra webpack entry

`monaco-editor` 0.57 starts its language service worker with
`new URL('…/editor.worker.js?esm', import.meta.url)` — a Vite-shaped pattern. webpack cannot resolve
it, and the page then logs `Failed to fetch dynamically imported module` while features that need
the worker silently do nothing.

The demo emits the worker as its own entry chunk (`monaco-editor/editor/editor.worker.js`, written
without the `esm/vs/` prefix because the package's `exports` map adds it) and points
`Monaco.environment.getWorkerUrl` at the file. monaco wraps the URL in
`new Worker(url, { type: "module" })`, so a bundled chunk is what it wants.

The tests avoid this entirely: they use `plaintext` and the built-in `kotlin` Monarch grammar, which
tokenise on the main thread. Exercising a language service is a separate piece of work (and would
need `json`/`typescript` workers as well).

## 10. Versions, CI and local mirrors

- Upstream npm versions live in the root `package.json`; the wrapper version is
  `{that version}-kt.{revision}`, per module, from `wrapper-revisions.properties`.
- CI runs on the official npm registry. A local Aliyun/npmmirror mirror is configured through
  `~/.gradle/init.d` and `~/.npmrc` on a developer machine and is **never** committed: the lock file
  keeps `registry.npmjs.org` URLs so CI and consumers resolve the same tarballs.
- `kotlin-js-store/package-lock.json` is regenerated with
  `./gradlew kotlinUpgradePackageLock` after an npm dependency changes; a lock that does not match
  the build fails `:kotlinStorePackageLock`.

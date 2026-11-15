# TODO

## ktjs-monaco-editor: wrap `monaco-editor` 0.57.0 for Kotlin/JS

Task goal: a published Kotlin/JS wrapper for the `monaco-editor` npm package, with a bilingual demo,
a bilingual documentation site that links to the demo and the API reference, GitHub Pages serving
all three, and CI green.

- Total: 460
- Done: 455
- Excluded: 5
- Pending: 0

The count is the upstream declarations in `editor.api.d.ts` — one entry per interface, class, enum,
type alias, function and constant. Detailed list, per-namespace breakdown and the exclusion reasons:
[coverage.md](docs/development/coverage.md).

### Current tasks

- [x] Declarations generated from `editor.api.d.ts` (`scripts/generate-api.py`)
- [x] Value classes bound to the module exports, with construction factories
- [x] Chromium test suite for the wrapper (Karma, 10 cases)
- [x] Demo page with a language switch, and its Playwright suite
- [x] Documentation site in English and Chinese, linking to the demo and `/api/`
- [x] GitHub Pages workflow
- [ ] Release `0.57.0-kt.1` to Maven Central (needs a manual run of `release.yml`; the wrapper is
      not published yet)

### Constraints worth remembering

- Generated files are never edited; re-run `scripts/generate-api.py` instead.
- The demo and the docs are the consumer's reference: anything a consumer must do (bundler rules,
  worker URL) is documented, not hidden inside the wrapper.
- A declaration counts as done only when the generator writes it **and** the module compiles; a
  browser case per declaration is not required, and coverage.md says so explicitly.
- No local npm mirror appears in the committed lock file.
- Decisions behind the shapes: [technical-decisions.md](docs/development/technical-decisions.md).

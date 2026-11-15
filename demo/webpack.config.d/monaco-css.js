// monaco-editor ships its stylesheets as ESM `import "./x.css"` statements, and its icons come from
// codicon.css `url(...)` references. webpack has no default rule for either, so every bundle that
// pulls the package in (this module's own test bundle, the demo, the docs) declares these two rules.
//
// The `include` matches the resource path (`node_modules/monaco-editor/...`) rather than a resolved
// directory, because the webpack `context` differs between those bundles.
//
// Keep this file identical in `ktjs-monaco-editor/`, `demo/` and `docs/`; see
// docs/development/technical-decisions.md for why the consumer's bundler has to handle CSS.
config.module.rules.push({
    test: /\.css$/,
    use: ["style-loader", "css-loader"],
    include: /node_modules[\\/]monaco-editor[\\/]/,
})

config.module.rules.push({
    test: /\.(woff2?|ttf|eot|svg)$/,
    type: "asset/resource",
})

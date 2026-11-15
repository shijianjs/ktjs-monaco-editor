---
sidebar_position: 2
title: 快速开始
---

# 快速开始

要让编辑器出现，需要三件事同时成立：依赖在 classpath 上、打包器能处理 monaco 的 CSS、项目告诉
monaco 它的 worker 在哪。第一件是 Gradle 的事，后两件取决于你用的打包器。

## 环境要求

- Kotlin/JS + `kotlin-browser`（封装的类型引用了 `web.html.*`、`web.workers.*`）。
- 打包器能产出 worker：Kotlin/JS 的 webpack 集成可以。
- 封装自身的构建用 JDK 21 就够。

## 依赖

```kotlin
dependencies {
    implementation("io.github.shijianjs:ktjs-monaco-editor:0.57.0-kt.1")
    // 由封装发布的 POM 带入；这里列出来是为了让版本可见。
    implementation(npm("monaco-editor", "0.57.0"))
    // monaco 样式表需要的两个 loader，见下面的「打包器」。
    devNpm("css-loader", "6.11.0")
    devNpm("style-loader", "4.0.0")
}
```

模块以 `ktjs-monaco-editor` 发布，版本号是 `{上游 npm 版本}-kt.{修订号}`。`npm("monaco-editor", …)`
依赖随 POM 一起传入，只有你自己要固定版本时才需要跟着改。

## 第一个编辑器

```kotlin
import io.github.shijianjs.ktjs.monaco.Monaco
import io.github.shijianjs.ktjs.monaco.editor.IStandaloneEditorConstructionOptions
import js.objects.unsafeJso
import web.dom.document
import web.html.HtmlTagName

fun main() {
    val host = document.createElement(HtmlTagName.div).apply {
        // monaco 会测量容器尺寸，所以要给它真实大小。
        style.width = "100%"
        style.height = "480px"
    }
    document.body.append(host)

    val editor = Monaco.editor.create(
        host,
        unsafeJso<IStandaloneEditorConstructionOptions> {
            value = "fun main() { println(\"Hello, Kotlin/JS\") }"
            language = "kotlin"
            automaticLayout = true
        },
    )
}
```

`Monaco.editor.create` 返回 `IStandaloneCodeEditor`：模型、光标、装饰和标记都挂在它上面。
`unsafeJso<...> { }` 是对象字面量构造器——每个属性都会对照上游接口检查，所以拼错的选项是编译错误，
而不是被静默忽略。

包的入口会注册全部内置语言，因此只写 `language = "kotlin"` 就能得到语法高亮。语言**服务**
（补全、诊断）另外需要 worker；编辑器本身和 Monarch 语法不需要。

## 打包器

`monaco-editor` 的样式表是以 ESM `import "./x.css"` 语句引入的，图标则是 `url(...)` 引用。
两者 webpack 默认都不处理，所以引入该包的项目要声明两条规则：

```javascript
// webpack.config.d/monaco-css.js —— Kotlin/JS 的 webpack 任务从模块目录读取它。
config.module.rules.push({
    test: /\.css$/,
    use: ["style-loader", "css-loader"],
    include: /node_modules[\\/]monaco-editor[\\/]/,
})
config.module.rules.push({
    test: /\.(woff2?|ttf|eot|svg)$/,
    type: "asset/resource",
})
```

本仓库把这份文件放在两处——封装模块（给它自己的 Karma 测试 bundle 用）和 demo（给页面 bundle 用）——
两份内容完全一致。封装没法替你完成这一步：用哪个 loader 是消费者自己的打包器配置。

## Worker

`monaco-editor` 0.57 用 `new URL('…/editor.worker.js?esm', import.meta.url)` 启动语言服务 worker，
这是 Vite 风格的写法，webpack 解析不了；项目不给答案的话，页面会报
`Failed to fetch dynamically imported module`，依赖 worker 的功能则静默失效。两步解决：

1. 把 worker 单独打成一个 bundle。webpack 里：

   ```javascript
   // webpack.config.d/monaco-worker.js
   config.entry["editorWorker"] = {
       import: require.resolve("monaco-editor/editor/editor.worker.js"),
       filename: "editor.worker.js",
   }
   ```

   这个 specifier 走包的 `exports` 映射（`"./*": "./esm/vs/*.js"`），所以要写去掉 `esm/vs/`
   前缀的路径，否则会解析成 `esm/vs/esm/vs/...`。

2. 在创建第一个编辑器之前指向它：

   ```kotlin
   Monaco.environment = unsafeJso<Environment> {
       getWorkerUrl = { _, _ -> "editor.worker.js" }
   }
   ```

   `getWorkerUrl` 会收到 worker id 和 label；内置语言共用 `editor.worker.js`。monaco 会把它包成
   `new Worker(url, { type: "module" })`，所以产物是一个普通打包脚本。如果你的打包器能为每种语言
   （`json`、`css`、`typescript`、`html`）各打一个 worker，就按 label 返回不同文件。

demo 就是这么做的；[演示](demo.md) 展示了它产出的页面。

## 验证构建

```bash
./gradlew :ktjs-monaco-editor:jsBrowserTest   # 封装自己的 Chromium 测试
./gradlew :demo:e2eTest                       # 演示页面，走 Playwright
```

封装自身的测试通过 Karma 在 Chromium 里跑，检查的是真实包而不是 mock：写错的 `@JsName`、
漏掉的成员、错的值类型都会在这里失败。demo 的测试跑在生产 bundle 上，只有在你打真实包时才会暴露
CSS、worker 这类打包问题。

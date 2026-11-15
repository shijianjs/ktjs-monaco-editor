# ktjs-monaco-editor

<p align="center"><img src="docs/src/jsMain/resources/static/img/ktjs-monaco-editor-logo.png" width="120" alt="ktjs-monaco-editor"></p>

[`monaco-editor`](https://www.npmjs.com/package/monaco-editor) `0.57.0`（VS Code 用的那个编辑器）的
Kotlin/JS wrapper，附带中英双语演示页、双语文档站和一套 Chromium 测试。

[English](README.md) | [简体中文](README.zh-CN.md)

**[文档](https://shijianjs.github.io/ktjs-monaco-editor/zh-Hans/)** ·
**[在线演示](https://shijianjs.github.io/ktjs-monaco-editor/demo/)** ·
**[API 文档](https://shijianjs.github.io/ktjs-monaco-editor/api/)**

## 用法

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

编辑器需要它自己的样式表，所以消费方的 bundler 必须处理 monaco 的 CSS import 与图标字体。那两条
webpack 规则写在[快速开始](https://shijianjs.github.io/ktjs-monaco-editor/zh-Hans/guide/getting-started)，
本仓库的 `demo/webpack.config.d/monaco-css.js` 是一份能跑的例子。跑在 worker 里的语言还需要 worker
chunk——`demo/webpack.config.d/monaco-worker.js` 演示怎么产出它，`Monaco.environment` 是把它指过去的
地方。

## 封装了什么

`esm/vs/editor/editor.api.d.ts` 里的每一条声明，全部由脚本生成而非手抄：

| 范围 | 内容 |
| --- | --- |
| `root` | `Uri`、`Position`、`Range`、`Selection`、`KeyCode`、`KeyMod`、`Token`、`MarkerSeverity` 等 |
| `editor` | `create`、`createModel`、`createDiffEditor`、主题、标记、装饰、命令、语言等 |
| `languages` | `register`、`setMonarchTokensProvider`、补全 / 悬停 / 定义 provider 等 |
| `worker` | worker 消息相关的类型 |

`scripts/generate-api.py` 读声明文件、写 Kotlin；两个手写文件只放生成器表达不了的部分。逐条状态
（含 5 条刻意排除及理由）见 [`docs/development/coverage.md`](docs/development/coverage.md)；声明为什么
长成这样——根级类用命名导入绑定、可选方法是函数属性、CSS 交给消费方的 bundler——见
[`docs/development/technical-decisions.md`](docs/development/technical-decisions.md)。

## 模块

| 模块 | npm 包 | wrapper 版本 |
| --- | --- | --- |
| [ktjs-monaco-editor](ktjs-monaco-editor/README.zh-CN.md) | `monaco-editor` | `0.57.0-kt.1` |

- `ktjs-monaco-editor` 是发布模块；`demo` 与 `docs` 不发布，也不进 Dokka。
- `demo` 挂在 Pages 的 `/demo/`；`docs` 就是站点本身，Dokka 产物在 `/api/`。

## 版本

每个模块独立版本，格式为 `{上游 npm 版本}-kt.{修订号}`。上游版本只在根 [`package.json`](package.json)
的 `dependencies` 里声明，修订号在 [`wrapper-revisions.properties`](wrapper-revisions.properties) 里按模块
维护；Gradle 从这两处推导基础版本与 `npm(...)` 依赖：

- 只发布带 `-kt.N` 的版本，不带后缀的版本不存在。
- 工作树里的版本一律再加 `-SNAPSHOT`：推导出的是「下一个要发的版本」，Central 上还没有，开发构建不冒用
  这个名字。发布时传 `-Pktjs.release=true` 去掉后缀。
- 上游升级：改 `package.json` 里的版本，基础版本自动跟随，修订号不变（即使 wrapper 代码没动）。
- 只改 wrapper：基础版本不变，修订号递增（`kt.2`、`kt.3`……）。

## 环境要求

- JDK 21 或更高（构建用 Gradle 9.4.1）。
- Node 与 npm 由 Kotlin Gradle 插件自动下载；`gradle.properties` 里的 `kotlin.js.yarn=false` 保留 npm
  作为包管理器。
- wrapper 的浏览器测试跑 Karma headless，需要一个 Chrome/Chromium；demo 的端到端测试同样需要，由
  `./gradlew :demo:installPlaywrightBrowsers` 下载。

## 构建与测试

```bash
./gradlew build                       # 编译、Detekt、wrapper 测试
./gradlew :ktjs-monaco-editor:jsBrowserTest

# demo 页的端到端检查（Playwright，针对打包产物）
./gradlew :demo:installPlaywrightBrowsers
./gradlew :demo:e2eTest

# 文档站：Kotlin 配置、Markdown 页面，中英双语
./gradlew :docs:docusaurusBuild

# GitHub Pages 站点（文档在根目录，demo 在 /demo/，Dokka 在 /api/）
./gradlew pagesSite
```

新增或升级 npm 依赖后刷新共享锁文件：

```bash
./gradlew kotlinUpgradePackageLock
```

`docs` 也是一个 Kotlin/JS 模块。三个 ktjs-docusaurus 版本都在 `gradle.properties` 里——wrapper klib、
Gradle 插件、落地页用到的工具箱——因为那几个模块各有自己的修订号；写 SNAPSHOT 时从 `mavenLocal` 解析。
工具箱的浏览器 bundle 不是 Maven 产物，而是 klib 里的资源，由 `extractKitBundle` 拷到站点的
`static/ktjs-client/`。

## 发布

发布配置统一在根 [`build.gradle.kts`](build.gradle.kts)。发布走 `.github/workflows/release.yml`：签名、
为每个 Maven 坐标打一个归档、把注册推给共享的 Maven Central hub，由 hub 合并成一次 deployment。目前还
没有发布过，见 [`TODO.md`](TODO.md)。

## 许可

[MIT License](LICENSE)。

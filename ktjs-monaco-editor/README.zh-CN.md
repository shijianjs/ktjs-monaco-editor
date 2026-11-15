# ktjs-monaco-editor

[`monaco-editor`](https://www.npmjs.com/package/monaco-editor) `0.57.0` 的 Kotlin/JS wrapper。一个 npm 包、
一个 Gradle 模块、一个 Kotlin 包，一个入口：`Monaco`。

## 安装

```kotlin
dependencies {
    implementation("io.github.shijianjs:ktjs-monaco-editor:0.57.0-kt.1")
}
```

## 用法

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

构造参数与其他对象字面量都走 `js.objects.unsafeJso`：wrapper 声明形状，调用方只写自己需要的字段。

`Monaco` 就是全部对外表面：`Monaco.editor` 与 `Monaco.languages` 是上游的两个命名空间，`Monaco.environment`
是编辑器环境（worker URL 挂在这里），`Monaco.uri/position/range/selection` 造模块导出的值类，值类
本身挂在 `Monaco.Uri`、`Monaco.Position`、`Monaco.Range`、`Monaco.Selection`、`Monaco.KeyMod` 上。

## 包里有什么

| 包 | 内容 |
| --- | --- |
| `io.github.shijianjs.ktjs.monaco` | `Monaco` 入口，`MarkerSeverity` 等常量对象 |
| `…monaco.root` | `Uri`、`Position`、`Range`、`Selection`、`KeyCode`、`KeyMod`、`Token` 等 |
| `…monaco.editor` | `editor` 命名空间：model、editor、主题、标记、装饰、选项 |
| `…monaco.languages` | `languages` 命名空间：注册、Monarch、各类 provider |
| `…monaco.worker` | worker 消息类型（只有类型——模块没有导出 `worker` 这个值） |

文件由 `scripts/generate-api.py` 从 `editor.api.d.ts` 生成，带说明头；要改就改生成器，别改产物。覆盖
状态与每种形状的理由在 [`docs/development/`](../docs/development/coverage.md)。

## 样式表

monaco 的 CSS 与图标字体是 ESM import。bundler 必须处理这两者，否则编辑器没有样式：

```js
// webpack.config.d/monaco-css.js
config.module.rules.push(
  { test: /\.css$/, use: ["style-loader", "css-loader"], include: /node_modules[\\/]monaco-editor[\\/]/ },
  { test: /\.(woff2?|ttf|eot|svg)$/, type: "asset/resource" },
);
```

## 构建与测试

```bash
./gradlew :ktjs-monaco-editor:jsBrowserTest    # Karma + headless Chrome
```

新增或升级 npm 依赖后，刷新共享锁文件：

```bash
./gradlew kotlinUpgradePackageLock
```

package demo.page.overview

import io.github.shijianjs.ktjs.demo.page.DemoPage
import io.github.shijianjs.ktjs.dom.webcomponent.defineIfMissing
import web.components.CustomElementReference
import web.dom.TagName
import demo.API_URL
import demo.DOCS_URL
import demo.SOURCE_URL
import io.github.shijianjs.ktjs.demo.i18n.Binder
import io.github.shijianjs.ktjs.demo.i18n.Lang
import io.github.shijianjs.ktjs.demo.i18n.Langs
import io.github.shijianjs.ktjs.demo.i18n.Translations
import io.github.shijianjs.ktjs.demo.i18n.texts
import io.github.shijianjs.ktjs.demo.page.anchor
import io.github.shijianjs.ktjs.demo.page.codeText
import io.github.shijianjs.ktjs.demo.page.paragraph
import io.github.shijianjs.ktjs.demo.page.row
import io.github.shijianjs.ktjs.demo.page.sectionBlock
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.element.unaryPlus
import web.html.HtmlTagName

/** Every string this page shows, in one language. */
private class OverviewTexts(
    val title: String,
    val summary: String,
    val docsLink: String,
    val sourceLink: String,
    val apiLink: String,
    val purposeHeading: String,
    val body: String,
    val bullets: List<String>,
    val workerHeading: String,
    val workerBody: String,
    val nextHeading: String,
    val nextBody: String,
)

private val overviewEnglish: OverviewTexts = OverviewTexts(
    title = "Overview",
    summary = "A Kotlin/JS wrapper for monaco-editor: typed editor, model and decoration " +
        "declarations, and one place to point monaco at its worker.",
    docsLink = "Documentation",
    sourceLink = "Source repository",
    apiLink = "API reference",
    purposeHeading = "What it wraps",
    body = "monaco's own types name every option and every edit operation, and in JavaScript none of " +
        "them are checked. The wrapper declares them: `Monaco.editor.create` takes a typed " +
        "construction object, decorations are `IModelDeltaDecoration`, markers are `IMarkerData`, " +
        "and a misspelled option is a compile error.",
    bullets = listOf(
        "Monaco.editor — create, setModelLanguage, setTheme, deltaDecorations, setModelMarkers.",
        "Monaco.editor.create(host, options) — one editor over one host element.",
        "Monaco.environment — where the worker script lives, set before the first editor.",
    ),
    workerHeading = "The worker",
    workerBody = "monaco runs its language services in a worker. The wrapper does not bundle one: a " +
        "consumer emits the worker with their bundler and hands monaco its URL through " +
        "`Monaco.environment`. This demo does exactly that — the same three lines work in any " +
        "webpack or Vite project.",
    nextHeading = "Reading order",
    nextBody = "The overview, then the playground: one editor on the left, one wrapper call per " +
        "button on the right, and the Kotlin that ran underneath.",
)

private val overviewChinese: OverviewTexts = OverviewTexts(
    title = "概览",
    summary = "monaco-editor 的 Kotlin/JS 封装：编辑器、模型、装饰与标记都有类型，worker 的地址也只有一处要配。",
    docsLink = "使用文档",
    sourceLink = "源码仓库",
    apiLink = "API 文档",
    purposeHeading = "它封装了什么",
    body = "monaco 自己的类型给每个选项、每个编辑操作都起了名字，在 JavaScript 里这些名字一个都不检查。封装把" +
        "它们声明出来：`Monaco.editor.create` 收的是有类型的构造对象，装饰是 `IModelDeltaDecoration`，" +
        "标记是 `IMarkerData`，选项拼错就是编译错误。",
    bullets = listOf(
        "Monaco.editor —— create、setModelLanguage、setTheme、deltaDecorations、setModelMarkers。",
        "Monaco.editor.create(host, options) —— 一个宿主元素上建一个编辑器。",
        "Monaco.environment —— worker 脚本在哪里，第一个编辑器之前就要设好。",
    ),
    workerHeading = "worker 怎么来",
    workerBody = "monaco 的语言服务跑在 worker 里。封装本身不打这个包：使用方用自己的打包器产出 worker，" +
        "把地址交给 `Monaco.environment`。这个 demo 就是这么做的——同样的三行在任何 webpack 或 Vite 项目里" +
        "都能照搬。",
    nextHeading = "建议顺序",
    nextBody = "先看概览，再看演练场：左边一个编辑器，右边每个按钮一次封装调用，下面是被执行的那段 Kotlin。",
)

private val overviewTexts: Translations<OverviewTexts> = texts(
    Langs.EN to overviewEnglish,
    Langs.ZH_HANS to overviewChinese,
)

/**
 * Landing page: what the library wraps, how it is consumed, where the reference lives.
 *
 * The sections are plain blocks, not components: they have nothing to swap at run time, so a
 * component per section would only add indirection.
 */
internal class OverviewPage : DemoPage() {

    private val texts = Binder(overviewTexts)


    init {
        addLink(texts.bind(anchor("", DOCS_URL, external = true), { it.docsLink }))
        addLink(texts.bind(anchor("", API_URL, external = true), { it.apiLink }))
        addLink(texts.bind(anchor("", SOURCE_URL, external = true), { it.sourceLink }))

        val purpose = sectionBlock()
        texts.bind(purpose.title, { it.purposeHeading })
        val bullets = HtmlTagName.ul {
            List(3) { index -> texts.bind(+HtmlTagName.li()) { it.bullets.getOrNull(index).orEmpty() } }
        }
        purpose.body.append(texts.bind(paragraph(), { it.body }), bullets)

        val worker = sectionBlock()
        texts.bind(worker.title, { it.workerHeading })
        worker.body.append(
            texts.bind(paragraph(), { it.workerBody }),
            row(codeText("Monaco.environment = unsafeJso<Environment> { getWorkerUrl = { _, _ -> \"editor.worker.js\" } }")),
        )

        val next = sectionBlock()
        texts.bind(next.title, { it.nextHeading })
        next.body.append(texts.bind(paragraph(), { it.nextBody }))

        body.append(purpose.root, worker.root, next.root)
    }

    override fun applyTranslation(lang: Lang) {
        val current = overviewTexts[lang]
        setTitle(current.title)
        setSummary(current.summary)
        texts.apply(lang)
    }

    companion object : CustomElementReference<OverviewPage>(OverviewPage::class.js) {
        val NAME: TagName<OverviewPage> = TagName("monaco-overview-page")

        fun define() {
            defineIfMissing(NAME, this)
        }
    }
}

/** Creates a defined `<monaco-overview-page>`. */
internal fun newOverviewPage(): OverviewPage {
    OverviewPage.define()
    return OverviewPage.NAME()
}

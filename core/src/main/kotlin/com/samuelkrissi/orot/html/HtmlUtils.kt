package com.samuelkrissi.orot.html

private val NAMED_ENTITIES = mapOf(
    "amp" to "&",
    "quot" to "\"",
    "apos" to "'",
    "lt" to "<",
    "gt" to ">",
    "nbsp" to " ",
    "ndash" to "–",
    "mdash" to "—",
    "laquo" to "«",
    "raquo" to "»",
    "hellip" to "…",
    "rsquo" to "’",
    "lsquo" to "‘",
    "rdquo" to "”",
    "ldquo" to "“",
)

private val ENTITY_REGEX = Regex("""&(#x[0-9a-fA-F]+|#\d+|[a-zA-Z]+);""")
private val TAG_REGEX = Regex("""</?([a-zA-Z0-9]+)([^>]*)>""", RegexOption.IGNORE_CASE)
private val SCRIPT_STYLE_REGEX = Regex(
    """<(script|style|iframe|object|embed)[^>]*>[\s\S]*?</\1>""",
    setOf(RegexOption.IGNORE_CASE),
)
private val ALLOWED_TAGS = setOf(
    "p", "br", "div", "span", "strong", "b", "em", "i", "u",
    "blockquote", "ul", "ol", "li", "h1", "h2", "h3", "h4",
    "sup", "sub", "a",
)

fun decodeHtmlEntities(text: String): String {
    return ENTITY_REGEX.replace(text) { match ->
        val body = match.groupValues[1]
        when {
            body.startsWith("#x") || body.startsWith("#X") ->
                body.drop(2).toIntOrNull(16)?.toChar()?.toString() ?: match.value
            body.startsWith("#") ->
                body.drop(1).toIntOrNull()?.toChar()?.toString() ?: match.value
            else -> NAMED_ENTITIES[body] ?: match.value
        }
    }
}

fun stripHtmlTags(html: String): String {
    val withoutBlocks = SCRIPT_STYLE_REGEX.replace(html, " ")
    val withoutTags = TAG_REGEX.replace(withoutBlocks, " ")
    return decodeHtmlEntities(withoutTags)
        .replace(Regex("""\s+"""), " ")
        .trim()
}

fun sanitizeHtml(html: String): String {
    val withoutBlocks = SCRIPT_STYLE_REGEX.replace(html, "")
    return TAG_REGEX.replace(withoutBlocks) { match ->
        val raw = match.value
        val name = match.groupValues[1].lowercase()
        if (name !in ALLOWED_TAGS) return@replace ""
        if (raw.startsWith("</")) return@replace "</$name>"
        if (name == "br") return@replace "<br>"
        if (name == "a") {
            val href = Regex("""href\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                .find(raw)
                ?.groupValues
                ?.get(1)
                .orEmpty()
            val safeHref = if (href.startsWith("#") || href.startsWith("https://yhb.org.il")) href else "#"
            return@replace """<a href="$safeHref">"""
        }
        "<$name>"
    }
}

fun wrapReaderDocument(
    title: String,
    bodyHtml: String,
    fontSizePx: Int,
    dark: Boolean,
): String {
    val bg = if (dark) "#1c1914" else "#f7f1e4"
    val text = if (dark) "#f3ead7" else "#2b2116"
    val quote = if (dark) "#e8c27a" else "#6b3e1d"
    val muted = if (dark) "#c9b89a" else "#6f5b45"
    val safeTitle = decodeHtmlEntities(title)
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
    val safeBody = sanitizeHtml(bodyHtml)
    return """
        <!DOCTYPE html>
        <html dir="rtl" lang="he">
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <style>
            html, body {
              margin: 0;
              padding: 0;
              background: $bg;
              color: $text;
            }
            body {
              font-family: "Noto Serif Hebrew", "Noto Sans Hebrew", "David", "Times New Roman", serif;
              font-size: ${fontSizePx}px;
              line-height: 1.85;
              padding: 8px 18px 48px;
              text-align: justify;
            }
            h1 {
              font-size: 1.25em;
              line-height: 1.45;
              margin: 8px 0 20px;
              color: $quote;
              text-align: right;
            }
            p { margin: 0 0 1em; }
            a { color: $quote; text-decoration: none; }
            .orot.quote strong, strong { color: $quote; font-weight: 700; }
            .orot.parentheses { color: $muted; font-size: 0.92em; }
            sup { font-size: 0.75em; }
          </style>
        </head>
        <body>
          <h1>$safeTitle</h1>
          $safeBody
        </body>
        </html>
    """.trimIndent()
}

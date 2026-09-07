package com.samuelkrissi.orot.html

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HtmlUtilsTest {
    @Test
    fun decodeNumericAndNamedEntities() {
        val raw = "א &#8211; מַהוּת &quot;כנסת&quot;"
        assertEquals("א – מַהוּת \"כנסת\"", decodeHtmlEntities(raw))
    }

    @Test
    fun stripTagsKeepsText() {
        val html = "<p>שלום <strong>עולם</strong></p>"
        assertEquals("שלום עולם", stripHtmlTags(html))
    }

    @Test
    fun sanitizeDropsScriptsAndKeepsParagraphs() {
        val html = """<p>טקסט</p><script>alert(1)</script><iframe src="x"></iframe>"""
        val clean = sanitizeHtml(html)
        assertTrue(clean.contains("<p>טקסט</p>"))
        assertFalse(clean.contains("script"))
        assertFalse(clean.contains("iframe"))
    }

    @Test
    fun readerDocumentIsRtlHebrew() {
        val doc = wrapReaderDocument("כותרת", "<p>תוכן</p>", 20, dark = false)
        assertTrue(doc.contains("dir=\"rtl\""))
        assertTrue(doc.contains("lang=\"he\""))
        assertTrue(doc.contains("<p>תוכן</p>"))
        assertTrue(doc.contains("כותרת"))
    }
}

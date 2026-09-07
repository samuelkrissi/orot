package com.samuelkrissi.orot.catalog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LessonIndexTest {
    @Test
    fun parseStandardSlug() {
        val slug = parseLessonSlug("01-08-12")
        assertNotNull(slug)
        assertEquals("01", slug.bookCode)
        assertEquals("08", slug.chapterCode)
        assertEquals("12", slug.paragraphCode)
        assertEquals("01-08-12", slug.key)
    }

    @Test
    fun parsePrefixedAndShortSlugs() {
        val prefixed = parseLessonSlug("b-orot-07-01-2")
        assertNotNull(prefixed)
        assertEquals("07", prefixed.bookCode)
        assertEquals("01", prefixed.chapterCode)
        assertEquals("2", prefixed.paragraphCode)

        val short = parseLessonSlug("b-orot-08-03")
        assertNotNull(short)
        assertEquals("08", short.bookCode)
        assertEquals("03", short.chapterCode)
        assertEquals("01", short.paragraphCode)
    }

    @Test
    fun rejectNonNumericBook() {
        assertNull(parseLessonSlug("intro-01-01"))
    }

    @Test
    fun matchBookFromSlug() {
        val book = Books.byId("b-orot-04")
        assertNotNull(book)
        assertEquals(true, book.matchesSlug("b-orot-04-01-03"))
        assertEquals(false, book.matchesSlug("01-01-01"))
    }

    @Test
    fun extractChapterTitle() {
        val title = "א – מַהוּת כְּנֶסֶת־יִשְׂרָאֵל, פסקה א"
        assertEquals("א – מַהוּת כְּנֶסֶת־יִשְׂרָאֵל", chapterTitleFromLesson(title))
    }

    @Test
    fun groupLessonsByChapter() {
        val lessons = sortLessons(
            listOf(
                LessonRef(3, "01-02-02", "ב – פרק שני, פסקה ב"),
                LessonRef(1, "01-01-01", "א – פרק ראשון, פסקה א"),
                LessonRef(2, "01-02-01", "ב – פרק שני, פסקה א"),
            ),
        )
        val chapters = groupIntoChapters(lessons)
        assertEquals(2, chapters.size)
        assertEquals("01", chapters[0].code)
        assertEquals("א – פרק ראשון", chapters[0].title)
        assertEquals(1, chapters[0].lessonCount)
        assertEquals("02", chapters[1].code)
        assertEquals(2, chapters[1].lessonCount)
        assertEquals("01-02-01", chapters[1].lessons[0].slug)
    }

    @Test
    fun catalogContainsNineSiteBooks() {
        assertEquals(9, Books.all.size)
        assertEquals("אורות ישראל", Books.all.first().titleHe)
        assertEquals("עקבי הצאן", Books.all.last().titleHe)
        assertEquals(2872, Books.byId("b-orot-01")?.genreId)
    }
}

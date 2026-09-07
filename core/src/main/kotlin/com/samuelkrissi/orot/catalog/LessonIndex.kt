package com.samuelkrissi.orot.catalog

data class LessonSlug(
    val bookCode: String,
    val chapterCode: String,
    val paragraphCode: String,
) {
    val key: String get() = "$bookCode-$chapterCode-$paragraphCode"
}

data class LessonRef(
    val id: Long,
    val slug: String,
    val title: String,
    val excerpt: String = "",
    val link: String = "",
) {
    val parsedSlug: LessonSlug? get() = parseLessonSlug(slug)

    val chapterTitle: String get() = chapterTitleFromLesson(title)

    val paragraphLabel: String get() {
        val after = title.substringAfter("פסקה", missingDelimiterValue = "")
            .ifBlank { title.substringAfter("פס׳", missingDelimiterValue = "") }
            .trim()
        return if (after.isNotBlank()) "פסקה $after" else title
    }
}

data class Chapter(
    val code: String,
    val title: String,
    val lessons: List<LessonRef>,
) {
    val lessonCount: Int get() = lessons.size
}

fun parseLessonSlug(slug: String): LessonSlug? {
    val cleaned = slug.trim().lowercase().removePrefix("b-orot-")
    val parts = cleaned.split("-").filter { it.isNotEmpty() }
    if (parts.isEmpty() || parts.any { !it.all(Char::isDigit) }) return null
    val book = parts[0].padStart(2, '0')
    val chapter = (parts.getOrNull(1) ?: "00").padStart(2, '0')
    val paragraph = parts.getOrNull(2) ?: "01"
    return LessonSlug(book, chapter, paragraph)
}

fun Book.matchesSlug(slug: String): Boolean =
    parseLessonSlug(slug)?.bookCode?.toIntOrNull() == number

fun chapterTitleFromLesson(title: String): String {
    val cleaned = title.trim()
    val markers = listOf(", פסקה", " פסקה", ", פס׳", " פס׳")
    for (marker in markers) {
        val index = cleaned.indexOf(marker)
        if (index > 0) return cleaned.substring(0, index).trim(' ', ',', '–', '-', '־')
    }
    return cleaned
}

fun groupIntoChapters(lessons: List<LessonRef>): List<Chapter> {
    val grouped = linkedMapOf<String, MutableList<LessonRef>>()
    val leftovers = mutableListOf<LessonRef>()
    for (lesson in lessons) {
        val slug = lesson.parsedSlug
        if (slug == null) {
            leftovers += lesson
        } else {
            grouped.getOrPut(slug.chapterCode) { mutableListOf() }.add(lesson)
        }
    }
    val chapters = grouped.map { (code, items) ->
        Chapter(
            code = code,
            title = items.first().chapterTitle,
            lessons = items,
        )
    }
    if (leftovers.isEmpty()) return chapters
    return chapters + Chapter(
        code = "other",
        title = "פרקים נוספים",
        lessons = leftovers,
    )
}

fun sortLessons(lessons: List<LessonRef>): List<LessonRef> =
    lessons.sortedWith(
        compareBy<LessonRef> { it.parsedSlug?.bookCode?.toIntOrNull() ?: Int.MAX_VALUE }
            .thenBy { it.parsedSlug?.chapterCode?.toIntOrNull() ?: Int.MAX_VALUE }
            .thenBy { it.parsedSlug?.paragraphCode?.toIntOrNull() ?: Int.MAX_VALUE }
            .thenBy { it.slug },
    )

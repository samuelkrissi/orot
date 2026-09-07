package com.samuelkrissi.orot.data.repo

import com.samuelkrissi.orot.catalog.Book
import com.samuelkrissi.orot.catalog.Books
import com.samuelkrissi.orot.catalog.LessonRef
import com.samuelkrissi.orot.catalog.sortLessons
import com.samuelkrissi.orot.data.api.ShiurDto
import com.samuelkrissi.orot.data.api.YhbApi
import com.samuelkrissi.orot.html.decodeHtmlEntities
import com.samuelkrissi.orot.html.stripHtmlTags
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

data class LessonContent(
    val ref: LessonRef,
    val html: String,
)

@Serializable
private data class CachedLessonRef(
    val id: Long,
    val slug: String,
    val title: String,
    val excerpt: String = "",
    val link: String = "",
)

@Serializable
private data class CachedLessonPage(
    val items: List<CachedLessonRef>,
)

class OrotRepository(
    private val api: YhbApi,
    cacheDir: File,
    private val json: Json,
) {
    private val tocDir = File(cacheDir, "toc").apply { mkdirs() }
    private val lessonDir = File(cacheDir, "lessons").apply { mkdirs() }
    private val memoryToc = mutableMapOf<String, List<LessonRef>>()
    private val memoryLessons = mutableMapOf<Long, LessonContent>()

    suspend fun loadBookToc(book: Book, forceRefresh: Boolean = false): List<LessonRef> {
        if (!forceRefresh) {
            memoryToc[book.id]?.let { return it }
            readTocCache(book.id)?.let {
                memoryToc[book.id] = it
                return it
            }
        }
        val collected = mutableListOf<LessonRef>()
        var page = 1
        while (page <= 20) {
            val batch = try {
                api.listLessons(genreId = book.genreId, page = page)
            } catch (error: retrofit2.HttpException) {
                if (error.code() == 400 && collected.isNotEmpty()) emptyList() else throw error
            }
            if (batch.isEmpty()) break
            collected += batch.map { it.toRef() }
            if (batch.size < 100) break
            page += 1
        }
        val sorted = sortLessons(collected.distinctBy { it.id })
        memoryToc[book.id] = sorted
        writeTocCache(book.id, sorted)
        return sorted
    }

    suspend fun loadLesson(id: Long): LessonContent {
        memoryLessons[id]?.let { return it }
        readLessonCache(id)?.let {
            memoryLessons[id] = it
            return it
        }
        val dto = api.lesson(id)
        val content = LessonContent(ref = dto.toRef(), html = dto.content.rendered)
        memoryLessons[id] = content
        writeLessonCache(content)
        return content
    }

    suspend fun search(query: String, book: Book?): List<LessonRef> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()
        return api.searchLessons(
            query = trimmed,
            genreId = book?.genreId,
            seriesId = if (book == null) Books.SERIES_ID else null,
        ).map { it.toRef() }
    }

    private fun ShiurDto.toRef(): LessonRef = LessonRef(
        id = id,
        slug = slug,
        title = decodeHtmlEntities(title.rendered),
        excerpt = stripHtmlTags(excerpt.rendered),
        link = link,
    )

    private fun readTocCache(bookId: String): List<LessonRef>? {
        val file = File(tocDir, "$bookId.json")
        if (!file.exists()) return null
        return runCatching {
            json.decodeFromString<CachedLessonPage>(file.readText()).items.map {
                LessonRef(it.id, it.slug, it.title, it.excerpt, it.link)
            }
        }.getOrNull()
    }

    private fun writeTocCache(bookId: String, lessons: List<LessonRef>) {
        runCatching {
            val page = CachedLessonPage(
                lessons.map { CachedLessonRef(it.id, it.slug, it.title, it.excerpt, it.link) },
            )
            File(tocDir, "$bookId.json").writeText(json.encodeToString(page))
        }
    }

    private fun readLessonCache(id: Long): LessonContent? {
        val file = File(lessonDir, "$id.json")
        if (!file.exists()) return null
        return runCatching {
            val dto = json.decodeFromString<ShiurDto>(file.readText())
            LessonContent(dto.toRef(), dto.content.rendered)
        }.getOrNull()
    }

    private fun writeLessonCache(content: LessonContent) {
        runCatching {
            val dto = ShiurDto(
                id = content.ref.id,
                slug = content.ref.slug,
                link = content.ref.link,
                title = com.samuelkrissi.orot.data.api.RenderedText(content.ref.title),
                excerpt = com.samuelkrissi.orot.data.api.RenderedText(content.ref.excerpt),
                content = com.samuelkrissi.orot.data.api.RenderedText(content.html),
            )
            File(lessonDir, "${content.ref.id}.json").writeText(json.encodeToString(dto))
        }
    }
}

package com.samuelkrissi.orot.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class RenderedText(
    val rendered: String = "",
)

@Serializable
data class ShiurDto(
    val id: Long,
    val slug: String = "",
    val link: String = "",
    val title: RenderedText = RenderedText(),
    val excerpt: RenderedText = RenderedText(),
    val content: RenderedText = RenderedText(),
)

interface YhbApi {
    @GET("shiurim")
    suspend fun listLessons(
        @Query("genre") genreId: Int,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 100,
        @Query("orderby") orderBy: String = "slug",
        @Query("order") order: String = "asc",
        @Query("_fields") fields: String = "id,slug,title,excerpt,link",
    ): List<ShiurDto>

    @GET("shiurim/{id}")
    suspend fun lesson(
        @Path("id") id: Long,
        @Query("_fields") fields: String = "id,slug,title,excerpt,link,content",
    ): ShiurDto

    @GET("shiurim")
    suspend fun searchLessons(
        @Query("search") query: String,
        @Query("genre") genreId: Int? = null,
        @Query("yseries") seriesId: Int? = null,
        @Query("per_page") perPage: Int = 40,
        @Query("orderby") orderBy: String = "relevance",
        @Query("_fields") fields: String = "id,slug,title,excerpt,link",
    ): List<ShiurDto>
}

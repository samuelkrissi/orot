package com.samuelkrissi.orot.catalog

/**
 * The nine books of ביאורי אורות as published on https://yhb.org.il/b-orot/
 *
 * [genreId] matches the WordPress `genre` taxonomy used by the `shiurim` API.
 */
data class Book(
    val id: String,
    val genreId: Int,
    val number: Int,
    val titleHe: String,
    val coverUrl: String,
)

object Books {
    const val SITE_HOME = "https://yhb.org.il/b-orot/"
    const val API_BASE = "https://yhb.org.il/wp-json/wp/v2/"
    const val SERIES_ID = 2856
    const val SERIES_TITLE = "ביאורי אורות"
    const val SERIES_AUTHOR = "הרב זאב סולטנוביץ'"
    const val SERIES_SOURCE = "ישיבת הר ברכה"

    val all: List<Book> = listOf(
        Book(
            id = "b-orot-01",
            genreId = 2872,
            number = 1,
            titleHe = "אורות ישראל",
            coverUrl = "https://yhb.org.il/wp-content/uploads/2023/08/אורות-ישראל-2_optimized.png",
        ),
        Book(
            id = "b-orot-02",
            genreId = 2935,
            number = 2,
            titleHe = "אורות התשובה",
            coverUrl = "https://yhb.org.il/wp-content/uploads/2023/08/אורות-התשובה-2_optimized.png",
        ),
        Book(
            id = "b-orot-03",
            genreId = 2951,
            number = 3,
            titleHe = "מידות הראי״ה",
            coverUrl = "https://yhb.org.il/wp-content/uploads/2023/08/מידות-הראיה_optimized.png",
        ),
        Book(
            id = "b-orot-04",
            genreId = 2967,
            number = 4,
            titleHe = "אורות התחיה",
            coverUrl = "https://yhb.org.il/wp-content/uploads/2023/08/אורות-התחיה-2_optimized.png",
        ),
        Book(
            id = "b-orot-05",
            genreId = 2923,
            number = 5,
            titleHe = "אורות מאופל",
            coverUrl = "https://yhb.org.il/wp-content/uploads/2023/08/אורות-מאופל_optimized.png",
        ),
        Book(
            id = "b-orot-06",
            genreId = 3019,
            number = 6,
            titleHe = "למהלך האידיאות בישראל",
            coverUrl = "https://cdn1.yhb.org.il/uploads/%D7%9C%D7%9E%D7%94%D7%9C%D7%9A-%D7%94%D7%90%D7%99%D7%93%D7%90%D7%95%D7%AA-%D7%91%D7%99%D7%A9%D7%A8%D7%90%D7%9C.jpg",
        ),
        Book(
            id = "b-orot-07",
            genreId = 3032,
            number = 7,
            titleHe = "זרעונים",
            coverUrl = "https://yhb.org.il/wp-content/uploads/2023/08/זרעונים_optimized.png",
        ),
        Book(
            id = "b-orot-08",
            genreId = 3049,
            number = 8,
            titleHe = "אורות הקודש",
            coverUrl = "https://yhb.org.il/wp-content/uploads/2023/08/אורות-הקודש_optimized.png",
        ),
        Book(
            id = "b-orot-09",
            genreId = 3184,
            number = 9,
            titleHe = "עקבי הצאן",
            coverUrl = "https://cdn1.yhb.org.il/uploads/%D7%94%D7%93%D7%9E%D7%99%D7%94-%D7%A2%D7%A7%D7%91%D7%99-%D7%94%D7%A6%D7%90%D7%9F-%D7%A8%D7%A7%D7%A2-%D7%9C%D7%91%D7%9F.jpg",
        ),
    )

    fun byId(id: String): Book? = all.firstOrNull { it.id == id }

    fun byGenreId(genreId: Int): Book? = all.firstOrNull { it.genreId == genreId }
}

package com.samuelkrissi.orot.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.samuelkrissi.orot.catalog.Books
import com.samuelkrissi.orot.data.AppContainer
import com.samuelkrissi.orot.ui.book.BookScreen
import com.samuelkrissi.orot.ui.book.BookViewModel
import com.samuelkrissi.orot.ui.books.BooksScreen
import com.samuelkrissi.orot.ui.books.BooksViewModel
import com.samuelkrissi.orot.ui.reader.ReaderScreen
import com.samuelkrissi.orot.ui.reader.ReaderViewModel
import com.samuelkrissi.orot.ui.search.SearchScreen
import com.samuelkrissi.orot.ui.search.SearchViewModel

@Composable
fun OrotRoot(container: AppContainer) {
    val navController = rememberNavController()
    val factory = remember(container) { OrotViewModelFactory(container) }

    NavHost(navController = navController, startDestination = "books") {
        composable("books") {
            val vm: BooksViewModel = viewModel(factory = factory)
            BooksScreen(
                viewModel = vm,
                onOpenBook = { bookId -> navController.navigate("book/$bookId") },
                onContinue = { bookId, lessonId ->
                    navController.navigate("reader/$bookId/$lessonId")
                },
                onSearch = { navController.navigate("search/all") },
            )
        }
        composable(
            route = "book/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType }),
        ) { entry ->
            val bookId = entry.arguments?.getString("bookId").orEmpty()
            val book = Books.byId(bookId) ?: return@composable
            val vm: BookViewModel = viewModel(factory = factory)
            BookScreen(
                book = book,
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenLesson = { lessonId ->
                    navController.navigate("reader/${book.id}/$lessonId")
                },
                onSearch = { navController.navigate("search/${book.id}") },
            )
        }
        composable(
            route = "reader/{bookId}/{lessonId}",
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.LongType },
            ),
        ) { entry ->
            val bookId = entry.arguments?.getString("bookId").orEmpty()
            val lessonId = entry.arguments?.getLong("lessonId") ?: return@composable
            val vm: ReaderViewModel = viewModel(factory = factory)
            ReaderScreen(
                bookId = bookId,
                lessonId = lessonId,
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenLesson = { nextBookId, nextLessonId ->
                    navController.navigate("reader/$nextBookId/$nextLessonId") {
                        popUpTo("reader/$bookId/$lessonId") { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = "search/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType }),
        ) { entry ->
            val bookId = entry.arguments?.getString("bookId").orEmpty()
            val vm: SearchViewModel = viewModel(factory = factory)
            SearchScreen(
                bookId = bookId.takeIf { it.isNotBlank() && it != "all" },
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenLesson = { foundBookId, lessonId ->
                    navController.navigate("reader/$foundBookId/$lessonId")
                },
            )
        }
    }
}

class OrotViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(BooksViewModel::class.java) ->
                BooksViewModel(container.prefs) as T
            modelClass.isAssignableFrom(BookViewModel::class.java) ->
                BookViewModel(container.repository) as T
            modelClass.isAssignableFrom(ReaderViewModel::class.java) ->
                ReaderViewModel(container.repository, container.prefs) as T
            modelClass.isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(container.repository) as T
            else -> error("Unknown ViewModel ${modelClass.name}")
        }
    }
}

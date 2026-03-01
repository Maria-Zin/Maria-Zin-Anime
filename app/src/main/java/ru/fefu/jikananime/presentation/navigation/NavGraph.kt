package ru.fefu.jikananime.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.fefu.jikananime.presentation.screens.detail.DetailScreen
import ru.fefu.jikananime.presentation.screens.detail.DetailViewModel
import ru.fefu.jikananime.presentation.screens.favourites.FavouritesScreen
import ru.fefu.jikananime.presentation.screens.favourites.FavouritesViewModel
import ru.fefu.jikananime.presentation.screens.search.SearchScreen
import ru.fefu.jikananime.presentation.screens.search.SearchViewModel

@Composable
fun NavGraph(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Search.route,
        modifier = modifier
    ) {
        composable(route = Screen.Search.route) {
            val viewModel: SearchViewModel = hiltViewModel()
            SearchScreen(
                state = viewModel.state.value,
                onEvent = viewModel::onEvent,
                onAnimeClick = { animeId ->
                    navController.navigate(Screen.Detail.createRoute(animeId))
                },
                onNavigateToFavourites = {
                    navController.navigate(Screen.Favourites.route)
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("animeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val animeId = backStackEntry.arguments?.getInt("animeId") ?: 0
            val viewModel: DetailViewModel = hiltViewModel()
            DetailScreen(
                state = viewModel.state.value,
                onEvent = viewModel::onEvent,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Favourites.route) {
            val viewModel: FavouritesViewModel = hiltViewModel()
            FavouritesScreen(
                state = viewModel.state.value,
                onEvent = viewModel::onEvent,
                onAnimeClick = { animeId ->
                    navController.navigate(Screen.Detail.createRoute(animeId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Search : Screen("search")
    object Detail : Screen("detail/{animeId}") {
        fun createRoute(animeId: Int) = "detail/$animeId"
    }
    object Favourites : Screen("favourites")
}
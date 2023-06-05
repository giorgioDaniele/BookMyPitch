package com.example.courtreservation

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.courtreservation.accessview.AccessView
import com.example.courtreservation.accessview.AccessViewModel
import com.example.courtreservation.addreservations.AddReservationsScreen
import com.example.courtreservation.addreservations.BrowseAvailabilityScreen
import com.example.courtreservation.profile.EditProfileScreen
import com.example.courtreservation.profile.ProfileScreen
import com.example.courtreservation.rateplaygrounds.RatePlaygroundsScreen
import com.example.courtreservation.server.ServerViewModel
import com.example.courtreservation.service.FirebaseMessages
import com.example.courtreservation.show_edit_reservations.MainView
import com.example.courtreservation.show_edit_reservations.ShowEditViewModel
import com.example.courtreservation.ui.theme.CourtReservationTheme
import com.example.courtreservation.userprofiles.FriendsScreen
import com.example.courtreservation.userprofiles.UserProfileScreen
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val accessViewModel   by viewModels<AccessViewModel>()
    private val showEditViewModel by viewModels<ShowEditViewModel>()
    private val serverViewModel by viewModels<ServerViewModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // FIREBASE MESSAGING
        FirebaseMessages.subscribeTopic(this, "messages")
        FirebaseMessages.subscribeTopic(this, "reservations")
        serverViewModel.updateReservations()
        setContent {
            CourtReservationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    //var nickname by remember { mutableStateOf(accessViewModel.getNickname()) }
                    if(accessViewModel.userNickname.value == "") {
                        AccessView(vm = accessViewModel ) {  }
                    } else {
                        BottomAppBar(
                            accessViewModel,
                            vmShowEdit = showEditViewModel,
                            vmServer = serverViewModel,
                            accessViewModel.userNickname.value.toString()
                        )
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object ShowReservations: Screen("show_reservations", Icons.Filled.List, "Matches")
    object Profile: Screen("profile", Icons.Filled.Person, "Profile")
    object Friends: Screen("friends", Icons.Filled.Search, "Friends")
    object RatePlaygrounds: Screen("rate_playgrounds", Icons.Filled.Star, "Rate")
    object AddReservations: Screen("add_reservations", Icons.Filled.AddCircle, "Add")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomAppBar(accessVM:AccessViewModel, vmShowEdit: ShowEditViewModel, vmServer: ServerViewModel, nickname: String) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    //addUsers(Firebase.firestore)
    //addReservations(Firebase.firestore)

    val screens = listOf(
        Screen.ShowReservations,
        Screen.RatePlaygrounds,
        Screen.AddReservations,
        Screen.Friends,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            val currentDestination = navBackStackEntry?.destination

            NavigationBar {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route?.startsWith(screen.route) ?: false } == true,
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(unselectedIconColor = MaterialTheme.colorScheme.surfaceColorAtElevation(360.dp)),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = "show_reservations", Modifier.padding(innerPadding)) {
            screens.forEach { screen ->
                composable(screen.route) {
                    when (screen.route) {
                        "show_reservations" -> { MainView(vm = vmShowEdit, vmServer) }
                        "rate_playgrounds" -> RatePlaygroundsScreen(nickname)
                        "add_reservations" -> AddReservationsScreen(
                            nickname,
                            onNavigateToBrowseAvailabilityScreen = { playgroundId, sport, level, playgroundName ->
                                navController.navigate("add_reservations/browse_availability/$playgroundId?sport=$sport&level=$level&playgroundName=$playgroundName")
                            },
                            onNavigateToUserProfile = { username ->
                                navController.navigate("friends/user/$username")
                            }
                        ) {
                            navController.navigate("profile") {
                                popUpTo(navController.graph.id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        "friends" -> FriendsScreen { username ->
                            navController.navigate("friends/user/$username")
                        }
                        "profile" -> ProfileScreen (accessVM = accessVM){
                            navController.navigate("profile/edit")
                        }
                        else -> { }
                    }
                }
                composable("profile/edit") {
                    EditProfileScreen()
                }
                composable(
                    "friends/user/{username}",
                    arguments = listOf(navArgument("username") { type = NavType.StringType })
                ) {
                    UserProfileScreen(it.arguments!!.getString("username")!!)
                }
                composable(
                    "add_reservations/browse_availability/{playgroundId}?sport={sport}&level={level}&playgroundName={playgroundName}",
                    arguments = listOf(
                        navArgument("playgroundId") { type = NavType.StringType },
                        navArgument("sport") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                        navArgument("level") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                        navArgument("playgroundName") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) {
                    BrowseAvailabilityScreen(nickname)
                }
            }
        }
    }
}

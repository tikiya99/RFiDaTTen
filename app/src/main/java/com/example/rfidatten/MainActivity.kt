package com.example.rfidatten

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rfidatten.ui.navigation.Screen
import com.example.rfidatten.ui.screen.*
import com.example.rfidatten.ui.theme.RFiDaTTenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RFiDaTTenTheme {
                RFIDAttendanceApp()
            }
        }
    }
}

@Composable
fun RFIDAttendanceApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToCardManager = {
                    navController.navigate(Screen.CardManager.route)
                },
                onNavigateToSessions = {
                    navController.navigate(Screen.Sessions.route)
                }
            )
        }
        
        composable(Screen.CardManager.route) {
            CardManagerScreen(
                onNavigateToProfile = { profileId ->
                    navController.navigate(Screen.Profile.createRoute(profileId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: 0L
            ProfileScreen(
                profileId = profileId,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Sessions.route) {
            SessionsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
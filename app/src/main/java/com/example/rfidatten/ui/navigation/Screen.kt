package com.example.rfidatten.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object CardManager : Screen("card_manager")
    object Profile : Screen("profile/{profileId}") {
        fun createRoute(profileId: Long) = "profile/$profileId"
    }
    object Sessions : Screen("sessions")
}

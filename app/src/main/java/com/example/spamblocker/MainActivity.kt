package com.example.spamblocker

import android.Manifest
import android.app.role.RoleManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spamblocker.ui.BlockedHistoryScreen
import com.example.spamblocker.ui.PatternScreen
import com.example.spamblocker.ui.theme.SpamBlockerTheme

sealed class Screen(val route: String, val label: String) {
    object Patterns : Screen("patterns", "Patterns")
    object History : Screen("history", "Blocked Calls")
}

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.Q)
    private val requestRole =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("ROLE", "Role result = ${result.resultCode}")
            val roleManager = getSystemService(RoleManager::class.java)
            Log.d("ROLE", "Role held = ${roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)}")
        }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            results.forEach { (perm, granted) -> Log.d("PERM", "$perm => $granted") }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions.launch(
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.ANSWER_PHONE_CALLS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_NUMBERS
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                requestRole.launch(
                    roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                )
            }
        }

        enableEdgeToEdge()
        setContent {
            SpamBlockerTheme {
                SpamBlockerApp()
            }
        }
    }
}

@Composable
fun SpamBlockerApp() {
    val navController = rememberNavController()
    val items = listOf(Screen.Patterns, Screen.History)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (screen) {
                                    Screen.Patterns -> Icons.Default.Home
                                    Screen.History -> Icons.Default.Warning
                                },
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Patterns.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Patterns.route) { PatternScreen() }
            composable(Screen.History.route) { BlockedHistoryScreen() }
        }
    }
}
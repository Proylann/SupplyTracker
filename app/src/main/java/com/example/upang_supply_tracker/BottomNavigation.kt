package com.example.upang_supply_tracker

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.upang_supply_tracker.Services.CartService
import com.example.upang_supply_tracker.databinding.ActivityBottomNavigationBinding

class BottomNavigation : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavigationBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Hides the default ActionBar
        CartService.initialize(applicationContext)

        binding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        // Get NavController from NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment_activity_bottom_navigation
        ) as NavHostFragment
        navController = navHostFragment.navController

        // Define top-level destinations
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )

        // Setup custom navigation with proper back stack handling
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Check if we're not already on the home fragment
                    if (navController.currentDestination?.id != R.id.navigation_home) {
                        try {
                            when (navController.currentDestination?.id) {
                                R.id.uniforms -> navController.navigate(R.id.action_uniforms_to_navigation_home)
                                R.id.books -> navController.navigate(R.id.action_books_to_navigation_home)
                                R.id.modules -> navController.navigate(R.id.action_modules_to_navigation_home)
                                else -> {
                                    // Ensure we don't lose the back stack by using safe navigation
                                    navController.navigate(R.id.navigation_home) {
                                        // Pop up to the start destination (home)
                                        popUpTo(R.id.navigation_home) {
                                            // Don't recreate home if already in back stack
                                            saveState = true
                                        }
                                        // Avoid duplication of the destination
                                        launchSingleTop = true
                                        // Restore state when re-selecting a previously selected item
                                        restoreState = true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Fallback in case the specific navigation action fails
                            navController.navigate(R.id.navigation_home)
                        }
                    }
                    true
                }
                R.id.navigation_dashboard -> {
                    // Only navigate if we're not already on the dashboard
                    if (navController.currentDestination?.id != R.id.navigation_dashboard) {
                        try {
                            when (navController.currentDestination?.id) {
                                R.id.uniforms -> navController.navigate(R.id.action_uniforms_to_navigation_dashboard)
                                R.id.books -> navController.navigate(R.id.action_books_to_navigation_dashboard)
                                R.id.modules -> navController.navigate(R.id.action_modules_to_navigation_dashboard)
                                else -> {
                                    navController.navigate(R.id.navigation_dashboard) {
                                        // Pop up to the home destination
                                        popUpTo(R.id.navigation_home) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Fallback in case the specific navigation action fails
                            navController.navigate(R.id.navigation_dashboard)
                        }
                    }
                    true
                }
                R.id.navigation_notifications -> {
                    // Only navigate if we're not already on notifications
                    if (navController.currentDestination?.id != R.id.navigation_notifications) {
                        try {
                            when (navController.currentDestination?.id) {
                                R.id.uniforms -> navController.navigate(R.id.action_uniforms_to_navigation_notifications)
                                R.id.books -> navController.navigate(R.id.action_books_to_navigation_notifications)
                                R.id.modules -> navController.navigate(R.id.action_modules_to_navigation_notifications)
                                else -> {
                                    navController.navigate(R.id.navigation_notifications) {
                                        // Pop up to the home destination
                                        popUpTo(R.id.navigation_home) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Fallback in case the specific navigation action fails
                            navController.navigate(R.id.navigation_notifications)
                        }
                    }
                    true
                }
                else -> false
            }
        }

        // Keep the bottom navigation in sync with the current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home, R.id.uniforms, R.id.books, R.id.modules ->
                    navView.menu.findItem(R.id.navigation_home).isChecked = true
                R.id.navigation_dashboard ->
                    navView.menu.findItem(R.id.navigation_dashboard).isChecked = true
                R.id.navigation_notifications ->
                    navView.menu.findItem(R.id.navigation_notifications).isChecked = true
            }
        }
    }

    // Handle system back button
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    // Override back button to ensure proper navigation
    @Override
    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.navigation_home) {
            // If we're on the home screen, proceed with normal back behavior (exit app)
            super.onBackPressed()
        } else {
            // If we're on another destination, navigate back in our graph
            navController.navigateUp()
        }
    }
}
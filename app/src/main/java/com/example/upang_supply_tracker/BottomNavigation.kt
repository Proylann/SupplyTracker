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

        // Setup custom navigation with safeguards
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Check if we're not already on the home fragment
                    if (navController.currentDestination?.id != R.id.navigation_home) {
                        // If in Uniforms fragment, use the custom action to navigate back to home
                        if (navController.currentDestination?.id == R.id.uniforms) {
                            navController.navigate(R.id.action_uniforms_to_navigation_home)
                        } else if(navController.currentDestination?.id == R.id.books){

                        }
                        else {
                            navController.navigate(R.id.navigation_home)
                        }
                    }
                    true
                }
                R.id.navigation_dashboard -> {
                    // Only navigate if we're not already on the dashboard
                    if (navController.currentDestination?.id != R.id.navigation_dashboard) {
                        navController.navigate(R.id.navigation_dashboard)
                    }
                    true
                }
                R.id.navigation_notifications -> {
                    // Only navigate if we're not already on notifications
                    if (navController.currentDestination?.id != R.id.navigation_notifications) {
                        navController.navigate(R.id.navigation_notifications)
                    }
                    true
                }
                else -> false
            }
        }

        // Keep the bottom navigation in sync with the current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> navView.menu.findItem(R.id.navigation_home).isChecked = true
                R.id.navigation_dashboard -> navView.menu.findItem(R.id.navigation_dashboard).isChecked = true
                R.id.navigation_notifications -> navView.menu.findItem(R.id.navigation_notifications).isChecked = true

            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
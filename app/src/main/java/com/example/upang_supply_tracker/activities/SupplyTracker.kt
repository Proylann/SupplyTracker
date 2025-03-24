package com.example.upang_supply_tracker.activities

import android.app.Application
import com.example.upang_supply_tracker.Services.CartService
import com.example.upang_supply_tracker.Services.UserManager

class SupplyTracker : Application() {
    override fun onCreate() {
        super.onCreate()
        UserManager.initialize(applicationContext)
        CartService.initialize(applicationContext)
    }
}
package com.example.upang_supply_tracker

import android.app.Application
import com.example.upang_supply_tracker.Services.CartService

class SupplyTracker : Application() {
    override fun onCreate() {
        super.onCreate()

        CartService.initialize(applicationContext)
    }
}
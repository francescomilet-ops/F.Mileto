package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubscriptionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shirin_subscription_prefs", Context.MODE_PRIVATE)

    private val _subscriptionTier = MutableStateFlow(prefs.getString("subscription_tier", "FREE") ?: "FREE")
    val subscriptionTier: StateFlow<String> = _subscriptionTier.asStateFlow()

    private val _isTrialActive = MutableStateFlow(prefs.getBoolean("is_trial_active", true))
    val isTrialActive: StateFlow<Boolean> = _isTrialActive.asStateFlow()

    fun setSubscriptionTier(tier: String) {
        prefs.edit().putString("subscription_tier", tier).apply()
        _subscriptionTier.value = tier
    }

    fun setTrialActive(isActive: Boolean) {
        prefs.edit().putBoolean("is_trial_active", isActive).apply()
        _isTrialActive.value = isActive
    }
}

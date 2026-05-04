package com.lssgoo.planner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.lssgoo.planner.data.analytics.AnalyticsManager
import com.lssgoo.planner.data.local.LocalStorageManager
import com.lssgoo.planner.data.model.AnalyticsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(application: Application) : BaseViewModel(application) {

    private val storage = LocalStorageManager(application)
    private val analyticsManager = AnalyticsManager(storage)

    private val _analyticsData = MutableStateFlow<AnalyticsData?>(null)
    val analyticsData: StateFlow<AnalyticsData?> = _analyticsData.asStateFlow()

    init {
        refreshAnalytics()
    }

    fun refreshAnalytics() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _analyticsData.value = analyticsManager.generateComprehensiveReport()
            _isLoading.value = false
        }
    }
}

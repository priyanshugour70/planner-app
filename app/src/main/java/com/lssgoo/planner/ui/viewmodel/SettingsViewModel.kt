package com.lssgoo.planner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.lssgoo.planner.data.local.LocalStorageManager
import com.lssgoo.planner.data.remote.PlannerApiService
import com.lssgoo.planner.data.repository.DataRepository
import com.lssgoo.planner.features.settings.models.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : BaseViewModel(application) {

    private val repository = DataRepository(application)
    private val storage = LocalStorageManager(application)
    private val apiService = PlannerApiService(application)

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            _userProfile.value = storage.getUserProfile()
            _isAuthenticated.value = apiService.isLoggedIn()
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            storage.saveUserProfile(profile)
            _userProfile.value = profile
            if (apiService.isLoggedIn()) {
                try {
                    apiService.updateProfile(mapOf(
                        "firstName" to profile.firstName,
                        "lastName" to profile.lastName,
                        "phoneNumber" to profile.phoneNumber,
                        "dateOfBirth" to profile.dateOfBirth,
                        "occupation" to profile.occupation
                    ))
                } catch (_: Exception) {}
            }
        }
    }

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.login(email, password)
                if (response?.status == "OK" || response?.data != null) {
                    _isAuthenticated.value = true
                    onResult(true, null)
                } else {
                    onResult(false, response?.message ?: "Login failed")
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Network error")
            }
        }
    }

    fun register(firstName: String, lastName: String, email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.register(firstName, lastName, email, password)
                if (response?.status == "OK" || response?.data != null) {
                    _isAuthenticated.value = true
                    onResult(true, null)
                } else {
                    onResult(false, response?.message ?: "Registration failed")
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Network error")
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            try { apiService.logout() } catch (_: Exception) {}
            _isAuthenticated.value = false
        }
    }
}

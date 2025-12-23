package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rojgar.model.CalendarEventModel
import com.example.rojgar.model.CalendarMonthModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarViewModel(
    private val repo: CalendarRepo,
    private val currentUserId: String,
    private val currentUserType: String // "company" or "jobseeker"
) : ViewModel() {

    private val _currentMonth = MutableLiveData<CalendarMonthModel>()
    val currentMonth: LiveData<CalendarMonthModel> get() = _currentMonth

    private val _selectedDateEvents = MutableLiveData<List<CalendarEventModel>>()
    val selectedDateEvents: LiveData<List<CalendarEventModel>> get() = _selectedDateEvents

    private val _upcomingEvents = MutableLiveData<List<CalendarEventModel>>()
    val upcomingEvents: LiveData<List<CalendarEventModel>> get() = _upcomingEvents

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    init {
        loadCurrentMonth()
        loadUpcomingEvents(5)
    }

    fun loadCurrentMonth() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Convert to 1-12

        loadMonth(year, month)
    }

    fun loadMonth(year: Int, month: Int) {
        _loading.value = true
        viewModelScope.launch {
            repo.getEventsByMonth(currentUserId, currentUserType, year, month) { success, message, monthData ->
                _loading.postValue(false)
                if (success && monthData != null) {
                    _currentMonth.postValue(monthData)
                } else {
                    _error.postValue(message)
                }
            }
        }
    }

    fun loadEventsForDate(date: String) {
        _loading.value = true
        viewModelScope.launch {
            repo.getEventsByDate(currentUserId, currentUserType, date) { success, message, events ->
                _loading.postValue(false)
                if (success) {
                    _selectedDateEvents.postValue(events ?: emptyList())
                } else {
                    _error.postValue(message)
                    _selectedDateEvents.postValue(emptyList())
                }
            }
        }
    }

    fun loadUpcomingEvents(limit: Int = 5) {
        viewModelScope.launch {
            repo.getUpcomingEvents(currentUserId, currentUserType, limit) { success, message, events ->
                if (success) {
                    _upcomingEvents.postValue(events ?: emptyList())
                }
            }
        }
    }

    fun addEvent(event: CalendarEventModel, callback: (Boolean, String) -> Unit) {
        // Ensure event has correct user info
        val eventWithUser = event.copy(
            userId = currentUserId,
            userType = currentUserType
        )

        viewModelScope.launch {
            repo.addEvent(eventWithUser) { success, message ->
                if (success) {
                    // Refresh data
                    loadCurrentMonth()
                    loadUpcomingEvents(5)
                    loadEventsForDate(event.eventDate)
                }
                callback(success, message)
            }
        }
    }

    fun updateEvent(eventId: String, event: CalendarEventModel, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            repo.updateEvent(eventId, event) { success, message ->
                if (success) {
                    // Refresh data
                    loadCurrentMonth()
                    loadUpcomingEvents(5)
                    loadEventsForDate(event.eventDate)
                }
                callback(success, message)
            }
        }
    }

    fun deleteEvent(eventId: String, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            repo.deleteEvent(eventId) { success, message ->
                if (success) {
                    // Refresh data
                    loadCurrentMonth()
                    loadUpcomingEvents(5)
                    // Note: We don't know which date to refresh, so we'll refresh current month
                }
                callback(success, message)
            }
        }
    }

    fun navigateToPreviousMonth() {
        calendar.add(Calendar.MONTH, -1)
        loadCurrentMonth()
    }

    fun navigateToNextMonth() {
        calendar.add(Calendar.MONTH, 1)
        loadCurrentMonth()
    }

    fun getCurrentMonthDisplay(): String {
        return displayDateFormat.format(calendar.time)
    }

    fun getTodayDate(): String {
        return dateFormat.format(System.currentTimeMillis())
    }

    fun isToday(date: String): Boolean {
        return date == getTodayDate()
    }
}
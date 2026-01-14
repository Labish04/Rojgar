package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.rojgar.model.CalendarEventModel
import com.example.rojgar.repository.CalendarRepo

class CalendarViewModel(val repo: CalendarRepo) {

    private val _events = MutableLiveData<List<CalendarEventModel>>(emptyList())
    val events: LiveData<List<CalendarEventModel>> = _events

    private val _selectedDayEvents = MutableLiveData<List<CalendarEventModel>>(emptyList())
    val selectedDayEvents: LiveData<List<CalendarEventModel>> = _selectedDayEvents

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _message = MutableLiveData<String>("")
    val message: LiveData<String> = _message

    fun observeAllEventsForUser(userId: String) {
        _loading.value = true
        repo.observeAllEventsForUser(userId) { success, message, events ->
            _loading.value = false
            _message.value = message
            if (success && events != null) {
                _events.value = events
            } else {
                _events.value = emptyList()
            }
        }
    }

    fun observeEventsForUserInRange(userId: String, startMillis: Long, endMillis: Long) {
        _loading.value = true
        repo.observeEventsForUserInRange(userId, startMillis, endMillis) { success, message, events ->
            _loading.value = false
            _message.value = message
            if (success && events != null) {
                _selectedDayEvents.value = events
            } else {
                _selectedDayEvents.value = emptyList()
            }
        }
    }

    fun getEventById(eventId: String, callback: (Boolean, String, CalendarEventModel?) -> Unit) {
        repo.getEventById(eventId, callback)
    }

    fun addEvent(event: CalendarEventModel, callback: (Boolean, String, String?) -> Unit) {
        _loading.value = true
        repo.addEvent(event) { success, message, eventId ->
            _loading.value = false
            _message.value = message
            callback(success, message, eventId)
        }
    }

    fun updateEvent(event: CalendarEventModel, callback: (Boolean, String) -> Unit) {
        _loading.value = true
        repo.updateEvent(event) { success, message ->
            _loading.value = false
            _message.value = message
            callback(success, message)
        }
    }

    fun deleteEvent(eventId: String, callback: (Boolean, String) -> Unit) {
        _loading.value = true
        repo.deleteEvent(eventId) { success, message ->
            _loading.value = false
            _message.value = message
            callback(success, message)
        }
    }
}

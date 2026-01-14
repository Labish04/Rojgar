package com.example.rojgar.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rojgar.model.ReviewModel
import com.example.rojgar.repository.ReviewRepo
import java.text.SimpleDateFormat
import java.util.*

class ReviewViewModel(private val repo: ReviewRepo) : ViewModel() {

    companion object {
        private const val TAG = "ReviewViewModel"
    }

    // Loading state
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> get() = _loading

    // Reviews list
    private val _reviews = MutableLiveData<List<ReviewModel>>(emptyList())
    val reviews: LiveData<List<ReviewModel>> get() = _reviews

    // Average rating
    private val _averageRating = MutableLiveData<Double>(0.0)
    val averageRating: LiveData<Double> get() = _averageRating

    // Current user review
    private val _userReview = MutableLiveData<ReviewModel?>()
    val userReview: LiveData<ReviewModel?> get() = _userReview

    // Toast messages
    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> get() = _toastMessage

    // Job seeker usernames cache
    private val _jobSeekerUsernames = MutableLiveData<Map<String, String>>(emptyMap())
    val jobSeekerUsernames: LiveData<Map<String, String>> get() = _jobSeekerUsernames

    // Review listener ID for cleanup
    private var reviewListenerId: String? = null

    fun addReview(review: ReviewModel) {
        _loading.value = true
        repo.addReview(review) { success, message ->
            _loading.value = false
            _toastMessage.value = message
            if (success) {
                _userReview.value = review
                // No need to manually reload, real-time listener will update
            }
        }
    }

    fun fetchJobSeekerUsername(jobSeekerId: String) {
        if (_jobSeekerUsernames.value?.containsKey(jobSeekerId) == true) {
            return
        }

        repo.getJobSeekerUsername(jobSeekerId) { success, _, username ->
            if (success && username != null) {
                val currentMap = _jobSeekerUsernames.value?.toMutableMap() ?: mutableMapOf()
                currentMap[jobSeekerId] = username
                _jobSeekerUsernames.value = currentMap
            }
        }
    }

    fun getJobSeekerUsername(jobSeekerId: String): String? {
        return _jobSeekerUsernames.value?.get(jobSeekerId)
    }

    fun updateReview(review: ReviewModel) {
        _loading.value = true
        repo.updateReview(review) { success, message ->
            _loading.value = false
            _toastMessage.value = message
            if (success) {
                _userReview.value = review
            }
        }
    }

    fun deleteReview(reviewId: String, companyId: String) {
        _loading.value = true
        repo.deleteReview(reviewId) { success, message ->
            _loading.value = false
            _toastMessage.value = message
            if (success) {
                _userReview.value = null
            }
        }
    }

    fun loadReviews(companyId: String) {
        Log.d(TAG, "Loading reviews for company: $companyId")
        _loading.value = true
        repo.getReviewsByCompanyId(companyId) { success, message, reviews ->
            _loading.value = false
            Log.d(TAG, "Reviews loaded: success=$success, count=${reviews?.size ?: 0}")
            if (success && reviews != null) {
                _reviews.value = reviews
                calculateAverageRating(reviews)

                // Fetch usernames for all reviewers
                reviews.forEach { review ->
                    if (getJobSeekerUsername(review.userId) == null) {
                        fetchJobSeekerUsername(review.userId)
                    }
                }
            } else {
                _toastMessage.value = message
                Log.e(TAG, "Failed to load reviews: $message")
            }
        }
    }

    fun setupRealTimeUpdates(companyId: String, currentUserId: String) {
        Log.d(TAG, "Setting up real-time updates for company: $companyId, user: $currentUserId")

        // Remove existing listener if any
        reviewListenerId?.let {
            Log.d(TAG, "Removing existing listener: $it")
            repo.removeReviewListener(it)
        }

        // Setup real-time listener
        reviewListenerId = repo.addReviewListener(
            companyId = companyId,
            onDataChange = { reviews ->
                Log.d(TAG, "Real-time update received: ${reviews.size} reviews")
                _reviews.value = reviews
                calculateAverageRating(reviews)

                // Fetch usernames for all reviewers
                reviews.forEach { review ->
                    if (getJobSeekerUsername(review.userId) == null) {
                        fetchJobSeekerUsername(review.userId)
                    }
                }

                // Update current user review
                val userReviewFound = reviews.find { it.userId == currentUserId }
                _userReview.value = userReviewFound
                Log.d(TAG, "User review found: ${userReviewFound != null}")
            },
            onError = { error ->
                Log.e(TAG, "Real-time updates error: $error")
                _toastMessage.value = "Real-time updates error: $error"
            }
        )
        Log.d(TAG, "Real-time listener ID: $reviewListenerId")
    }

    fun checkUserReview(userId: String, companyId: String) {
        Log.d(TAG, "Checking user review: userId=$userId, companyId=$companyId")
        repo.checkUserAlreadyReviewed(userId, companyId) { success, message, review ->
            Log.d(TAG, "User review check result: success=$success, hasReview=${review != null}")
            _userReview.value = if (success) review else null
        }
    }

    private fun calculateAverageRating(reviews: List<ReviewModel>) {
        val average = if (reviews.isNotEmpty()) {
            reviews.map { it.rating }.average()
        } else {
            0.0
        }
        _averageRating.value = average
        Log.d(TAG, "Average rating calculated: $average from ${reviews.size} reviews")
    }

    fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            weeks < 4 -> "${weeks}w ago"
            months < 12 -> "${months}mo ago"
            else -> "${years}y ago"
        }
    }

    fun formatTimestampForDisplay(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getEditedLabel(review: ReviewModel): String {
        return if (review.isEdited) {
            val editedTime = review.editedTimestamp?.let { formatTimeAgo(it) } ?: ""
            " (edited${if (editedTime.isNotEmpty()) " $editedTime" else ""})"
        } else {
            ""
        }
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared, removing listener")
        // Clean up listeners when ViewModel is destroyed
        reviewListenerId?.let { repo.removeReviewListener(it) }
    }

}

class ReviewViewModelFactory(private val repo: ReviewRepo) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReviewViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
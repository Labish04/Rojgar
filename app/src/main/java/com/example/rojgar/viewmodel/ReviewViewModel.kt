package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rojgar.model.ReviewModel
import com.example.rojgar.repository.ReviewRepo
import java.text.SimpleDateFormat
import java.util.*

class ReviewViewModel(private val repo: ReviewRepo) : ViewModel() {

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

    // Review listener ID for cleanup
    private var reviewListenerId: String? = null

    fun addReview(review: ReviewModel) {
        _loading.value = true
        repo.addReview(review) { success, message ->
            _loading.value = false
            _toastMessage.value = message
            if (success) {
                _userReview.value = review
                loadReviews(review.companyId) // Reload reviews after adding
            }
        }
    }

    fun updateReview(review: ReviewModel) {
        _loading.value = true
        repo.updateReview(review) { success, message ->
            _loading.value = false
            _toastMessage.value = message
            if (success) {
                _userReview.value = review
                loadReviews(review.companyId) // Reload reviews after updating
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
                loadReviews(companyId) // Reload reviews after deleting
            }
        }
    }

    fun loadReviews(companyId: String) {
        _loading.value = true
        repo.getReviewsByCompanyId(companyId) { success, message, reviews ->
            _loading.value = false
            if (success && reviews != null) {
                _reviews.value = reviews
                calculateAverageRating(reviews)
            } else {
                _toastMessage.value = message
            }
        }
    }

    fun setupRealTimeUpdates(companyId: String, currentUserId: String) {
        // Remove existing listener if any
        reviewListenerId?.let { repo.removeReviewListener(it) }

        reviewListenerId = repo.addReviewListener(
            companyId = companyId,
            onDataChange = { reviews ->
                _reviews.value = reviews
                calculateAverageRating(reviews)

                // Update current user review
                _userReview.value = reviews.find { it.userId == currentUserId }
            },
            onError = { error ->
                _toastMessage.value = error
            }
        )
    }

    fun checkUserReview(userId: String, companyId: String) {
        repo.checkUserAlreadyReviewed(userId, companyId) { success, _, review ->
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

package com.example.rojgar.repository

import com.example.rojgar.model.ReviewModel

interface ReviewRepo {

    fun addReview(
        review: ReviewModel,
        callback: (Boolean, String) -> Unit
    )

    fun updateReview(
        review: ReviewModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteReview(
        reviewId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getReviewsByCompanyId(
        companyId: String,
        callback: (Boolean, String, List<ReviewModel>?) -> Unit
    )

    fun getReviewById(
        reviewId: String,
        callback: (Boolean, String, ReviewModel?) -> Unit
    )

    fun checkUserAlreadyReviewed(
        userId: String,
        companyId: String,
        callback: (Boolean, String, ReviewModel?) -> Unit
    )

    fun getAverageRating(
        companyId: String,
        callback: (Boolean, String, Double) -> Unit
    )

    fun addReviewListener(
        companyId: String,
        onDataChange: (List<ReviewModel>) -> Unit,
        onError: (String) -> Unit
    ): String?

    fun removeReviewListener(listenerId: String?)

    fun getJobSeekerUsername(
        jobSeekerId: String,
        callback: (Boolean, String, String?) -> Unit
    )
}

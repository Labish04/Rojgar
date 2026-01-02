package com.example.rojgar.repository

import com.example.rojgar.model.ReviewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class ReviewRepoImpl : ReviewRepo {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref: DatabaseReference = database.getReference("Reviews")

    private val listeners = mutableMapOf<String, ValueEventListener>()

    override fun addReview(
        review: ReviewModel,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            val reviewId = ref.push().key ?: UUID.randomUUID().toString()
            val reviewWithId = review.copy(reviewId = reviewId)

            ref.child(reviewId).setValue(reviewWithId.toMap()).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Review added successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to add review")
                }
            }
        } catch (e: Exception) {
            callback(false, e.message ?: "An error occurred")
        }
    }

    override fun updateReview(
        review: ReviewModel,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            val updatedReview = review.copy(
                isEdited = true,
                editedTimestamp = System.currentTimeMillis()
            )

            ref.child(review.reviewId).setValue(updatedReview.toMap()).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Review updated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to update review")
                }
            }
        } catch (e: Exception) {
            callback(false, e.message ?: "An error occurred")
        }
    }

    override fun deleteReview(
        reviewId: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            ref.child(reviewId).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Review deleted successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to delete review")
                }
            }
        } catch (e: Exception) {
            callback(false, e.message ?: "An error occurred")
        }
    }

    override fun getReviewsByCompanyId(
        companyId: String,
        callback: (Boolean, String, List<ReviewModel>?) -> Unit
    ) {
        try {
            ref.orderByChild("companyId").equalTo(companyId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reviews = mutableListOf<ReviewModel>()
                    for (childSnapshot in snapshot.children) {
                        try {
                            val reviewMap = childSnapshot.value as? Map<String, Any?>
                            if (reviewMap != null) {
                                val review = ReviewModel.fromMap(reviewMap)
                                reviews.add(review)
                            }
                        } catch (e: Exception) {
                            // Skip malformed review data
                            continue
                        }
                    }
                    // Sort by timestamp (newest first)
                    reviews.sortByDescending { it.timestamp }
                    callback(true, "Reviews fetched successfully", reviews)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
        } catch (e: Exception) {
            callback(false, e.message ?: "An error occurred", null)
        }
    }

    override fun getReviewById(
        reviewId: String,
        callback: (Boolean, String, ReviewModel?) -> Unit
    ) {
        try {
            ref.child(reviewId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        try {
                            val reviewMap = snapshot.value as? Map<String, Any?>
                            if (reviewMap != null) {
                                val review = ReviewModel.fromMap(reviewMap)
                                callback(true, "Review fetched successfully", review)
                            } else {
                                callback(false, "Invalid review data", null)
                            }
                        } catch (e: Exception) {
                            callback(false, "Failed to parse review data", null)
                        }
                    } else {
                        callback(false, "Review not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
        } catch (e: Exception) {
            callback(false, e.message ?: "An error occurred", null)
        }
    }

    override fun checkUserAlreadyReviewed(
        userId: String,
        companyId: String,
        callback: (Boolean, String, ReviewModel?) -> Unit
    ) {
        try {
            ref.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        try {
                            val reviewMap = childSnapshot.value as? Map<String, Any?>
                            if (reviewMap != null) {
                                val review = ReviewModel.fromMap(reviewMap)
                                if (review.companyId == companyId) {
                                    callback(true, "User has already reviewed this company", review)
                                    return
                                }
                            }
                        } catch (e: Exception) {
                            continue
                        }
                    }
                    callback(false, "User has not reviewed this company", null)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
        } catch (e: Exception) {
            callback(false, e.message ?: "An error occurred", null)
        }
    }

    override fun getAverageRating(
        companyId: String,
        callback: (Boolean, String, Double) -> Unit
    ) {
        try {
            ref.orderByChild("companyId").equalTo(companyId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ratings = mutableListOf<Int>()
                    for (childSnapshot in snapshot.children) {
                        try {
                            val reviewMap = childSnapshot.value as? Map<String, Any?>
                            if (reviewMap != null) {
                                val review = ReviewModel.fromMap(reviewMap)
                                ratings.add(review.rating)
                            }
                        } catch (e: Exception) {
                            continue
                        }
                    }

                    val averageRating = if (ratings.isNotEmpty()) {
                        ratings.average()
                    } else {
                        0.0
                    }

                    callback(true, "Average rating calculated successfully", averageRating)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, 0.0)
                }
            })
        } catch (e: Exception) {
            callback(false, e.message ?: "An error occurred", 0.0)
        }
    }

    override fun addReviewListener(
        companyId: String,
        onDataChange: (List<ReviewModel>) -> Unit,
        onError: (String) -> Unit
    ): String? {
        try {
            val listenerId = UUID.randomUUID().toString()
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reviews = mutableListOf<ReviewModel>()
                    for (childSnapshot in snapshot.children) {
                        try {
                            val reviewMap = childSnapshot.value as? Map<String, Any?>
                            if (reviewMap != null) {
                                val review = ReviewModel.fromMap(reviewMap)
                                reviews.add(review)
                            }
                        } catch (e: Exception) {
                            continue
                        }
                    }
                    // Sort by timestamp (newest first)
                    reviews.sortByDescending { it.timestamp }
                    onDataChange(reviews)
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error.message)
                }
            }

            listeners[listenerId] = listener
            ref.orderByChild("companyId").equalTo(companyId).addValueEventListener(listener)
            return listenerId
        } catch (e: Exception) {
            onError(e.message ?: "An error occurred")
            return null
        }
    }

    override fun removeReviewListener(listenerId: String?) {
        listenerId?.let { id ->
            listeners[id]?.let { listener ->
                ref.removeEventListener(listener)
                listeners.remove(id)
            }
        }
    }
}

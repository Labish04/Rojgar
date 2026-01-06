package com.example.rojgar.repository

import com.example.rojgar.model.FeedbackModel
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FeedbackRepoImpl : FeedbackRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val feedbackRef: DatabaseReference = database.getReference("Feedbacks")

    override suspend fun sendFeedback(feedback: FeedbackModel): Result<String> {
        return try {
            val feedbackId = feedbackRef.push().key ?: return Result.failure(Exception("Failed to generate ID"))
            val feedbackWithId = feedback.copy(id = feedbackId)

            feedbackRef.child(feedbackId).setValue(feedbackWithId).await()
            Result.success(feedbackId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllFeedbacks(): Flow<List<FeedbackModel>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val feedbacks = mutableListOf<FeedbackModel>()

                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val feedback = data.getValue(FeedbackModel::class.java)
                        if (feedback != null) {
                            feedbacks.add(feedback)
                        }
                    }
                }

                // Sort by timestamp descending (newest first)
                feedbacks.sortByDescending { it.timestamp }
                trySend(feedbacks)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        feedbackRef.addValueEventListener(listener)

        awaitClose {
            feedbackRef.removeEventListener(listener)
        }
    }

    override fun getFeedbacksByCompany(companyId: String): Flow<List<FeedbackModel>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val feedbacks = mutableListOf<FeedbackModel>()

                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val feedback = data.getValue(FeedbackModel::class.java)
                        if (feedback != null && feedback.companyId == companyId) {
                            feedbacks.add(feedback)
                        }
                    }
                }

                // Sort by timestamp descending (newest first)
                feedbacks.sortByDescending { it.timestamp }
                trySend(feedbacks)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        feedbackRef.addValueEventListener(listener)

        awaitClose {
            feedbackRef.removeEventListener(listener)
        }
    }
}
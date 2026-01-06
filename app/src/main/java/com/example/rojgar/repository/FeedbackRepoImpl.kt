package com.example.rojgar.repository

import com.example.rojgar.model.FeedbackModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FeedbackRepoImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : FeedbackRepo {

    private val feedbackCollection = firestore.collection("feedbacks")

    override suspend fun sendFeedback(feedback: FeedbackModel): Result<String> {
        return try {
            val docRef = feedbackCollection.document()
            val feedbackWithId = feedback.copy(id = docRef.id)
            docRef.set(feedbackWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllFeedbacks(): Flow<List<FeedbackModel>> = callbackFlow {
        val listener = feedbackCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val feedbacks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FeedbackModel::class.java)
                } ?: emptyList()

                trySend(feedbacks)
            }

        awaitClose { listener.remove() }
    }

    override fun getFeedbacksByCompany(companyId: String): Flow<List<FeedbackModel>> = callbackFlow {
        val listener = feedbackCollection
            .whereEqualTo("companyId", companyId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val feedbacks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FeedbackModel::class.java)
                } ?: emptyList()

                trySend(feedbacks)
            }

        awaitClose { listener.remove() }
    }
}
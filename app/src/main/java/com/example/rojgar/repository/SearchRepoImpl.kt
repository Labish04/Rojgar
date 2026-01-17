package com.example.rojgar.repository

import com.example.rojgar.model.SearchHistoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.Query

class SearchRepoImpl : SearchRepo {
    private val database = FirebaseDatabase.getInstance()
    private val searchRef = database.getReference("searches")

    override fun saveSearch(
        search: SearchHistoryModel,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            searchRef.child(search.searchId).setValue(search.toMap())
                .addOnSuccessListener {
                    callback(true, "Search saved successfully")
                }
                .addOnFailureListener { e ->
                    callback(false, "Failed to save search: ${e.message}")
                }
        } catch (e: Exception) {
            callback(false, "Error saving search: ${e.message}")
        }
    }

    override fun getSearchHistory(
        userId: String,
        userType: String,
        limit: Int,
        callback: (Boolean, String, List<SearchHistoryModel>?) -> Unit
    ) {
        try {
            searchRef.orderByChild("userId")
                .equalTo(userId)
                .limitToLast(limit)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val searches = mutableListOf<SearchHistoryModel>()

                        for (childSnapshot in snapshot.children) {
                            val search = childSnapshot.getValue(SearchHistoryModel::class.java)
                            if (search != null && search.userType == userType) {
                                searches.add(search)
                            }
                        }

                        // Sort by timestamp descending (most recent first)
                        searches.sortByDescending { it.timestamp }

                        callback(true, "Search history retrieved", searches)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        callback(false, "Failed to retrieve search history: ${error.message}", null)
                    }
                })
        } catch (e: Exception) {
            callback(false, "Error retrieving search history: ${e.message}", null)
        }
    }

    override fun deleteSearch(
        searchId: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            searchRef.child(searchId).removeValue()
                .addOnSuccessListener {
                    callback(true, "Search deleted successfully")
                }
                .addOnFailureListener { e ->
                    callback(false, "Failed to delete search: ${e.message}")
                }
        } catch (e: Exception) {
            callback(false, "Error deleting search: ${e.message}")
        }
    }

    override fun clearAllSearchHistory(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            searchRef.orderByChild("userId")
                .equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val updates = mutableMapOf<String, Any?>()
                        for (childSnapshot in snapshot.children) {
                            updates[childSnapshot.key!!] = null
                        }

                        if (updates.isEmpty()) {
                            callback(true, "No search history to clear")
                            return
                        }

                        searchRef.updateChildren(updates)
                            .addOnSuccessListener {
                                callback(true, "All search history cleared")
                            }
                            .addOnFailureListener { e ->
                                callback(false, "Failed to clear search history: ${e.message}")
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        callback(false, "Failed to clear search history: ${error.message}")
                    }
                })
        } catch (e: Exception) {
            callback(false, "Error clearing search history: ${e.message}")
        }
    }

    override fun getRecentSearches(
        userId: String,
        limit: Int,
        callback: (Boolean, String, List<SearchHistoryModel>?) -> Unit
    ) {
        try {
            searchRef.orderByChild("userId")
                .equalTo(userId)
                .limitToLast(limit)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val searches = mutableListOf<SearchHistoryModel>()

                        for (childSnapshot in snapshot.children) {
                            val search = childSnapshot.getValue(SearchHistoryModel::class.java)
                            if (search != null) {
                                searches.add(search)
                            }
                        }

                        // Sort by timestamp descending and take most recent
                        searches.sortByDescending { it.timestamp }

                        callback(true, "Recent searches retrieved", searches.take(limit))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        callback(false, "Failed to retrieve recent searches: ${error.message}", null)
                    }
                })
        } catch (e: Exception) {
            callback(false, "Error retrieving recent searches: ${e.message}", null)
        }
    }

    override fun updateSearchResultCount(
        searchId: String,
        resultCount: Int,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            searchRef.child(searchId).child("resultCount").setValue(resultCount)
                .addOnSuccessListener {
                    callback(true, "Result count updated")
                }
                .addOnFailureListener { e ->
                    callback(false, "Failed to update result count: ${e.message}")
                }
        } catch (e: Exception) {
            callback(false, "Error updating result count: ${e.message}")
        }
    }
    override fun deleteSearchHistory(
        userId: String,
        timestamp: Long,
        callback: (Boolean, String) -> Unit
    ) {
        val searchHistoryRef = database.getReference("SearchHistory")
            .child(userId)
            .orderByChild("timestamp")
            .equalTo(timestamp.toDouble())

        searchHistoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach { childSnapshot ->
                        childSnapshot.ref.removeValue()
                            .addOnSuccessListener {
                                callback(true, "Search history deleted")
                            }
                            .addOnFailureListener { exception ->
                                callback(false, "Failed to delete: ${exception.message}")
                            }
                    }
                } else {
                    callback(false, "Search history not found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Error: ${error.message}")
            }
        })
    }
}
package com.example.rojgar.repository

import com.example.rojgar.model.FollowModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class FollowRepoImpl : FollowRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val followsRef: DatabaseReference = database.getReference("Follows")
    private val statsRef: DatabaseReference = database.getReference("FollowStats")

    override fun follow(
        followerId: String,
        followerType: String,
        followingId: String,
        followingType: String,
        callback: (Boolean, String) -> Unit
    ) {
        // Use compound key for check
        val compoundKey = "$followerId-$followingId"

        val query = followsRef
            .orderByChild("followerId_followingId")
            .equalTo(compoundKey)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    callback(false, "Already following")
                    return
                }

                // Create new follow record
                val followId = UUID.randomUUID().toString()
                val follow = FollowModel(
                    followId = followId,
                    followerId = followerId,
                    followerType = followerType,
                    followingId = followingId,
                    followingType = followingType
                )

                // Save to follows
                followsRef.child(followId).setValue(follow.toMap())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Update follower's following count
                            incrementFollowingCount(followerId)
                            // Update following's followers count
                            incrementFollowersCount(followingId)
                            callback(true, "Followed successfully")
                        } else {
                            callback(false, task.exception?.message ?: "Failed to follow")
                        }
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun unfollow(
        followerId: String,
        followerType: String,
        followingId: String,
        followingType: String,
        callback: (Boolean, String) -> Unit
    ) {
        // Create compound key for direct lookup
        val compoundKey = "$followerId-$followingId"

        // Query using the compound key
        val query = followsRef
            .orderByChild("followerId_followingId")
            .equalTo(compoundKey)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback(false, "Not following")
                    return
                }

                // There should be only one match for the compound key
                var followToRemoveKey: String? = null

                for (followSnapshot in snapshot.children) {
                    val follow = followSnapshot.getValue(FollowModel::class.java)
                    if (follow != null &&
                        follow.followerId == followerId &&
                        follow.followingId == followingId &&
                        follow.followerType == followerType &&
                        follow.followingType == followingType) {
                        followToRemoveKey = followSnapshot.key
                        break
                    }
                }

                if (followToRemoveKey != null) {
                    followsRef.child(followToRemoveKey).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Decrement counts
                                decrementFollowingCount(followerId)
                                decrementFollowersCount(followingId)
                                callback(true, "Unfollowed successfully")
                            } else {
                                callback(false, task.exception?.message ?: "Failed to unfollow")
                            }
                        }
                } else {
                    callback(false, "Not following")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun isFollowing(
        followerId: String,
        followingId: String,
        callback: (Boolean) -> Unit
    ) {
        // Use compound key for more efficient query
        val compoundKey = "$followerId-$followingId"
        val query = followsRef
            .orderByChild("followerId_followingId")
            .equalTo(compoundKey)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isFollowing = false

                for (followSnapshot in snapshot.children) {
                    val follow = followSnapshot.getValue(FollowModel::class.java)
                    if (follow != null &&
                        follow.followerId == followerId &&
                        follow.followingId == followingId) {
                        isFollowing = true
                        break
                    }
                }

                callback(isFollowing)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }

    override fun getFollowersCount(
        userId: String,
        callback: (Int) -> Unit
    ) {
        // Use the stat counter instead of querying all follows
        statsRef.child(userId).child("followersCount")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.getValue(Int::class.java) ?: 0
                    callback(count)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Fallback to query if stat counter doesn't exist
                    val query = followsRef.orderByChild("followingId").equalTo(userId)
                    query.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            callback(snapshot.childrenCount.toInt())
                            // Initialize the stat counter
                            statsRef.child(userId).child("followersCount")
                                .setValue(snapshot.childrenCount.toInt())
                        }

                        override fun onCancelled(error: DatabaseError) {
                            callback(0)
                        }
                    })
                }
            })
    }

    override fun getFollowingCount(
        userId: String,
        callback: (Int) -> Unit
    ) {
        // Use the stat counter instead of querying all follows
        statsRef.child(userId).child("followingCount")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.getValue(Int::class.java) ?: 0
                    callback(count)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Fallback to query if stat counter doesn't exist
                    val query = followsRef.orderByChild("followerId").equalTo(userId)
                    query.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            callback(snapshot.childrenCount.toInt())
                            // Initialize the stat counter
                            statsRef.child(userId).child("followingCount")
                                .setValue(snapshot.childrenCount.toInt())
                        }

                        override fun onCancelled(error: DatabaseError) {
                            callback(0)
                        }
                    })
                }
            })
    }

    override fun getFollowers(
        userId: String,
        callback: (Boolean, String, List<FollowModel>?) -> Unit
    ) {
        val query = followsRef.orderByChild("followingId").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val followers = mutableListOf<FollowModel>()

                for (followSnapshot in snapshot.children) {
                    val follow = followSnapshot.getValue(FollowModel::class.java)
                    follow?.let { followers.add(it) }
                }

                callback(true, "Followers fetched", followers)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getFollowing(
        userId: String,
        callback: (Boolean, String, List<FollowModel>?) -> Unit
    ) {
        val query = followsRef.orderByChild("followerId").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val following = mutableListOf<FollowModel>()

                for (followSnapshot in snapshot.children) {
                    val follow = followSnapshot.getValue(FollowModel::class.java)
                    follow?.let { following.add(it) }
                }

                callback(true, "Following fetched", following)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    private fun incrementFollowingCount(userId: String) {
        statsRef.child(userId).child("followingCount")
            .runTransaction(object : com.google.firebase.database.Transaction.Handler {
                override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                    val currentCount = currentData.getValue(Int::class.java) ?: 0
                    currentData.value = currentCount + 1
                    return com.google.firebase.database.Transaction.success(currentData)
                }

                override fun onComplete(
                    error: com.google.firebase.database.DatabaseError?,
                    committed: Boolean,
                    currentData: com.google.firebase.database.DataSnapshot?
                ) {
                    // Optional: Log transaction completion
                }
            })
    }

    private fun incrementFollowersCount(userId: String) {
        statsRef.child(userId).child("followersCount")
            .runTransaction(object : com.google.firebase.database.Transaction.Handler {
                override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                    val currentCount = currentData.getValue(Int::class.java) ?: 0
                    currentData.value = currentCount + 1
                    return com.google.firebase.database.Transaction.success(currentData)
                }

                override fun onComplete(
                    error: com.google.firebase.database.DatabaseError?,
                    committed: Boolean,
                    currentData: com.google.firebase.database.DataSnapshot?
                ) {
                    // Optional: Log transaction completion
                }
            })
    }

    private fun decrementFollowingCount(userId: String) {
        statsRef.child(userId).child("followingCount")
            .runTransaction(object : com.google.firebase.database.Transaction.Handler {
                override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                    val currentCount = currentData.getValue(Int::class.java) ?: 0
                    if (currentCount > 0) {
                        currentData.value = currentCount - 1
                    }
                    return com.google.firebase.database.Transaction.success(currentData)
                }

                override fun onComplete(
                    error: com.google.firebase.database.DatabaseError?,
                    committed: Boolean,
                    currentData: com.google.firebase.database.DataSnapshot?
                ) {
                    // Optional: Log transaction completion
                }
            })
    }

    private fun decrementFollowersCount(userId: String) {
        statsRef.child(userId).child("followersCount")
            .runTransaction(object : com.google.firebase.database.Transaction.Handler {
                override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                    val currentCount = currentData.getValue(Int::class.java) ?: 0
                    if (currentCount > 0) {
                        currentData.value = currentCount - 1
                    }
                    return com.google.firebase.database.Transaction.success(currentData)
                }

                override fun onComplete(
                    error: com.google.firebase.database.DatabaseError?,
                    committed: Boolean,
                    currentData: com.google.firebase.database.DataSnapshot?
                ) {
                    // Optional: Log transaction completion
                }
            })
    }
}
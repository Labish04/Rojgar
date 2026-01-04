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
        // Check if already following
        val query = followsRef.orderByChild("followerId_followingId")
            .equalTo("$followerId-$followingId")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    callback(false, "Already following")
                } else {
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
        val query = followsRef.orderByChild("followerId_followingId")
            .equalTo("$followerId-$followingId")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.firstOrNull()?.let { followSnapshot ->
                        followSnapshot.ref.removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Decrement follower's following count
                                    decrementFollowingCount(followerId)
                                    // Decrement following's followers count
                                    decrementFollowersCount(followingId)
                                    callback(true, "Unfollowed successfully")
                                } else {
                                    callback(false, task.exception?.message ?: "Failed to unfollow")
                                }
                            }
                    } ?: callback(false, "Follow record not found")
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
        val query = followsRef.orderByChild("followerId_followingId")
            .equalTo("$followerId-$followingId")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.exists())
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
        val query = followsRef.orderByChild("followingId").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.childrenCount.toInt())
            }

            override fun onCancelled(error: DatabaseError) {
                callback(0)
            }
        })
    }

    override fun getFollowingCount(
        userId: String,
        callback: (Int) -> Unit
    ) {
        val query = followsRef.orderByChild("followerId").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.childrenCount.toInt())
            }

            override fun onCancelled(error: DatabaseError) {
                callback(0)
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
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentCount = snapshot.getValue(Int::class.java) ?: 0
                    statsRef.child(userId).child("followingCount").setValue(currentCount + 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Initialize if doesn't exist
                    statsRef.child(userId).child("followingCount").setValue(1)
                }
            })
    }

    private fun incrementFollowersCount(userId: String) {
        statsRef.child(userId).child("followersCount")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentCount = snapshot.getValue(Int::class.java) ?: 0
                    statsRef.child(userId).child("followersCount").setValue(currentCount + 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Initialize if doesn't exist
                    statsRef.child(userId).child("followersCount").setValue(1)
                }
            })
    }

    private fun decrementFollowingCount(userId: String) {
        statsRef.child(userId).child("followingCount")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentCount = snapshot.getValue(Int::class.java) ?: 0
                    if (currentCount > 0) {
                        statsRef.child(userId).child("followingCount").setValue(currentCount - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Do nothing
                }
            })
    }

    private fun decrementFollowersCount(userId: String) {
        statsRef.child(userId).child("followersCount")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentCount = snapshot.getValue(Int::class.java) ?: 0
                    if (currentCount > 0) {
                        statsRef.child(userId).child("followersCount").setValue(currentCount - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Do nothing
                }
            })
    }
}
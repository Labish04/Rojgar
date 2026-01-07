package com.example.rojgar.repository

import com.example.rojgar.model.BlockModel
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
    private val blocksRef: DatabaseReference = database.getReference("Blocks")

    override fun follow(
        followerId: String,
        followerType: String,
        followingId: String,
        followingType: String,
        callback: (Boolean, String) -> Unit
    ) {
        // Check if blocked first
        isBlocked(followerId, followingId) { isBlocked ->
            if (isBlocked) {
                callback(false, "Cannot follow a blocked user")
                return@isBlocked
            }

            // Check if already following
            val query = followsRef.orderByChild("followerId_followingId")
                .equalTo("${followerId}_$followingId")

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

                        // Save to follows with composite key
                        val followData = follow.toMap().toMutableMap()
                        followData["followerId_followingId"] = "${followerId}_$followingId"

                        followsRef.child(followId).setValue(followData)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    incrementFollowingCount(followerId)
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
    }

    override fun unfollow(
        followerId: String,
        followerType: String,
        followingId: String,
        followingType: String,
        callback: (Boolean, String) -> Unit
    ) {
        val query = followsRef.orderByChild("followerId_followingId")
            .equalTo("${followerId}_$followingId")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.firstOrNull()?.let { followSnapshot ->
                        followSnapshot.ref.removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    decrementFollowingCount(followerId)
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
            .equalTo("${followerId}_$followingId")

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
        statsRef.child(userId).child("followersCount")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.getValue(Int::class.java) ?: 0
                    callback(count)
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
        statsRef.child(userId).child("followingCount")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.getValue(Int::class.java) ?: 0
                    callback(count)
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

    override fun blockUser(
        blockerId: String,
        blockerType: String,
        blockedId: String,
        blockedType: String,
        callback: (Boolean, String) -> Unit
    ) {
        // Check if already blocked
        val query = blocksRef.orderByChild("blockerId_blockedId")
            .equalTo("${blockerId}_$blockedId")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    callback(false, "User already blocked")
                } else {
                    // Create new block record
                    val blockId = UUID.randomUUID().toString()
                    val block = BlockModel(
                        blockId = blockId,
                        blockerId = blockerId,
                        blockerType = blockerType,
                        blockedId = blockedId,
                        blockedType = blockedType
                    )

                    // Save to blocks with composite key
                    val blockData = block.toMap().toMutableMap()
                    blockData["blockerId_blockedId"] = "${blockerId}_$blockedId"

                    blocksRef.child(blockId).setValue(blockData)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Remove follow relationship if exists
                                removeFollowRelationship(blockerId, blockedId)
                                callback(true, "User blocked successfully")
                            } else {
                                callback(false, task.exception?.message ?: "Failed to block user")
                            }
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun unblockUser(
        blockerId: String,
        blockedId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val query = blocksRef.orderByChild("blockerId_blockedId")
            .equalTo("${blockerId}_$blockedId")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.firstOrNull()?.let { blockSnapshot ->
                        blockSnapshot.ref.removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    callback(true, "User unblocked successfully")
                                } else {
                                    callback(false, task.exception?.message ?: "Failed to unblock user")
                                }
                            }
                    } ?: callback(false, "Block record not found")
                } else {
                    callback(false, "User not blocked")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun isBlocked(
        blockerId: String,
        blockedId: String,
        callback: (Boolean) -> Unit
    ) {
        val query = blocksRef.orderByChild("blockerId_blockedId")
            .equalTo("${blockerId}_$blockedId")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }

    private fun removeFollowRelationship(userId1: String, userId2: String) {
        // Remove both directions
        val query1 = followsRef.orderByChild("followerId_followingId")
            .equalTo("${userId1}_$userId2")

        query1.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.firstOrNull()?.ref?.removeValue()
                    ?.addOnSuccessListener {
                        decrementFollowingCount(userId1)
                        decrementFollowersCount(userId2)
                    }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        val query2 = followsRef.orderByChild("followerId_followingId")
            .equalTo("${userId2}_$userId1")

        query2.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.firstOrNull()?.ref?.removeValue()
                    ?.addOnSuccessListener {
                        decrementFollowingCount(userId2)
                        decrementFollowersCount(userId1)
                    }
            }

            override fun onCancelled(error: DatabaseError) {}
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

                override fun onCancelled(error: DatabaseError) {}
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

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
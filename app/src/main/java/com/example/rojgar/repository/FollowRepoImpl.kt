package com.example.rojgar.repository

import android.content.Context
import android.util.Log
import com.example.rojgar.model.FollowModel
import com.example.rojgar.utils.NotificationHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class FollowRepoImpl(private val context: Context) : FollowRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val followsRef: DatabaseReference = database.getReference("Follows")
    private val statsRef: DatabaseReference = database.getReference("FollowStats")

    private val blockedRef: DatabaseReference = database.getReference("BlockedUsers")

    override fun follow(
        followerId: String,
        followerType: String,
        followingId: String,
        followingType: String,
        callback: (Boolean, String) -> Unit
    ) {
        // First check if there's a block between users
        checkMutualBlock(followerId, followingId) { isBlocked ->
            if (isBlocked) {
                callback(false, "Cannot follow. User has blocked you or you have blocked this user.")
                return@checkMutualBlock
            }

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
                                // Send notification - FIXED: Now passes context
                                sendFollowNotification(followerId, followerType, followingId, followingType)
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

    override fun blockUser(
        blockerId: String,
        blockedId: String,
        blockerType: String,
        blockedType: String,
        callback: (Boolean, String) -> Unit
    ) {
        // Validation: Cannot block yourself
        if (blockerId == blockedId) {
            callback(false, "You cannot block yourself")
            return
        }

        // Validation: Only JobSeeker can block JobSeeker
        if (blockerType != "JobSeeker" || blockedType != "JobSeeker") {
            callback(false, "Only job seekers can block other job seekers")
            return
        }

        // Check if already blocked
        blockedRef.child(blockerId).child(blockedId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    callback(false, "User is already blocked")
                    return@addOnSuccessListener
                }

                // Create block record
                val blockData = hashMapOf<String, Any>(
                    "blockerId" to blockerId,
                    "blockedId" to blockedId,
                    "blockerType" to blockerType,
                    "blockedType" to blockedType,
                    "blockedAt" to System.currentTimeMillis()
                )

                // Store in both directions for easy querying
                val updates = hashMapOf<String, Any>(
                    "$blockerId/$blockedId" to blockData,
                    "reverse/$blockedId/$blockerId" to true
                )

                blockedRef.updateChildren(updates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            unfollow(blockerId, "JobSeeker", blockedId, "JobSeeker") { _, _ ->
                            }
                            unfollow(blockedId, "JobSeeker", blockerId, "JobSeeker") { _, _ ->
                            }
                            callback(true, "User blocked successfully")
                        } else {
                            callback(false, task.exception?.message ?: "Failed to block user")
                        }
                    }
            }
            .addOnFailureListener { e ->
                callback(false, "Error: ${e.message}")
            }
    }

    override fun unblockUser(
        blockerId: String,
        blockedId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val updates = hashMapOf<String, Any?>(
            "$blockerId/$blockedId" to null,
            "reverse/$blockedId/$blockerId" to null
        )

        blockedRef.updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "User unblocked successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to unblock user")
                }
            }
    }

    override fun isUserBlocked(
        blockerId: String,
        blockedId: String,
        callback: (Boolean) -> Unit
    ) {
        blockedRef.child(blockerId).child(blockedId).get()
            .addOnSuccessListener { snapshot ->
                callback(snapshot.exists())
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    override fun hasBlockedYou(
        blockerId: String,
        blockedId: String,
        callback: (Boolean) -> Unit
    ) {
        // Check if the other user has blocked you (reverse lookup)
        blockedRef.child("reverse").child(blockedId).child(blockerId).get()
            .addOnSuccessListener { snapshot ->
                callback(snapshot.exists())
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    override fun checkMutualBlock(
        user1Id: String,
        user2Id: String,
        callback: (Boolean) -> Unit
    ) {
        // Check if either user has blocked the other
        val check1 = blockedRef.child(user1Id).child(user2Id).get()
        val check2 = blockedRef.child("reverse").child(user2Id).child(user1Id).get()

        com.google.android.gms.tasks.Tasks.whenAllComplete(check1, check2)
            .addOnCompleteListener { task ->
                val blocked1 = check1.result?.exists() ?: false
                val blocked2 = check2.result?.exists() ?: false
                callback(blocked1 || blocked2)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    override fun getBlockedUsers(
        jobSeekerId: String,
        callback: (Boolean, String, List<String>?) -> Unit
    ) {
        blockedRef.child(jobSeekerId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val blockedIds = mutableListOf<String>()
                    for (child in snapshot.children) {
                        child.key?.let { blockedIds.add(it) }
                    }
                    callback(true, "Blocked users fetched", blockedIds)
                } else {
                    callback(true, "No blocked users", emptyList())
                }
            }
            .addOnFailureListener { e ->
                callback(false, "Error: ${e.message}", null)
            }
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
                }
            })
    }

    // FIXED: Updated to send actual notifications
    private fun sendFollowNotification(
        followerId: String,
        followerType: String,
        followingId: String,
        followingType: String
    ) {
        // Get follower's name
        when (followerType) {
            "JobSeeker" -> {
                FirebaseDatabase.getInstance()
                    .getReference("JobSeekers")
                    .child(followerId)
                    .child("fullName")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val name = snapshot.getValue(String::class.java) ?: "Someone"
                        // Store notification in database
                        storeFollowNotification(followingId, followerId, followerType, name)
                        // FIXED: Send actual push notification
                        NotificationHelper.sendFollowNotification(
                            context,
                            followingId,
                            name,
                            followerId,
                            followerType
                        )
                    }
            }
            "Company" -> {
                FirebaseDatabase.getInstance()
                    .getReference("Companys")
                    .child(followerId)
                    .child("companyName")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val name = snapshot.getValue(String::class.java) ?: "A company"
                        // Store notification in database
                        storeFollowNotification(followingId, followerId, followerType, name)
                        // FIXED: Send actual push notification
                        NotificationHelper.sendFollowNotification(
                            context,
                            followingId,
                            name,
                            followerId,
                            followerType
                        )
                    }
            }
        }
    }

    private fun storeFollowNotification(
        receiverId: String,
        senderId: String,
        senderType: String,
        senderName: String
    ) {
        val notificationRef = FirebaseDatabase.getInstance()
            .getReference("Notifications")
            .child(receiverId)
            .push()

        val notificationId = notificationRef.key ?: ""
        val timestamp = System.currentTimeMillis()

        val notificationData = hashMapOf<String, Any>(
            "notificationId" to notificationId,
            "receiverId" to receiverId,
            "senderId" to senderId,
            "senderType" to senderType,
            "senderName" to senderName,
            "type" to "follow",
            "message" to "$senderName started following you",
            "timestamp" to timestamp,
            "isRead" to false
        )

        notificationRef.setValue(notificationData)
            .addOnSuccessListener {
                Log.d("FollowRepo", "Follow notification stored successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FollowRepo", "Failed to store follow notification: ${e.message}")
            }


    }
}
// File: MutualFollowHelper.kt
package com.example.rojgar.utils

import com.example.rojgar.model.FollowModel
import com.example.rojgar.repository.FollowRepo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MutualFollowHelper(private val followRepo: FollowRepo) {

    suspend fun checkMutualFollow(
        userId1: String,
        userId2: String
    ): Boolean {
        return try {
            // Check if user1 follows user2
            val user1FollowsUser2 = suspendCancellableCoroutine<Boolean> { continuation ->
                followRepo.isFollowing(userId1, userId2) { isFollowing ->
                    continuation.resume(isFollowing)
                }
            }

            // Check if user2 follows user1
            val user2FollowsUser1 = suspendCancellableCoroutine<Boolean> { continuation ->
                followRepo.isFollowing(userId2, userId1) { isFollowing ->
                    continuation.resume(isFollowing)
                }
            }

            user1FollowsUser2 && user2FollowsUser1
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getMutualFollowers(userId: String): List<String> {
        return try {
            // Get users that current user follows
            val followingUsers = suspendCancellableCoroutine<List<String>> { continuation ->
                followRepo.getFollowing(userId) { success, _, followingList ->
                    if (success && followingList != null) {
                        continuation.resume(followingList.map { it.followingId })
                    } else {
                        continuation.resume(emptyList())
                    }
                }
            }

            // Get users who follow current user
            val followersUsers = suspendCancellableCoroutine<List<String>> { continuation ->
                followRepo.getFollowers(userId) { success, _, followersList ->
                    if (success && followersList != null) {
                        continuation.resume(followersList.map { it.followerId })
                    } else {
                        continuation.resume(emptyList())
                    }
                }
            }

            // Find intersection (mutual follows)
            followingUsers.intersect(followersUsers).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Alternative simpler version without suspendCancellableCoroutine
    fun getMutualFollowersSimple(
        userId: String,
        callback: (List<String>) -> Unit
    ) {
        // Get users that current user follows
        followRepo.getFollowing(userId) { success1, _, followingList ->
            if (success1 && followingList != null) {
                val followingIds = followingList.map { it.followingId }

                // Get users who follow current user
                followRepo.getFollowers(userId) { success2, _, followersList ->
                    if (success2 && followersList != null) {
                        val followerIds = followersList.map { it.followerId }

                        // Find intersection
                        val mutualFollows = followingIds.intersect(followerIds).toList()
                        callback(mutualFollows)
                    } else {
                        callback(emptyList())
                    }
                }
            } else {
                callback(emptyList())
            }
        }
    }

    fun checkMutualFollowSimple(
        userId1: String,
        userId2: String,
        callback: (Boolean) -> Unit
    ) {
        var user1FollowsUser2 = false
        var user2FollowsUser1 = false

        // Check if user1 follows user2
        followRepo.isFollowing(userId1, userId2) { follows ->
            user1FollowsUser2 = follows

            // Check if user2 follows user1
            followRepo.isFollowing(userId2, userId1) { follows2 ->
                user2FollowsUser1 = follows2

                // Both must follow each other
                callback(user1FollowsUser2 && user2FollowsUser1)
            }
        }
    }
}
package com.example.rojgar.repository

import com.example.rojgar.model.FollowModel

interface FollowRepo {
    fun follow(
        followerId: String,
        followerType: String,
        followingId: String,
        followingType: String,
        callback: (Boolean, String) -> Unit
    )

    fun unfollow(
        followerId: String,
        followerType: String,
        followingId: String,
        followingType: String,
        callback: (Boolean, String) -> Unit
    )

    fun isFollowing(
        followerId: String,
        followingId: String,
        callback: (Boolean) -> Unit
    )

    fun getFollowersCount(
        userId: String,
        callback: (Int) -> Unit
    )

    fun getFollowingCount(
        userId: String,
        callback: (Int) -> Unit
    )

    fun getFollowers(
        userId: String,
        callback: (Boolean, String, List<FollowModel>?) -> Unit
    )

    fun getFollowing(
        userId: String,
        callback: (Boolean, String, List<FollowModel>?) -> Unit
    )

    fun blockUser(
        blockerId: String,
        blockedId: String,
        blockerType: String = "JobSeeker",
        blockedType: String = "JobSeeker",
        callback: (Boolean, String) -> Unit
    )

    fun unblockUser(
        blockerId: String,
        blockedId: String,
        callback: (Boolean, String) -> Unit
    )

    fun isUserBlocked(
        blockerId: String,
        blockedId: String,
        callback: (Boolean) -> Unit
    )

    fun hasBlockedYou(
        blockerId: String,
        blockedId: String,
        callback: (Boolean) -> Unit
    )

    fun checkMutualBlock(
        user1Id: String,
        user2Id: String,
        callback: (Boolean) -> Unit
    )

    fun getBlockedUsers(
        jobSeekerId: String,
        callback: (Boolean, String, List<String>?) -> Unit
    )

}
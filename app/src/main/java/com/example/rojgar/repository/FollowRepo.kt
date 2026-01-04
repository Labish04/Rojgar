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
}
package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rojgar.model.FollowModel
import com.example.rojgar.repository.FollowRepo
import kotlinx.coroutines.launch

class FollowViewModel(private val followRepo: FollowRepo) : ViewModel() {

    private val _isFollowing = MutableLiveData<Boolean>()
    val isFollowing: LiveData<Boolean> = _isFollowing

    private val _isBlocked = MutableLiveData<Boolean>()
    val isBlocked: LiveData<Boolean> = _isBlocked

    private val _followersCount = MutableLiveData<Int>(0)
    val followersCount: LiveData<Int> = _followersCount

    private val _followingCount = MutableLiveData<Int>(0)
    val followingCount: LiveData<Int> = _followingCount

    private val _followers = MutableLiveData<List<FollowModel>>()
    val followers: LiveData<List<FollowModel>> = _followers

    private val _following = MutableLiveData<List<FollowModel>>()
    val following: LiveData<List<FollowModel>> = _following

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    fun checkFollowStatus(followerId: String, followingId: String) {
        viewModelScope.launch {
            followRepo.isFollowing(followerId, followingId) { isFollowing ->
                _isFollowing.postValue(isFollowing)
            }
        }
    }

    fun checkBlockStatus(blockerId: String, blockedId: String) {
        viewModelScope.launch {
            followRepo.isBlocked(blockerId, blockedId) { isBlocked ->
                _isBlocked.postValue(isBlocked)
            }
        }
    }

    fun follow(
        followerId: String,
        followerType: String,
        followingId: String,
        followingType: String,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        _loading.postValue(true)
        viewModelScope.launch {
            followRepo.follow(followerId, followerType, followingId, followingType) { success, message ->
                _loading.postValue(false)
                if (success) {
                    _isFollowing.postValue(true)
                    getFollowersCount(followingId)
                    getFollowingCount(followerId)
                }
                onComplete(success, message)
            }
        }
    }

    fun unfollow(
        followerId: String,
        followerType: String,
        followingId: String,
        followingType: String,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        _loading.postValue(true)
        viewModelScope.launch {
            followRepo.unfollow(followerId, followerType, followingId, followingType) { success, message ->
                _loading.postValue(false)
                if (success) {
                    _isFollowing.postValue(false)
                    getFollowersCount(followingId)
                    getFollowingCount(followerId)
                }
                onComplete(success, message)
            }
        }
    }

    fun blockUser(
        blockerId: String,
        blockerType: String,
        blockedId: String,
        blockedType: String,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        _loading.postValue(true)
        viewModelScope.launch {
            followRepo.blockUser(blockerId, blockerType, blockedId, blockedType) { success, message ->
                _loading.postValue(false)
                if (success) {
                    _isBlocked.postValue(true)
                    _isFollowing.postValue(false)
                    getFollowersCount(blockedId)
                    getFollowingCount(blockerId)
                }
                onComplete(success, message)
            }
        }
    }

    fun unblockUser(
        blockerId: String,
        blockedId: String,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        _loading.postValue(true)
        viewModelScope.launch {
            followRepo.unblockUser(blockerId, blockedId) { success, message ->
                _loading.postValue(false)
                if (success) {
                    _isBlocked.postValue(false)
                }
                onComplete(success, message)
            }
        }
    }

    fun getFollowersCount(userId: String) {
        viewModelScope.launch {
            followRepo.getFollowersCount(userId) { count ->
                _followersCount.postValue(count)
            }
        }
    }

    fun getFollowingCount(userId: String) {
        viewModelScope.launch {
            followRepo.getFollowingCount(userId) { count ->
                _followingCount.postValue(count)
            }
        }
    }

    fun getFollowers(userId: String) {
        _loading.postValue(true)
        viewModelScope.launch {
            followRepo.getFollowers(userId) { success, message, followers ->
                _loading.postValue(false)
                if (success && followers != null) {
                    _followers.postValue(followers)
                }
            }
        }
    }

    fun getFollowing(userId: String) {
        _loading.postValue(true)
        viewModelScope.launch {
            followRepo.getFollowing(userId) { success, message, following ->
                _loading.postValue(false)
                if (success && following != null) {
                    _following.postValue(following)
                }
            }
        }
    }
}
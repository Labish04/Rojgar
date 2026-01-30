package com.example.rojgar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rojgar.model.GroupMember
import com.example.rojgar.repository.GroupChatRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GroupManagementViewModel(private val repository: GroupChatRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<GroupManagementUiState>(GroupManagementUiState.Idle)
    val uiState: StateFlow<GroupManagementUiState> = _uiState.asStateFlow()

    private val _members = MutableStateFlow<List<GroupMember>>(emptyList())
    val members: StateFlow<List<GroupMember>> = _members.asStateFlow()

    private val _leaveGroupResult = MutableStateFlow<LeaveGroupResult?>(null)
    val leaveGroupResult: StateFlow<LeaveGroupResult?> = _leaveGroupResult.asStateFlow()

    fun loadGroupMembers(groupId: String) {
        _uiState.value = GroupManagementUiState.Loading
        viewModelScope.launch {
            repository.getGroupMembersDetails(groupId) { result ->
                result.onSuccess { members ->
                    _members.value = members
                    _uiState.value = GroupManagementUiState.Success
                }.onFailure { error ->
                    _uiState.value = GroupManagementUiState.Error(error.message ?: "Failed to load members")
                }
            }
        }
    }

    fun leaveGroup(groupId: String, userId: String) {
        _uiState.value = GroupManagementUiState.Loading
        viewModelScope.launch {
            repository.leaveGroup(groupId, userId) { result ->
                result.onSuccess {
                    _leaveGroupResult.value = LeaveGroupResult.Success
                    _uiState.value = GroupManagementUiState.Success
                }.onFailure { error ->
                    _leaveGroupResult.value = LeaveGroupResult.Error(error.message ?: "Failed to leave group")
                    _uiState.value = GroupManagementUiState.Error(error.message ?: "Failed to leave group")
                }
            }
        }
    }

    fun updateMemberRole(groupId: String, memberId: String, role: String) {
        viewModelScope.launch {
            repository.updateGroupMemberRole(groupId, memberId, role) { result ->
                result.onSuccess {
                    // Refresh members list
                    loadGroupMembers(groupId)
                }.onFailure {
                    // Handle error silently for now
                }
            }
        }
    }

    fun updateMember(
        groupId: String,
        oldMemberId: String,
        newMemberId: String,
        newMemberName: String,
        newMemberPhoto: String
    ) {
        _uiState.value = GroupManagementUiState.Loading
        viewModelScope.launch {
            repository.updateGroupMember(
                groupId,
                oldMemberId,
                newMemberId,
                newMemberName,
                newMemberPhoto
            ) { result ->
                result.onSuccess {
                    // Refresh members list
                    loadGroupMembers(groupId)
                    _uiState.value = GroupManagementUiState.Success
                }.onFailure { error ->
                    _uiState.value = GroupManagementUiState.Error(error.message ?: "Failed to update member")
                }
            }
        }
    }

    fun clearLeaveGroupResult() {
        _leaveGroupResult.value = null
    }

    fun clearError() {
        if (_uiState.value is GroupManagementUiState.Error) {
            _uiState.value = GroupManagementUiState.Idle
        }
    }
}

sealed class GroupManagementUiState {
    object Idle : GroupManagementUiState()
    object Loading : GroupManagementUiState()
    object Success : GroupManagementUiState()
    data class Error(val message: String) : GroupManagementUiState()
}

sealed class LeaveGroupResult {
    object Success : LeaveGroupResult()
    data class Error(val message: String) : LeaveGroupResult()
}
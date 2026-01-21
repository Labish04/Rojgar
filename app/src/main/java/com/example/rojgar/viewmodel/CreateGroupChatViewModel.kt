package com.example.rojgar.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rojgar.model.MutualContact
import com.example.rojgar.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CreateGroupViewModel : ViewModel() {

    private lateinit var mutualContactsRepository: MutualContactsRepository
    private lateinit var groupChatRepository: GroupChatRepository
    private lateinit var userRepo: UserRepo
    private lateinit var followRepo: FollowRepo
    private lateinit var companyRepo: CompanyRepo
    private lateinit var jobSeekerRepo: JobSeekerRepo

    // Initialize repositories
    fun initRepositories(
        userRepo: UserRepo,
        followRepo: FollowRepo,
        companyRepo: CompanyRepo,
        jobSeekerRepo: JobSeekerRepo
    ) {
        this.userRepo = userRepo
        this.followRepo = followRepo
        this.companyRepo = companyRepo
        this.jobSeekerRepo = jobSeekerRepo

        // Initialize MutualContactsRepository
        mutualContactsRepository = MutualContactsRepositoryImpl().apply {
            initRepositories(followRepo, companyRepo, jobSeekerRepo)
        }

        // Initialize GroupChatRepository
        groupChatRepository = GroupChatRepositoryImpl()
    }

    // UI State
    private val _uiState = MutableStateFlow<CreateGroupUiState>(CreateGroupUiState.Idle)
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    private val _mutualContacts = MutableStateFlow<List<MutualContact>>(emptyList())
    val mutualContacts: StateFlow<List<MutualContact>> = _mutualContacts.asStateFlow()

    private val _selectedContacts = MutableStateFlow<Set<String>>(emptySet())
    val selectedContacts: StateFlow<Set<String>> = _selectedContacts.asStateFlow()

    private val _groupName = MutableStateFlow("")
    val groupName: StateFlow<String> = _groupName.asStateFlow()

    private val _groupImage = MutableStateFlow<String?>(null)
    val groupImage: StateFlow<String?> = _groupImage.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0.0)
    val uploadProgress: StateFlow<Double> = _uploadProgress.asStateFlow()

    private var currentUserId = ""
    private var currentUserName = ""
    private var currentUserType = ""

    fun loadCurrentUser() {
        currentUserId = userRepo.getCurrentUserId()

        userRepo.getUserType { userType ->
            currentUserType = userType ?: ""

            when (userType) {
                "Company" -> {
                    companyRepo.getCompanyById(currentUserId) { success, message, company ->
                        if (success && company != null) {
                            currentUserName = company.companyName
                        }
                    }
                }
                "JobSeeker" -> {
                    jobSeekerRepo.getJobSeekerById(currentUserId) { success, message, jobSeeker ->
                        if (success && jobSeeker != null) {
                            currentUserName = jobSeeker.fullName
                        }
                    }
                }
            }
        }
    }

    fun loadMutualContacts() {
        if (currentUserId.isEmpty()) {
            _uiState.value = CreateGroupUiState.Error("User not logged in")
            return
        }

        _uiState.value = CreateGroupUiState.Loading
        viewModelScope.launch {
            mutualContactsRepository.getMutualContacts(currentUserId) { result ->
                result.onSuccess { contacts ->
                    _mutualContacts.value = contacts
                    _uiState.value = CreateGroupUiState.Success
                }.onFailure { error ->
                    _uiState.value = CreateGroupUiState.Error(error.message ?: "Failed to load contacts")
                }
            }
        }
    }

    fun toggleContactSelection(userId: String) {
        val currentSelected = _selectedContacts.value.toMutableSet()
        if (currentSelected.contains(userId)) {
            currentSelected.remove(userId)
        } else {
            currentSelected.add(userId)
        }
        _selectedContacts.value = currentSelected

        // Update contact in list to reflect selection
        _mutualContacts.value = _mutualContacts.value.map { contact ->
            if (contact.userId == userId) {
                contact.copy(isSelected = !contact.isSelected)
            } else {
                contact
            }
        }
    }

    fun updateGroupName(name: String) {
        _groupName.value = name
    }

    fun uploadGroupImage(context: Context, imageUri: Uri) {
        _uiState.value = CreateGroupUiState.Uploading
        viewModelScope.launch {
            groupChatRepository.uploadGroupImage(
                context = context,
                imageUri = imageUri,
                onProgress = { progress ->
                    _uploadProgress.value = progress
                },
                onSuccess = { imageUrl ->
                    _groupImage.value = imageUrl
                    _uiState.value = CreateGroupUiState.Success
                },
                onFailure = { error ->
                    _uiState.value = CreateGroupUiState.Error("Image upload failed: $error")
                }
            )
        }
    }

    fun createGroup() {
        if (currentUserId.isEmpty() || currentUserName.isEmpty()) {
            _uiState.value = CreateGroupUiState.Error("User information not available")
            return
        }

        if (_groupName.value.isEmpty()) {
            _uiState.value = CreateGroupUiState.Error("Group name cannot be empty")
            return
        }

        if (_selectedContacts.value.isEmpty()) {
            _uiState.value = CreateGroupUiState.Error("Select at least one contact")
            return
        }

        val selectedContactsList = _mutualContacts.value.filter { it.isSelected }
        if (selectedContactsList.isEmpty()) {
            _uiState.value = CreateGroupUiState.Error("No contacts selected")
            return
        }

        _uiState.value = CreateGroupUiState.Loading
        viewModelScope.launch {
            groupChatRepository.createGroup(
                groupName = _groupName.value,
                groupImage = _groupImage.value ?: "",
                createdBy = currentUserId,
                createdByName = currentUserName,
                members = selectedContactsList.map { it.userId } + currentUserId,
                memberNames = selectedContactsList.map { it.userName } + currentUserName,
                memberPhotos = selectedContactsList.map { it.userPhoto } + (_groupImage.value ?: ""),
                callback = { result ->
                    result.onSuccess { groupId ->
                        _uiState.value = CreateGroupUiState.GroupCreated(groupId)
                        // Reset form
                        resetForm()
                    }.onFailure { error ->
                        _uiState.value = CreateGroupUiState.Error(error.message ?: "Failed to create group")
                    }
                }
            )
        }
    }

    fun clearError() {
        if (_uiState.value is CreateGroupUiState.Error) {
            _uiState.value = CreateGroupUiState.Idle
        }
    }

    private fun resetForm() {
        _groupName.value = ""
        _groupImage.value = null
        _selectedContacts.value = emptySet()
        _mutualContacts.value = _mutualContacts.value.map { it.copy(isSelected = false) }
    }
}

sealed class CreateGroupUiState {
    object Idle : CreateGroupUiState()
    object Loading : CreateGroupUiState()
    object Uploading : CreateGroupUiState()
    object Success : CreateGroupUiState()
    data class Error(val message: String) : CreateGroupUiState()
    data class GroupCreated(val groupId: String) : CreateGroupUiState()
}
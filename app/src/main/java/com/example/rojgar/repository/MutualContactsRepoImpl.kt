package com.example.rojgar.repository

import android.util.Log
import com.example.rojgar.model.MutualContact
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MutualContactsRepositoryImpl : MutualContactsRepository {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val companiesRef = database.getReference("Companys")
    private val jobSeekersRef = database.getReference("JobSeekers")

    private lateinit var followRepo: FollowRepo
    private lateinit var companyRepo: CompanyRepo
    private lateinit var jobSeekerRepo: JobSeekerRepo

    // Initialize repositories (called from ViewModel)
    fun initRepositories(
        followRepo: FollowRepo,
        companyRepo: CompanyRepo,
        jobSeekerRepo: JobSeekerRepo
    ) {
        this.followRepo = followRepo
        this.companyRepo = companyRepo
        this.jobSeekerRepo = jobSeekerRepo
    }

    override fun getMutualContacts(
        currentUserId: String,
        callback: (Result<List<MutualContact>>) -> Unit
    ) {
        if (!::followRepo.isInitialized) {
            callback(Result.failure(Exception("Repositories not initialized")))
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Step 1: Get users that current user is following
                val followingList = suspendCoroutine<List<Pair<String, String>>> { cont ->
                    followRepo.getFollowing(currentUserId) { success, message, following ->
                        if (success && following != null) {
                            val pairs = following.map { it.followingId to it.followingType }
                            cont.resume(pairs)
                        } else {
                            cont.resumeWithException(Exception(message ?: "Failed to get following"))
                        }
                    }
                }

                // Step 2: Check mutual follows
                val mutualContacts = mutableListOf<MutualContact>()
                val deferredResults = followingList.map { (userId, userType) ->
                    async {
                        val isMutualFollow = suspendCoroutine<Boolean> { cont ->
                            followRepo.isFollowing(userId, currentUserId) { isFollowing ->
                                cont.resume(isFollowing)
                            }
                        }

                        if (isMutualFollow) {
                            getUserDetailsSync(userId, userType)
                        } else {
                            null
                        }
                    }
                }

                // Wait for all async operations
                val results = deferredResults.awaitAll().filterNotNull()
                mutualContacts.addAll(results)

                // Return success
                withContext(Dispatchers.Main) {
                    callback(Result.success(mutualContacts))
                }

            } catch (e: Exception) {
                Log.e("MutualContactsRepo", "Error getting mutual contacts: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback(Result.failure(e))
                }
            }
        }
    }

    private suspend fun getUserDetailsSync(userId: String, userType: String): MutualContact? {
        return try {
            when (userType) {
                "Company" -> {
                    val company = suspendCoroutine<com.example.rojgar.model.CompanyModel?> { cont ->
                        companyRepo.getCompanyById(userId) { success, message, company ->
                            if (success && company != null) {
                                cont.resume(company)
                            } else {
                                cont.resume(null)
                            }
                        }
                    }
                    company?.let {
                        MutualContact(
                            userId = userId,
                            userType = userType,
                            userName = it.companyName,
                            userPhoto = it.companyProfileImage?: ""
                        )
                    }
                }
                "JobSeeker" -> {
                    val jobSeeker = suspendCoroutine<com.example.rojgar.model.JobSeekerModel?> { cont ->
                        jobSeekerRepo.getJobSeekerById(userId) { success, message, jobSeeker ->
                            if (success && jobSeeker != null) {
                                cont.resume(jobSeeker)
                            } else {
                                cont.resume(null)
                            }
                        }
                    }
                    jobSeeker?.let {
                        MutualContact(
                            userId = userId,
                            userType = userType,
                            userName = it.fullName,
                            userPhoto = it.profilePhoto ?: ""
                        )
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e("MutualContactsRepo", "Error getting user details: ${e.message}")
            null
        }
    }

    override fun getUserDetails(
        userId: String,
        userType: String,
        callback: (Result<MutualContact>) -> Unit
    ) {
        when (userType) {
            "Company" -> {
                companyRepo.getCompanyById(userId) { success, message, company ->
                    if (success && company != null) {
                        callback(Result.success(
                            MutualContact(
                                userId = userId,
                                userType = userType,
                                userName = company.companyName,
                                userPhoto = company.companyProfileImage ?: ""
                            )
                        ))
                    } else {
                        callback(Result.failure(Exception(message ?: "Failed to get company details")))
                    }
                }
            }
            "JobSeeker" -> {
                jobSeekerRepo.getJobSeekerById(userId) { success, message, jobSeeker ->
                    if (success && jobSeeker != null) {
                        callback(Result.success(
                            MutualContact(
                                userId = userId,
                                userType = userType,
                                userName = jobSeeker.fullName,
                                userPhoto = jobSeeker.profilePhoto ?: ""
                            )
                        ))
                    } else {
                        callback(Result.failure(Exception(message ?: "Failed to get job seeker details")))
                    }
                }
            }
            else -> callback(Result.failure(Exception("Unknown user type")))
        }
    }
}
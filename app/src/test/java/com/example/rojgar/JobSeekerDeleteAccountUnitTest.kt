package com.example.rojgar

import com.example.rojgar.repository.JobSeekerRepo
import com.example.rojgar.viewmodel.JobSeekerViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class JobSeekerDeleteAccountUnitTest {

    @Test
    fun jobseekerDeleteAccount_success_test() {
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Account deleted successfully")
            null
        }.whenever(repo).deleteAccount(eq("testUserId"), any())

        var successResult = false
        var messageResult = ""

        viewModel.deleteAccount("testUserId") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Account deleted successfully", messageResult)

        verify(repo).deleteAccount(eq("testUserId"), any())
    }

    }
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

class JobSeekerDeactivateAccountUnitTest {

    @Test
    fun jobseekerDeactivateAccount_success_test() {
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Account deactivated successfully")
            null
        }.whenever(repo).deactivateAccount(eq("testUserId"), any())

        var successResult = false
        var messageResult = ""

        viewModel.deactivateAccount("testUserId") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Account deactivated successfully", messageResult)

        verify(repo).deactivateAccount(eq("testUserId"), any())
    }

}
package com.example.rojgar

import com.example.rojgar.repository.JobSeekerRepo
import com.example.rojgar.viewmodel.JobSeekerViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class JobSeekerRegisterUnitTest {
    @Test
    fun jobseekerRegister_success_test() {
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(2)
            callback(true, "Register success", "testUserId")
            null
        }.`when`(repo).register(eq("test@gmail.com"), eq("123456"), any())

        var successResult = false
        var messageResult = ""

        viewModel.register("test@gmail.com", "123456") { success, msg, userId ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Register success", messageResult)

        verify(repo).register(eq("test@gmail.com"), eq("123456"), any())
    }
}
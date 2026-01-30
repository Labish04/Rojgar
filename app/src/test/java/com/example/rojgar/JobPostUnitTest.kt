package com.example.rojgar

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.rojgar.model.JobModel
import com.example.rojgar.repository.JobRepo
import com.example.rojgar.viewmodel.JobViewModel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class JobPostUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun createJobPost_success_test() {
        val repo = mock<JobRepo>()
        val viewModel = JobViewModel(repo)

        val jobPost = JobModel(
            postId = "test123",
            title = "Software Developer",
            companyId = "company123",
            position = "Senior Developer"
        )

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Job post created successfully")
            null
        }.`when`(repo).createJobPost(eq(jobPost), any())

        var successResult = false
        var messageResult = ""

        viewModel.createJobPost(jobPost) { success, message ->
            successResult = success
            messageResult = message
        }

        assertTrue(successResult)
        assertEquals("Job post created successfully", messageResult)

        verify(repo).createJobPost(eq(jobPost), any())
    }
}
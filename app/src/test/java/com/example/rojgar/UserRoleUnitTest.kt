package com.example.rojgar.repository

import com.example.rojgar.viewmodel.AuthViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UserRoleUnitTest{

    @Test
    fun loadUserRole_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = AuthViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(String?) -> Unit>(0)
            callback("Company")
            null
        }.`when`(repo).getCurrentUserRole(any())

        var roleResult: String? = null

        repo.getCurrentUserRole { role ->
            roleResult = role
        }

        assertTrue(roleResult != null)
        assertEquals("Company", roleResult)

        verify(repo).getCurrentUserRole(any())
    }
}
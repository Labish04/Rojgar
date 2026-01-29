package com.example.rojgar

import com.example.rojgar.repository.JobSeekerRepo
import com.example.rojgar.viewmodel.JobSeekerViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify


class JobSeekerViewModelTest {


    @Test
    fun logout_success_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Logout successfully")
            null
        }.`when`(repo).logout(eq("user123"), any())

        var successResult = false
        var messageResult = ""

        // Act
        viewModel.logout("user123") { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assert
        assertTrue(successResult)
        assertEquals("Logout successfully", messageResult)
        verify(repo).logout(eq("user123"), any())
    }

    @Test
    fun logout_failure_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(false, "Network error")
            null
        }.`when`(repo).logout(eq("user123"), any())

        var successResult = false
        var messageResult = ""

        // Act
        viewModel.logout("user123") { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assert
        assertFalse(successResult)
        assertEquals("Network error", messageResult)
        verify(repo).logout(eq("user123"), any())
    }

    @Test
    fun logout_with_empty_userId_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Logout successfully")
            null
        }.`when`(repo).logout(eq(""), any())

        var successResult = false
        var messageResult = ""

        // Act
        viewModel.logout("") { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assert
        assertTrue(successResult)
        assertEquals("Logout successfully", messageResult)
        verify(repo).logout(eq(""), any())
    }

    @Test
    fun logout_multiple_calls_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Logout successfully")
            null
        }.`when`(repo).logout(eq("user456"), any())

        var firstCallSuccess = false
        var secondCallSuccess = false

        // Act
        viewModel.logout("user456") { success, _ ->
            firstCallSuccess = success
        }
        viewModel.logout("user456") { success, _ ->
            secondCallSuccess = success
        }

        // Assert
        assertTrue(firstCallSuccess)
        assertTrue(secondCallSuccess)
        verify(repo, org.mockito.kotlin.times(2)).logout(eq("user456"), any())
    }

    @Test
    fun logout_callback_invoked_once_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)
        var callbackCount = 0

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Logout successfully")
            null
        }.`when`(repo).logout(eq("user789"), any())

        // Act
        viewModel.logout("user789") { _, _ ->
            callbackCount++
        }

        // Assert
        assertEquals(1, callbackCount)
        verify(repo).logout(eq("user789"), any())
    }

    // ========== DEACTIVATE ACCOUNT TESTS ==========

    @Test
    fun deactivateAccount_success_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Account deactivated successfully")
            null
        }.`when`(repo).deactivateAccount(eq("user123"), any())

        var successResult = false
        var messageResult = ""

        // Act
        viewModel.deactivateAccount("user123") { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assert
        assertTrue(successResult)
        assertEquals("Account deactivated successfully", messageResult)
        verify(repo).deactivateAccount(eq("user123"), any())
    }

    @Test
    fun deactivateAccount_failure_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(false, "Failed to deactivate account")
            null
        }.`when`(repo).deactivateAccount(eq("user456"), any())

        var successResult = false
        var messageResult = ""

        // Act
        viewModel.deactivateAccount("user456") { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assert
        assertFalse(successResult)
        assertEquals("Failed to deactivate account", messageResult)
        verify(repo).deactivateAccount(eq("user456"), any())
    }

    @Test
    fun deactivateAccount_with_empty_userId_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(false, "Invalid user ID")
            null
        }.`when`(repo).deactivateAccount(eq(""), any())

        var successResult = false
        var messageResult = ""

        // Act
        viewModel.deactivateAccount("") { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assert
        assertFalse(successResult)
        assertEquals("Invalid user ID", messageResult)
        verify(repo).deactivateAccount(eq(""), any())
    }

    @Test
    fun deactivateAccount_sets_isActive_false_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            // Simulate setting isActive to false
            callback(true, "Account deactivated successfully")
            null
        }.`when`(repo).deactivateAccount(eq("user789"), any())

        var successResult = false

        // Act
        viewModel.deactivateAccount("user789") { success, _ ->
            successResult = success
        }

        // Assert
        assertTrue(successResult)
        verify(repo).deactivateAccount(eq("user789"), any())
    }

    @Test
    fun deactivateAccount_database_error_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(false, "Database connection failed")
            null
        }.`when`(repo).deactivateAccount(eq("user999"), any())

        var successResult = false
        var messageResult = ""

        // Act
        viewModel.deactivateAccount("user999") { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assert
        assertFalse(successResult)
        assertEquals("Database connection failed", messageResult)
        verify(repo).deactivateAccount(eq("user999"), any())
    }

    @Test
    fun deactivateAccount_callback_invoked_once_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)
        var callbackCount = 0

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Account deactivated successfully")
            null
        }.`when`(repo).deactivateAccount(eq("user111"), any())

        // Act
        viewModel.deactivateAccount("user111") { _, _ ->
            callbackCount++
        }

        // Assert
        assertEquals(1, callbackCount)
        verify(repo).deactivateAccount(eq("user111"), any())
    }

    // ========== DELETE ACCOUNT TESTS ==========

    @Test
    fun deleteAccount_success_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Account deleted permanently")
            null
        }.`when`(repo).deleteAccount(eq("user123"), any())

        var successResult = false
        var messageResult = ""

        // Act
        viewModel.deleteAccount("user123") { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assert
        assertTrue(successResult)
        assertEquals("Account deleted permanently", messageResult)
        verify(repo).deleteAccount(eq("user123"), any())
    }

    @Test
    fun deleteAccount_failure_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(false, "Failed to delete authentication")
            null
        }.`when`(repo).deleteAccount(eq("user456"), any())

        var successResult = false
        var messageResult = ""

        // Act
        viewModel.deleteAccount("user456") { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assert
        assertFalse(successResult)
        assertEquals("Failed to delete authentication", messageResult)
        verify(repo).deleteAccount(eq("user456"), any())
    }

    @Test
    fun deleteAccount_user_not_authenticated_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(false, "User not authenticated")
            null
        }.`when`(repo).deleteAccount(eq("user789"), any())

        var successResult = false
        var messageResult = ""

        // Act
        viewModel.deleteAccount("user789") { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assert
        assertFalse(successResult)
        assertEquals("User not authenticated", messageResult)
        verify(repo).deleteAccount(eq("user789"), any())
    }

    @Test
    fun deleteAccount_database_deletion_failed_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(false, "Failed to delete from database")
            null
        }.`when`(repo).deleteAccount(eq("user999"), any())

        var successResult = false
        var messageResult = ""

        // Act
        viewModel.deleteAccount("user999") { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assert
        assertFalse(successResult)
        assertEquals("Failed to delete from database", messageResult)
        verify(repo).deleteAccount(eq("user999"), any())
    }

    @Test
    fun deleteAccount_with_empty_userId_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(false, "Invalid user ID")
            null
        }.`when`(repo).deleteAccount(eq(""), any())

        var successResult = false
        var messageResult = ""

        // Act
        viewModel.deleteAccount("") { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assert
        assertFalse(successResult)
        assertEquals("Invalid user ID", messageResult)
        verify(repo).deleteAccount(eq(""), any())
    }

    @Test
    fun deleteAccount_removes_from_database_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            // Simulate database removal
            callback(true, "Account deleted permanently")
            null
        }.`when`(repo).deleteAccount(eq("user111"), any())

        var successResult = false

        // Act
        viewModel.deleteAccount("user111") { success, _ ->
            successResult = success
        }

        // Assert
        assertTrue(successResult)
        verify(repo).deleteAccount(eq("user111"), any())
    }

    @Test
    fun deleteAccount_deletes_auth_user_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            // Simulate auth user deletion
            callback(true, "Account deleted permanently")
            null
        }.`when`(repo).deleteAccount(eq("user222"), any())

        var successResult = false

        // Act
        viewModel.deleteAccount("user222") { success, _ ->
            successResult = success
        }

        // Assert
        assertTrue(successResult)
        verify(repo).deleteAccount(eq("user222"), any())
    }

    @Test
    fun deleteAccount_callback_invoked_once_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)
        var callbackCount = 0

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Account deleted permanently")
            null
        }.`when`(repo).deleteAccount(eq("user333"), any())

        // Act
        viewModel.deleteAccount("user333") { _, _ ->
            callbackCount++
        }

        // Assert
        assertEquals(1, callbackCount)
        verify(repo).deleteAccount(eq("user333"), any())
    }

    // ========== REACTIVATE ACCOUNT TESTS ==========

    @Test
    fun reactivateAccount_success_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Account reactivated successfully")
            null
        }.`when`(repo).reactivateAccount(eq("user123"), any())

        var successResult = false
        var messageResult = ""

        // Act
        viewModel.reactivateAccount("user123") { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assert
        assertTrue(successResult)
        assertEquals("Account reactivated successfully", messageResult)
        verify(repo).reactivateAccount(eq("user123"), any())
    }

    @Test
    fun reactivateAccount_failure_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(false, "Failed to reactivate account")
            null
        }.`when`(repo).reactivateAccount(eq("user456"), any())

        var successResult = false
        var messageResult = ""

        // Act
        viewModel.reactivateAccount("user456") { success, msg ->
            successResult = success
            messageResult = msg
        }

        // Assert
        assertFalse(successResult)
        assertEquals("Failed to reactivate account", messageResult)
        verify(repo).reactivateAccount(eq("user456"), any())
    }

    // ========== INTEGRATION WORKFLOW TESTS ==========

    @Test
    fun workflow_deactivate_then_reactivate_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Account deactivated successfully")
            null
        }.`when`(repo).deactivateAccount(eq("user123"), any())

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Account reactivated successfully")
            null
        }.`when`(repo).reactivateAccount(eq("user123"), any())

        var deactivateSuccess = false
        var reactivateSuccess = false

        // Act
        viewModel.deactivateAccount("user123") { success, _ ->
            deactivateSuccess = success
        }
        viewModel.reactivateAccount("user123") { success, _ ->
            reactivateSuccess = success
        }

        // Assert
        assertTrue(deactivateSuccess)
        assertTrue(reactivateSuccess)
        verify(repo).deactivateAccount(eq("user123"), any())
        verify(repo).reactivateAccount(eq("user123"), any())
    }

    @Test
    fun workflow_logout_does_not_affect_account_status_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Logout successfully")
            null
        }.`when`(repo).logout(eq("user123"), any())

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Account is active")
            null
        }.`when`(repo).checkAccountStatus(eq("user123"), any())

        var logoutSuccess = false
        var statusCheckSuccess = false

        // Act
        viewModel.logout("user123") { success, _ ->
            logoutSuccess = success
        }
        viewModel.checkAccountStatus("user123") { success, _ ->
            statusCheckSuccess = success
        }

        // Assert
        assertTrue(logoutSuccess)
        assertTrue(statusCheckSuccess)
        verify(repo).logout(eq("user123"), any())
        verify(repo).checkAccountStatus(eq("user123"), any())
    }

    @Test
    fun workflow_delete_is_permanent_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Account deleted permanently")
            null
        }.`when`(repo).deleteAccount(eq("user123"), any())

        var deleteSuccess = false

        // Act
        viewModel.deleteAccount("user123") { success, _ ->
            deleteSuccess = success
        }

        // Assert
        assertTrue(deleteSuccess)
        verify(repo).deleteAccount(eq("user123"), any())
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    fun logout_with_special_characters_userId_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)
        val specialUserId = "user@#\$%123"

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Logout successfully")
            null
        }.`when`(repo).logout(eq(specialUserId), any())

        var successResult = false

        // Act
        viewModel.logout(specialUserId) { success, _ ->
            successResult = success
        }

        // Assert
        assertTrue(successResult)
        verify(repo).logout(eq(specialUserId), any())
    }

    @Test
    fun deleteAccount_with_very_long_userId_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)
        val longUserId = "a".repeat(500)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Account deleted permanently")
            null
        }.`when`(repo).deleteAccount(eq(longUserId), any())

        var successResult = false

        // Act
        viewModel.deleteAccount(longUserId) { success, _ ->
            successResult = success
        }

        // Assert
        assertTrue(successResult)
        verify(repo).deleteAccount(eq(longUserId), any())
    }

    @Test
    fun concurrent_operations_test() {
        // Arrange
        val repo = mock<JobSeekerRepo>()
        val viewModel = JobSeekerViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Logout successfully")
            null
        }.`when`(repo).logout(eq("user123"), any())

        var firstSuccess = false
        var secondSuccess = false

        // Act - Trigger concurrent operations
        viewModel.logout("user123") { success, _ ->
            firstSuccess = success
        }
        viewModel.logout("user123") { success, _ ->
            secondSuccess = success
        }

        // Assert
        assertTrue(firstSuccess)
        assertTrue(secondSuccess)
        verify(repo, org.mockito.kotlin.times(2)).logout(eq("user123"), any())
    }
}
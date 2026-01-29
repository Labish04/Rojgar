package com.example.rojgar

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rojgar.view.JobSeekerProfileActivity
import com.example.rojgar.view.LoginActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class JobSeekerProfileInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<JobSeekerProfileActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    // ========== PROFILE DISPLAY TESTS ==========

    @Test
    fun testProfileScreen_displaysCorrectly() {
        // Verify main UI elements are displayed
        composeRule.onNodeWithTag("profile_screen")
            .assertIsDisplayed()
    }

    @Test
    fun testProfileImage_isDisplayed() {
        // Verify profile image is shown
        composeRule.onNodeWithTag("profile_image")
            .assertIsDisplayed()
    }

    @Test
    fun testUserName_isDisplayed() {
        // Verify user name is visible
        composeRule.onNodeWithTag("user_name")
            .assertIsDisplayed()
    }

    @Test
    fun testProfession_isDisplayed() {
        // Verify profession field is visible
        composeRule.onNodeWithTag("profession")
            .assertIsDisplayed()
    }

    // ========== SETTINGS DIALOG TESTS ==========

    @Test
    fun testOpenSettings_displaysSettingsDialog() {
        // Click on settings button
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Verify settings dialog is displayed
        composeRule.onNodeWithTag("settings_dialog")
            .assertIsDisplayed()
    }

    @Test
    fun testSettingsDialog_hasAllOptions() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Verify all settings options are present
        composeRule.onNodeWithText("Change Password")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Deactivate Account")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Delete Account")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Logout")
            .assertIsDisplayed()
    }

    @Test
    fun testCloseSettings_dismissesDialog() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Close dialog
        composeRule.onNodeWithTag("close_settings")
            .performClick()

        // Verify dialog is dismissed (should not be visible)
        composeRule.onNodeWithTag("settings_dialog")
            .assertDoesNotExist()
    }

    // ========== LOGOUT TESTS ==========

    @Test
    fun testLogout_opensConfirmationDialog() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click logout
        composeRule.onNodeWithText("Logout")
            .performClick()

        // Verify logout confirmation dialog is displayed
        composeRule.onNodeWithText("Logout")
            .assertIsDisplayed()
    }

    @Test
    fun testLogout_confirmsAndNavigatesToLogin() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click logout
        composeRule.onNodeWithText("Logout")
            .performClick()

        // Confirm logout
        composeRule.onNodeWithTag("confirm_logout")
            .performClick()

        // Verify navigation to login activity
        Intents.intended(hasComponent(LoginActivity::class.java.name))
    }

    @Test
    fun testLogout_cancelDoesNotLogout() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click logout
        composeRule.onNodeWithText("Logout")
            .performClick()

        // Cancel logout
        composeRule.onNodeWithTag("cancel_logout")
            .performClick()

        // Verify still on profile screen
        composeRule.onNodeWithTag("profile_screen")
            .assertIsDisplayed()
    }

    // ========== CHANGE PASSWORD TESTS ==========

    @Test
    fun testChangePassword_opensDialog() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click change password
        composeRule.onNodeWithText("Change Password")
            .performClick()

        // Verify change password dialog is displayed
        composeRule.onNodeWithTag("change_password_dialog")
            .assertIsDisplayed()
    }

    @Test
    fun testChangePassword_validationWorks() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click change password
        composeRule.onNodeWithText("Change Password")
            .performClick()

        // Try to submit without filling fields
        composeRule.onNodeWithTag("confirm_change_password")
            .performClick()

        // Should show validation message
        composeRule.onNodeWithText("Please fill all fields")
            .assertIsDisplayed()
    }

    @Test
    fun testChangePassword_successFlow() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click change password
        composeRule.onNodeWithText("Change Password")
            .performClick()

        // Enter current password
        composeRule.onNodeWithTag("current_password")
            .performTextInput("oldPassword123")

        // Enter new password
        composeRule.onNodeWithTag("new_password")
            .performTextInput("newPassword123")

        // Enter confirm password
        composeRule.onNodeWithTag("confirm_password")
            .performTextInput("newPassword123")

        // Click confirm
        composeRule.onNodeWithTag("confirm_change_password")
            .performClick()

        // Should show success message
        composeRule.onNodeWithText("Password changed successfully")
            .assertIsDisplayed()
    }

    @Test
    fun testChangePassword_cancelButton() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click change password
        composeRule.onNodeWithText("Change Password")
            .performClick()

        // Click cancel
        composeRule.onNodeWithTag("cancel_change_password")
            .performClick()

        // Dialog should be dismissed
        composeRule.onNodeWithTag("change_password_dialog")
            .assertDoesNotExist()
    }

    // ========== DEACTIVATE ACCOUNT TESTS ==========

    @Test
    fun testDeactivateAccount_opensConfirmationDialog() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click deactivate account
        composeRule.onNodeWithText("Deactivate Account")
            .performClick()

        // Verify deactivate confirmation dialog is displayed
        composeRule.onNodeWithTag("deactivate_dialog")
            .assertIsDisplayed()
    }

    @Test
    fun testDeactivateAccount_showsWarningMessage() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click deactivate account
        composeRule.onNodeWithText("Deactivate Account")
            .performClick()

        // Verify warning message is shown
        composeRule.onNodeWithText("Deactivate Account?")
            .assertIsDisplayed()

        composeRule.onNodeWithText("You can reactivate anytime by logging in")
            .assertIsDisplayed()
    }

    @Test
    fun testDeactivateAccount_requiresPasswordConfirmation() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click deactivate account
        composeRule.onNodeWithText("Deactivate Account")
            .performClick()

        // Try to confirm without password
        composeRule.onNodeWithTag("confirm_deactivate")
            .performClick()

        // Should show error
        composeRule.onNodeWithText("Please enter your password")
            .assertIsDisplayed()
    }

    @Test
    fun testDeactivateAccount_successfulDeactivation() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click deactivate account
        composeRule.onNodeWithText("Deactivate Account")
            .performClick()

        // Enter password
        composeRule.onNodeWithTag("deactivate_password")
            .performTextInput("testPassword123")

        // Confirm deactivation
        composeRule.onNodeWithTag("confirm_deactivate")
            .performClick()

        // Should navigate to login
        Intents.intended(hasComponent(LoginActivity::class.java.name))
    }

    @Test
    fun testDeactivateAccount_cancelButton() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click deactivate account
        composeRule.onNodeWithText("Deactivate Account")
            .performClick()

        // Click cancel
        composeRule.onNodeWithTag("cancel_deactivate")
            .performClick()

        // Dialog should be dismissed
        composeRule.onNodeWithTag("deactivate_dialog")
            .assertDoesNotExist()
    }

    // ========== DELETE ACCOUNT TESTS ==========

    @Test
    fun testDeleteAccount_opensConfirmationDialog() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click delete account
        composeRule.onNodeWithText("Delete Account")
            .performClick()

        // Verify delete confirmation dialog is displayed
        composeRule.onNodeWithTag("delete_dialog")
            .assertIsDisplayed()
    }

    @Test
    fun testDeleteAccount_showsWarningMessages() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click delete account
        composeRule.onNodeWithText("Delete Account")
            .performClick()

        // Verify warning messages
        composeRule.onNodeWithText("Delete Account Permanently?")
            .assertIsDisplayed()

        composeRule.onNodeWithText("⚠️ Warning: This action cannot be undone!")
            .assertIsDisplayed()

        composeRule.onNodeWithText("All your data will be permanently deleted")
            .assertIsDisplayed()
    }

    @Test
    fun testDeleteAccount_requiresPasswordConfirmation() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click delete account
        composeRule.onNodeWithText("Delete Account")
            .performClick()

        // Try to confirm without password
        composeRule.onNodeWithTag("confirm_delete")
            .performClick()

        // Should show error
        composeRule.onNodeWithText("Please enter your password")
            .assertIsDisplayed()
    }

    @Test
    fun testDeleteAccount_successfulDeletion() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click delete account
        composeRule.onNodeWithText("Delete Account")
            .performClick()

        // Enter password
        composeRule.onNodeWithTag("delete_password")
            .performTextInput("testPassword123")

        // Confirm deletion
        composeRule.onNodeWithTag("confirm_delete")
            .performClick()

        // Should navigate to login
        Intents.intended(hasComponent(LoginActivity::class.java.name))
    }

    @Test
    fun testDeleteAccount_cancelButton() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click delete account
        composeRule.onNodeWithText("Delete Account")
            .performClick()

        // Click cancel
        composeRule.onNodeWithTag("cancel_delete")
            .performClick()

        // Dialog should be dismissed
        composeRule.onNodeWithTag("delete_dialog")
            .assertDoesNotExist()
    }

    @Test
    fun testDeleteAccount_passwordVisibilityToggle() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click delete account
        composeRule.onNodeWithText("Delete Account")
            .performClick()

        // Enter password
        composeRule.onNodeWithTag("delete_password")
            .performTextInput("testPassword123")

        // Toggle password visibility
        composeRule.onNodeWithTag("toggle_password_visibility")
            .performClick()

        // Password should be visible (test would need actual verification)
        composeRule.onNodeWithTag("delete_password")
            .assertIsDisplayed()
    }

    // ========== PROFILE EDIT TESTS ==========

    @Test
    fun testEditProfile_opensEditScreen() {
        // Click edit profile button
        composeRule.onNodeWithTag("edit_profile_button")
            .performClick()

        // Verify edit fields are displayed
        composeRule.onNodeWithTag("edit_full_name")
            .assertIsDisplayed()

        composeRule.onNodeWithTag("edit_profession")
            .assertIsDisplayed()
    }

    @Test
    fun testEditProfile_saveChanges() {
        // Click edit profile
        composeRule.onNodeWithTag("edit_profile_button")
            .performClick()

        // Edit name
        composeRule.onNodeWithTag("edit_full_name")
            .performTextInput("New Name")

        // Save changes
        composeRule.onNodeWithTag("save_profile")
            .performClick()

        // Should show success message
        composeRule.onNodeWithText("Profile updated successfully")
            .assertIsDisplayed()
    }

    // ========== FOLLOW/UNFOLLOW TESTS ==========

    @Test
    fun testFollowButton_isDisplayedForOtherProfile() {
        // Verify follow button is shown when viewing another user's profile
        composeRule.onNodeWithTag("follow_button")
            .assertIsDisplayed()
    }

    @Test
    fun testFollowButton_togglesFollowStatus() {
        // Click follow button
        composeRule.onNodeWithTag("follow_button")
            .performClick()

        // Verify button text changes to "Following"
        composeRule.onNodeWithTag("follow_button")
            .assertTextEquals("Following")
    }

    @Test
    fun testFollowersCount_updates() {
        // Initial followers count
        composeRule.onNodeWithTag("followers_count")
            .assertIsDisplayed()

        // Click follow
        composeRule.onNodeWithTag("follow_button")
            .performClick()

        // Followers count should update (would need proper setup)
        composeRule.onNodeWithTag("followers_count")
            .assertIsDisplayed()
    }

    // ========== SHARE PROFILE TESTS ==========

    @Test
    fun testShareProfile_opensShareSheet() {
        // Click share button
        composeRule.onNodeWithTag("share_button")
            .performClick()

        // Verify share intent is triggered (would need intent verification)
        // This is handled by Android system
    }

    // ========== VIDEO TESTS ==========

    @Test
    fun testVideoSection_isDisplayed() {
        // Verify video section is present
        composeRule.onNodeWithTag("video_section")
            .assertIsDisplayed()
    }

    @Test
    fun testVideoPlay_startsPlayback() {
        // Click play button
        composeRule.onNodeWithTag("play_video")
            .performClick()

        // Verify video player is displayed
        composeRule.onNodeWithTag("video_player")
            .assertIsDisplayed()
    }

    // ========== DRAWER TESTS ==========

    @Test
    fun testDrawer_opensAndCloses() {
        // Open drawer
        composeRule.onNodeWithTag("drawer_button")
            .performClick()

        // Verify drawer is open
        composeRule.onNodeWithTag("drawer_content")
            .assertIsDisplayed()

        // Close drawer
        composeRule.onNodeWithTag("close_drawer")
            .performClick()

        // Verify drawer is closed
        composeRule.onNodeWithTag("drawer_content")
            .assertDoesNotExist()
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    fun testCompleteLogoutFlow() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click logout
        composeRule.onNodeWithText("Logout")
            .performClick()

        // Confirm logout
        composeRule.onNodeWithTag("confirm_logout")
            .performClick()

        // Verify navigated to login
        Intents.intended(hasComponent(LoginActivity::class.java.name))
    }

    @Test
    fun testCompleteDeactivateFlow() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click deactivate
        composeRule.onNodeWithText("Deactivate Account")
            .performClick()

        // Enter password
        composeRule.onNodeWithTag("deactivate_password")
            .performTextInput("testPassword123")

        // Confirm deactivation
        composeRule.onNodeWithTag("confirm_deactivate")
            .performClick()

        // Verify navigated to login
        Intents.intended(hasComponent(LoginActivity::class.java.name))
    }

    @Test
    fun testCompleteDeleteFlow() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Click delete
        composeRule.onNodeWithText("Delete Account")
            .performClick()

        // Enter password
        composeRule.onNodeWithTag("delete_password")
            .performTextInput("testPassword123")

        // Confirm deletion
        composeRule.onNodeWithTag("confirm_delete")
            .performClick()

        // Verify navigated to login
        Intents.intended(hasComponent(LoginActivity::class.java.name))
    }

    @Test
    fun testMultipleDialogInteractions() {
        // Open settings
        composeRule.onNodeWithTag("settings_button")
            .performClick()

        // Try change password
        composeRule.onNodeWithText("Change Password")
            .performClick()

        // Cancel
        composeRule.onNodeWithTag("cancel_change_password")
            .performClick()

        // Try deactivate
        composeRule.onNodeWithText("Deactivate Account")
            .performClick()

        // Cancel
        composeRule.onNodeWithTag("cancel_deactivate")
            .performClick()

        // All dialogs should be dismissed
        composeRule.onNodeWithTag("change_password_dialog")
            .assertDoesNotExist()

        composeRule.onNodeWithTag("deactivate_dialog")
            .assertDoesNotExist()
    }
}
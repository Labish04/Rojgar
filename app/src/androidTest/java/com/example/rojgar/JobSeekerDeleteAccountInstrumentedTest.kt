package com.example.rojgar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rojgar.view.LoginActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JobSeekerDeleteAccountInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<LoginActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    // Given Login Screen
    // When job seeker logs in and deletes account
    // Then user should be redirected to Login screen
    @Test
    fun testDeleteAccount_success_navigatesToLogin() {

        // Enter email
        composeRule.onNodeWithTag("email")
            .assertIsDisplayed()
            .performTextInput("kashishshah421@gmail.com")

        // Enter password
        composeRule.onNodeWithTag("password")
            .assertIsDisplayed()
            .performTextInput("kashish321")

        // Click Login
        composeRule.onNodeWithTag("login")
            .assertIsDisplayed()
            .performClick()

        // Click Delete Account button
        composeRule.onNodeWithTag("deleteAccount")
            .assertIsDisplayed()
            .performClick()

        // Confirm Delete Account
        composeRule.onNodeWithTag("confirmDelete")
            .assertIsDisplayed()
            .performClick()

        // Verify navigation back to LoginActivity
        Intents.intended(
            hasComponent(LoginActivity::class.java.name)
        )
    }
}

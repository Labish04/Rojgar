package com.example.rojgar

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import com.example.rojgar.view.LoginActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JobSeekerDeactivateAccountInstrumentedTest {

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
    // When user logs in and deactivates account
    // Then user should be able to deactivate account successfully
    @Test
    fun testDeactivateAccount_success_navigatesToLogin() {
        // Enter email
        composeRule.onNodeWithTag("email")
            .performTextInput("kashishshah421@gmail.com")

        // Enter password
        composeRule.onNodeWithTag("password")
            .performTextInput("kashish321")

        // Click Login
        composeRule.onNodeWithTag("login")
            .performClick()

        Thread.sleep(3000)

        // Click on deactivate account button
        composeRule.onNodeWithTag("deactivateAccount")
            .performClick()

        Thread.sleep(2000)

        // Confirm deactivation
        composeRule.onNodeWithTag("confirmDeactivate")
            .performClick()

        Thread.sleep(3000)

        // Verify navigation back to Login screen
        Intents.intended(hasComponent(LoginActivity::class.java.name))
    }
}
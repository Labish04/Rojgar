package com.example.rojgar

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import com.example.rojgar.view.JobSeekerDashboardActivity
import com.example.rojgar.view.LoginActivity
import com.example.rojgar.view.RegisterAsActivity
import org.junit.After

class CompanyRegisterInstrumentedTes {
    @get:Rule
    val composeRule = createAndroidComposeRule<RegisterAsActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    // Given RegisterAs Screen
    // When user clicks on jobseeker, enter full name, email, password, confirm password and clicks on signup button
    // Then user should be navigated to LoginActivity

    @Test
    fun testSuccessfulLogin_navigatesToDashboard() {
        // Click Jobseeker card
        composeRule.onNodeWithTag("company")
            .performClick()

        // Enter Full Name
        composeRule.onNodeWithTag("companyName")
            .performTextInput("Test User")

        // Enter Phone Number
        composeRule.onNodeWithTag("phoneNumber")
            .performTextInput("1234567890")

        // Enter Email
        composeRule.onNodeWithTag("email")
            .performTextInput("test@gmail.com")

        // Enter password
        composeRule.onNodeWithTag("password")
            .performTextInput("test123")

        // Enter password
        composeRule.onNodeWithTag("confirmPassword")
            .performTextInput("test123")

        // Click signup
        composeRule.onNodeWithTag("signup")
            .performClick()

        Thread.sleep(3000)
        Intents.intended(hasComponent(LoginActivity::class.java.name))
    }
}
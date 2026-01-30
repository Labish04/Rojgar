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
import org.junit.After

@RunWith(AndroidJUnit4::class)
class JobSeekerLoginInstrumentedTest {
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
    // When user enter email, password and clicks on login button
    // Then user should be navigated to JobSeekerDashboardActivity

    @Test
    fun testSuccessfulLogin_navigatesToDashboard() {
        // Enter email
        composeRule.onNodeWithTag("email")
            .performTextInput("bbhawana131@gmail.com")

//        // Enter password
        composeRule.onNodeWithTag("password")
            .performTextInput("bhawana123")

        // Click Login
        composeRule.onNodeWithTag("login")
            .performClick()

        Thread.sleep(3000)
        Intents.intended(hasComponent(JobSeekerDashboardActivity::class.java.name))
    }

}
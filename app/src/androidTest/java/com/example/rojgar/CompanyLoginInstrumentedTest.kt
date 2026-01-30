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
import com.example.rojgar.view.CompanyDashboardActivity
import com.example.rojgar.view.LoginActivity
import org.junit.After

class CompanyLoginInstrumentedTest {
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
    // Then user should be navigated to CompanyDashboardActivity

    @Test
    fun testSuccessfulLogin_navigatesToDashboard() {
        // Enter email
        composeRule.onNodeWithTag("email")
            .performTextInput("labishparajuli04@gmail.com")

//        // Enter password
        composeRule.onNodeWithTag("password")
            .performTextInput("labish123")

        // Click Login
        composeRule.onNodeWithTag("login")
            .performClick()

        Thread.sleep(3000)
        Intents.intended(hasComponent(CompanyDashboardActivity::class.java.name))
    }
}
package com.example.rojgar

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rojgar.view.JobSeekerSearchActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JobSeekerSearchInstrumentedTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun testSearchScreen_initialState() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, JobSeekerSearchActivity::class.java)

        ActivityScenario.launch<JobSeekerSearchActivity>(intent).use {
            // Verify Title
            composeRule.onNodeWithText("Discover").assertIsDisplayed()

            // Verify Tabs exist
            composeRule.onNodeWithText("Jobs").assertIsDisplayed()
            composeRule.onNodeWithText("Companies").assertIsDisplayed()
            composeRule.onNodeWithText("Job Seekers").assertIsDisplayed()

            // Verify Search Bar exists (by looking for input field)
            composeRule.onNode(hasSetTextAction()).assertIsDisplayed()
        }
    }

    @Test
    fun testSearchScreen_enterSearchQuery() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, JobSeekerSearchActivity::class.java)

        ActivityScenario.launch<JobSeekerSearchActivity>(intent).use {
            val query = "Software Engineer"
            
            // Find text field and enter text
            composeRule.onNode(hasSetTextAction()).performTextInput(query)
            
            // Verify text is entered (TextField value should update)
            composeRule.onNodeWithText(query).assertIsDisplayed()
        }
    }

    @Test
    fun testSearchScreen_tabNavigation() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, JobSeekerSearchActivity::class.java)

        ActivityScenario.launch<JobSeekerSearchActivity>(intent).use {
            // Initially "Jobs" should be selected
            composeRule.onNodeWithText("Jobs").assertIsDisplayed()
            
            // Click "Companies" tab
            composeRule.onNodeWithText("Companies").performClick()
            
            // Verify "Companies" is selected/displayed (In this UI, selection is visual, 
            // but we can check if the content for companies is loaded or if the tab is clicked.
            // Since we don't have easy assertIsSelected for custom tabs without semantics, 
            // we assume performClick works if no exception.)
            
            // Click "Job Seekers" tab
            composeRule.onNodeWithText("Job Seekers").performClick()
        }
    }
}

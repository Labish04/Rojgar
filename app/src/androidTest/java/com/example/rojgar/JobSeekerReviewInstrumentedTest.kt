package com.example.rojgar

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rojgar.view.JobSeekerReviewActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JobSeekerReviewInstrumentedTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun testReviewLifecycle_createEditDelete() {
        // Prepare Intent with Company ID
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, JobSeekerReviewActivity::class.java).apply {
            putExtra("COMPANY_ID", "test_company_instrumented_test")
        }

        // Launch Activity
        ActivityScenario.launch<JobSeekerReviewActivity>(intent).use {
            
            // 1. Verify Screen Title
            composeRule.onNodeWithText("Company Reviews").assertIsDisplayed()

            // Note: This test assumes the user is logged in. 
            // If the "Write Your Review" button is not visible, it might be because the user already has a review
            // or there is some other state issue. 
            // For a robust test, we would ensure a clean state (no review) before starting.
            
            // 2. Write a Review
            // Click "Write Your Review" button
            // If this fails, ensure you are logged in and haven't reviewed this company yet.
            if (composeRule.onAllNodes(hasText("Write Your Review")).fetchSemanticsNodes().isNotEmpty()) {
                composeRule.onNodeWithText("Write Your Review").performClick()

                // Set Rating (e.g., 4 stars)
                composeRule.onNodeWithContentDescription("Star 4").performClick()

                // Enter Review Text
                // Finding the text field by its ability to accept text
                val reviewText = "This is an automated test review."
                composeRule.onNode(hasSetTextAction()).performTextInput(reviewText)

                // Click Submit
                composeRule.onNodeWithText("Submit").performClick()

                // Wait for the review to appear (simple wait by looking for node)
                composeRule.waitUntil(timeoutMillis = 5000) {
                    composeRule.onAllNodes(hasText(reviewText)).fetchSemanticsNodes().isNotEmpty()
                }

                // Verify review exists
                composeRule.onNodeWithText(reviewText).assertIsDisplayed()
            } else {
                // If "Write Your Review" is not present, maybe we already have a review.
                // We will try to find an existing review to edit.
                // In a real test, we should ensure clean state.
            }

            // 3. Edit the Review
            // Click "More options" on the review card
            composeRule.onNodeWithContentDescription("More options").performClick()

            // Click "Edit" from the dropdown menu
            composeRule.onNodeWithText("Edit").performClick()

            // Verify Edit Dialog appears
            composeRule.onNodeWithText("Edit Review").assertIsDisplayed()

            // Change Text
            val updatedText = "This is an updated automated test review."
            composeRule.onNode(hasSetTextAction()).performTextInput(updatedText)

            // Click Update
            composeRule.onNodeWithText("Update").performClick()

            // Wait for update
            composeRule.waitUntil(timeoutMillis = 5000) {
                composeRule.onAllNodes(hasText(updatedText)).fetchSemanticsNodes().isNotEmpty()
            }

            // Verify updated text exists
            composeRule.onNodeWithText(updatedText).assertIsDisplayed()

            // 4. Delete the Review
            // Click "More options"
            composeRule.onNodeWithContentDescription("More options").performClick()

            // Click "Delete"
            composeRule.onNodeWithText("Delete").performClick()

            // Verify review is gone
            // We wait until the node with text is no longer present
            composeRule.waitUntil(timeoutMillis = 5000) {
                composeRule.onAllNodes(hasText(updatedText)).fetchSemanticsNodes().isEmpty()
            }
            
            // Confirm it's gone
            composeRule.onNodeWithText(updatedText).assertDoesNotExist()
            
            // Verify "Write Your Review" button is back (since we deleted our review)
            composeRule.onNodeWithText("Write Your Review").assertIsDisplayed()
        }
    }
}

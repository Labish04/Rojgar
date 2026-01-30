package com.example.rojgar

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rojgar.view.CompanyUploadPost
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JobPostInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<CompanyUploadPost>()

    /**
     * Given: Company Upload Post Screen
     * When: User clicks "Create Job Post" button, fills in required fields, and submits
     * Then: Job post should be created successfully
     */
    @Test
    fun createJobPost_success_test() {
        // Wait for the screen to load
        composeRule.waitForIdle()

        // Click on "Create Job Post" button to show the form
        // Using test tag instead of text search
        composeRule.onNodeWithTag("Create Job Post")
            .performClick()

        // Wait for form to appear
        composeRule.waitForIdle()

        // Fill in Job Title (required field) - using test tag
        composeRule.onNodeWithTag("title")
            .performTextInput("Software Developer")

        // Fill in Position (required field) - using test tag
        composeRule.onNodeWithTag("position")
            .performTextInput("Senior Developer")

        // Click on categories dropdown
        composeRule.onNodeWithTag("categories")
            .performClick()

        composeRule.waitForIdle()

        // Select a category from the bottom sheet
        composeRule.onNodeWithText("IT and Networking", useUnmergedTree = true)
            .performClick()

        // Click Done button in the category bottom sheet
        composeRule.onNodeWithText("Done", useUnmergedTree = true)
            .performClick()

        composeRule.waitForIdle()

        // Fill in Experience
        composeRule.onNodeWithTag("experience")
            .performTextInput("3-5 years")

        // Click on job type dropdown
        composeRule.onNodeWithTag("jobType")
            .performClick()

        composeRule.waitForIdle()

        // Select job type from bottom sheet
        composeRule.onNodeWithText("Full-time", useUnmergedTree = true)
            .performClick()

        // Click Select button in job type bottom sheet
        composeRule.onNodeWithText("Select", useUnmergedTree = true)
            .performClick()

        composeRule.waitForIdle()

        // Fill in Education
        composeRule.onNodeWithTag("education")
            .performTextInput("Bachelor's Degree in Computer Science")

        // Fill in Skills
        composeRule.onNodeWithTag("skills")
            .performTextInput("Java, Kotlin, Android Development")

        // Fill in Salary
        composeRule.onNodeWithTag("salary")
            .performTextInput("Rs. 60,000 - 80,000")

        // Fill in Key Responsibilities
        composeRule.onNodeWithTag("responsibilities")
            .performTextInput("Develop and maintain Android applications")

        // Fill in Job Description
        composeRule.onNodeWithTag("jobDescription")
            .performTextInput("We are looking for an experienced Android developer to join our team")

        // Click Post Job button using test tag
        composeRule.onNodeWithTag("post")
            .performClick()

        // Wait for the post to be created
        composeRule.waitForIdle()
        Thread.sleep(3000)

        // Verify success by checking if we're back to the job list screen
        // The "Create Job Post" button should be visible again after successful post
        composeRule.onNodeWithTag("Create Job Post")
            .assertExists()
    }
}
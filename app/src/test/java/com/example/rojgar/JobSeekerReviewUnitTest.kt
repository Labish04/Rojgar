package com.example.rojgar

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.rojgar.model.ReviewModel
import com.example.rojgar.repository.ReviewRepo
import com.example.rojgar.viewmodel.ReviewViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class JobSeekerReviewUnitTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    @Test
    fun addReview_success_test() {
        val repo = mock<ReviewRepo>()
        val viewModel = ReviewViewModel(repo)
        val review = ReviewModel(reviewId = "1", userId = "user1", companyId = "comp1", rating = 5, reviewText = "Great")

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Review added")
            null
        }.`when`(repo).addReview(eq(review), any())

        viewModel.addReview(review)

        assertEquals("Review added", viewModel.toastMessage.value)
        assertEquals(review, viewModel.userReview.value)
        verify(repo).addReview(eq(review), any())
    }

    @Test
    fun loadReviews_success_test() {
        val repo = mock<ReviewRepo>()
        val viewModel = ReviewViewModel(repo)
        val review1 = ReviewModel(reviewId = "1", userId = "user1", companyId = "comp1", rating = 5, reviewText = "Great")
        val review2 = ReviewModel(reviewId = "2", userId = "user2", companyId = "comp1", rating = 4, reviewText = "Good")
        val reviews = listOf(review1, review2)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, List<ReviewModel>?) -> Unit>(1)
            callback(true, "Success", reviews)
            null
        }.`when`(repo).getReviewsByCompanyId(eq("comp1"), any())

        viewModel.loadReviews("comp1")

        assertEquals(reviews, viewModel.reviews.value)
        assertEquals(4.5, viewModel.averageRating.value)
        verify(repo).getReviewsByCompanyId(eq("comp1"), any())
    }

    @Test
    fun deleteReview_success_test() {
        val repo = mock<ReviewRepo>()
        val viewModel = ReviewViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Review deleted")
            null
        }.`when`(repo).deleteReview(eq("review1"), any())

        viewModel.deleteReview("review1", "comp1")

        assertEquals("Review deleted", viewModel.toastMessage.value)
        assertEquals(null, viewModel.userReview.value)
        verify(repo).deleteReview(eq("review1"), any())
    }

    @Test
    fun updateReview_success_test() {
        val repo = mock<ReviewRepo>()
        val viewModel = ReviewViewModel(repo)
        val review = ReviewModel(reviewId = "1", userId = "user1", companyId = "comp1", rating = 4, reviewText = "Updated")

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Review updated")
            null
        }.`when`(repo).updateReview(eq(review), any())

        viewModel.updateReview(review)

        assertEquals("Review updated", viewModel.toastMessage.value)
        assertEquals(review, viewModel.userReview.value)
        verify(repo).updateReview(eq(review), any())
    }
}

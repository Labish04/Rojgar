package com.example.rojgar.model

object ReportCategories {
    val CATEGORIES = listOf(
        ReportCategory("spam", "Spam or Misleading Content", "Company is posting spam or misleading information"),
        ReportCategory("inappropriate", "Inappropriate Content", "Offensive, abusive, or inappropriate content"),
        ReportCategory("fake", "Fake or Impersonation", "Company is pretending to be someone else"),
        ReportCategory("harassment", "Harassment or Bullying", "Company is harassing or bullying users"),
        ReportCategory("illegal", "Illegal Activities", "Company is involved in illegal activities"),
        ReportCategory("scam", "Scam or Fraud", "Company is running scams or fraudulent activities"),
        ReportCategory("privacy", "Privacy Violation", "Company is violating privacy or sharing personal data"),
        ReportCategory("other", "Other Issue", "Other type of issue not listed above")
    )

    data class ReportCategory(
        val id: String,
        val title: String,
        val description: String
    )
}
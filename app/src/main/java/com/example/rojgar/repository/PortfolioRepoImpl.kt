// File: PortfolioRepoImpl.kt
package com.example.rojgar.repository

import com.example.rojgar.model.PortfolioModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PortfolioRepoImpl : PortfolioRepo {

    private val database = FirebaseDatabase.getInstance()
    private val portfoliosRef: DatabaseReference = database.getReference("Portfolios")

    override fun addPortfolio(
        portfolio: PortfolioModel,
        callback: (Boolean, String) -> Unit
    ) {
        val portfolioId = portfoliosRef.push().key ?: System.currentTimeMillis().toString()
        val portfolioWithId = portfolio.copy(portfolioId = portfolioId)

        portfoliosRef.child(portfolioId).setValue(portfolioWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Portfolio added successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to add portfolio")
                }
            }
    }

    override fun getPortfoliosByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<PortfolioModel>?) -> Unit
    ) {
        portfoliosRef.orderByChild("jobSeekerId").equalTo(jobSeekerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val portfolioList = mutableListOf<PortfolioModel>()

                    if (snapshot.exists()) {
                        for (portfolioSnapshot in snapshot.children) {
                            val portfolio = portfolioSnapshot.getValue(PortfolioModel::class.java)
                            portfolio?.let {
                                portfolioList.add(it)
                            }
                        }
                        callback(true, "Portfolios fetched", portfolioList)
                    } else {
                        callback(true, "No portfolios found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun updatePortfolio(
        portfolioId: String,
        updatedPortfolio: PortfolioModel,
        callback: (Boolean, String) -> Unit
    ) {
        portfoliosRef.child(portfolioId).updateChildren(updatedPortfolio.toMap())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Portfolio updated successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to update portfolio")
                }
            }
    }

    override fun deletePortfolio(
        portfolioId: String,
        callback: (Boolean, String) -> Unit
    ) {
        portfoliosRef.child(portfolioId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Portfolio deleted successfully")
                } else {
                    callback(false, task.exception?.message ?: "Failed to delete portfolio")
                }
            }
    }

    override fun getPortfolioById(
        portfolioId: String,
        callback: (Boolean, String, PortfolioModel?) -> Unit
    ) {
        portfoliosRef.child(portfolioId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val portfolio = snapshot.getValue(PortfolioModel::class.java)
                        callback(true, "Portfolio fetched", portfolio)
                    } else {
                        callback(false, "Portfolio not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            }
        )
    }
}
package com.example.rojgar.repository

import com.example.rojgar.model.PortfolioModel

interface PortfolioRepo {
    fun addPortfolio(
        portfolio: PortfolioModel,
        callback: (Boolean, String) -> Unit
    )

    fun getPortfoliosByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<PortfolioModel>?) -> Unit
    )

    fun updatePortfolio(
        portfolioId: String,
        updatedPortfolio: PortfolioModel,
        callback: (Boolean, String) -> Unit
    )

    fun deletePortfolio(
        portfolioId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getPortfolioById(
        portfolioId: String,
        callback: (Boolean, String, PortfolioModel?) -> Unit
    )
}
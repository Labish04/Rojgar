// File: PortfolioViewModel.kt
package com.example.rojgar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rojgar.model.EducationModel
import com.example.rojgar.model.PortfolioModel
import com.example.rojgar.repository.PortfolioRepo
import kotlinx.coroutines.launch

class PortfolioViewModel(private val portfolioRepo: PortfolioRepo) : ViewModel() {
    private val _portfolio = MutableLiveData< PortfolioModel?>()
    val portfolio: LiveData<PortfolioModel?> = _portfolio

    private val _allPortfolios = MutableLiveData<List<PortfolioRepo>?>()
    val allPortfolios : MutableLiveData<List<PortfolioRepo>?> get() = _allPortfolios

    private val _portfolios = MutableLiveData<List<PortfolioModel>>()
    val portfolios: MutableLiveData<List<PortfolioModel>> get() = _portfolios

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: MutableLiveData<String?> get() = _error

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: MutableLiveData<String?> get() = _successMessage

    fun addPortfolio(portfolio: PortfolioModel, callback: (Boolean, String) -> Unit) {
        _loading.value = true
        portfolioRepo.addPortfolio(portfolio) { success, message ->
            _loading.value = false
            if (success) {
                _successMessage.value = message
                loadPortfoliosByJobSeekerId(portfolio.jobSeekerId)
            } else {
                _error.value = message
            }
            callback(success, message)
        }
    }

    fun getPortfoliosByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<PortfolioModel>?) -> Unit = { _, _, _ -> }
    ) {
        loadPortfoliosByJobSeekerId(jobSeekerId, callback)
    }

    private fun loadPortfoliosByJobSeekerId(
        jobSeekerId: String,
        callback: (Boolean, String, List<PortfolioModel>?) -> Unit = { _, _, _ -> }
    ) {
        _loading.value = true
        portfolioRepo.getPortfoliosByJobSeekerId(jobSeekerId) { success, message, portfolioList ->
            _loading.value = false
            if (success) {
                _portfolios.value = portfolioList ?: emptyList()
                _error.value = null
            } else {
                _error.value = message
                _portfolios.value = emptyList()
            }
            callback(success, message, portfolioList)
        }
    }

    fun updatePortfolio(
        portfolioId: String,
        updatedPortfolio: PortfolioModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        portfolioRepo.updatePortfolio(portfolioId, updatedPortfolio) { success, message ->
            _loading.value = false
            if (success) {
                _successMessage.value = message
                loadPortfoliosByJobSeekerId(updatedPortfolio.jobSeekerId)
            } else {
                _error.value = message
            }
            callback(success, message)
        }
    }

    fun deletePortfolio(
        portfolioId: String,
        jobSeekerId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.value = true
        portfolioRepo.deletePortfolio(portfolioId) { success, message ->
            _loading.value = false
            if (success) {
                _successMessage.value = message
                loadPortfoliosByJobSeekerId(jobSeekerId)
            } else {
                _error.value = message
            }
            callback(success, message)
        }
    }

    fun getPortfolioById(
        portfolioId: String,
        callback: (Boolean, String, PortfolioModel?) -> Unit
    ) {
        _loading.value = true
        portfolioRepo.getPortfolioById(portfolioId) { success, message, portfolio ->
            _loading.value = false
            if (success) {
                _error.value = null
            } else {
                _error.value = message
            }
            callback(success, message, portfolio)
        }
    }

    fun clearMessages() {
        _successMessage.value = null
        _error.value = null
    }


}
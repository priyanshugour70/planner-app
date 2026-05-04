package com.lssgoo.planner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.lssgoo.planner.data.model.Budget
import com.lssgoo.planner.data.model.Transaction
import com.lssgoo.planner.data.repository.DataRepository
import com.lssgoo.planner.features.finance.models.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : BaseViewModel(application) {

    private val repository = DataRepository(application)

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets.asStateFlow()

    init {
        loadFinance()
    }

    fun loadFinance() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _transactions.value = repository.getTransactions()
            _budgets.value = repository.getBudgets()
            _isLoading.value = false
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createTransaction(transaction)
            loadFinance()
        }
    }

    fun getTransactionsByType(type: TransactionType): List<Transaction> =
        _transactions.value.filter { it.type == type }

    fun getTotalIncome(): Double =
        _transactions.value.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

    fun getTotalExpense(): Double =
        _transactions.value.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    fun getBalance(): Double = getTotalIncome() - getTotalExpense()

    fun getUnsettledDebts(): List<Transaction> =
        _transactions.value.filter {
            (it.type == TransactionType.BORROWED || it.type == TransactionType.LENT) && !it.isSettled
        }
}

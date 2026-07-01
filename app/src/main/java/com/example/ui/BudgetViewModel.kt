package com.example.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository
    private val savingsRepository: SavingsGoalRepository
    private val metaItemDao: MetaItemDao
    private val budgetLimitDao: BudgetLimitDao

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepository(database.transactionDao())
        savingsRepository = SavingsGoalRepository(database.savingsGoalDao())
        metaItemDao = database.metaItemDao()
        budgetLimitDao = database.budgetLimitDao()
        viewModelScope.launch(Dispatchers.IO) {
            val items = metaItemDao.getAllMetaItems().firstOrNull() ?: emptyList()
            if (items.isEmpty()) {
                val defaults = listOf(
                    MetaItem(name = "Uang Saku", type = ItemType.INCOME_CATEGORY, colorHex = "#00BFA5"),
                    MetaItem(name = "Investasi", type = ItemType.INCOME_CATEGORY, colorHex = "#2196F3"),
                    MetaItem(name = "Lainnya", type = ItemType.INCOME_CATEGORY, colorHex = "#9E9E9E"),
                    
                    MetaItem(name = "Makanan & Minuman", type = ItemType.EXPENSE_CATEGORY, colorHex = "#FF5252"),
                    MetaItem(name = "Transportasi", type = ItemType.EXPENSE_CATEGORY, colorHex = "#FFC107"),
                    MetaItem(name = "Belanja harian", type = ItemType.EXPENSE_CATEGORY, colorHex = "#8BC34A"),
                    MetaItem(name = "Keperluan Kuliah", type = ItemType.EXPENSE_CATEGORY, colorHex = "#3F51B5"),
                    MetaItem(name = "Tagihan & Pulsa", type = ItemType.EXPENSE_CATEGORY, colorHex = "#9C27B0"),
                    MetaItem(name = "Hiburan", type = ItemType.EXPENSE_CATEGORY, colorHex = "#FF9800"),
                    MetaItem(name = "Lainnya", type = ItemType.EXPENSE_CATEGORY, colorHex = "#9E9E9E"),
                    
                    MetaItem(name = "Cash", type = ItemType.METHOD, colorHex = "#4CAF50"),
                    MetaItem(name = "Wondr", type = ItemType.METHOD, colorHex = "#00BFA5"),
                    MetaItem(name = "Dana", type = ItemType.METHOD, colorHex = "#2196F3"),
                    MetaItem(name = "GoPay", type = ItemType.METHOD, colorHex = "#00A5CF"),
                    MetaItem(name = "Lainnya", type = ItemType.METHOD, colorHex = "#9E9E9E")
                )
                defaults.forEach { metaItemDao.insertMetaItem(it) }
            }
        }
    }

    val metaItems: StateFlow<List<MetaItem>> = metaItemDao.getAllMetaItems()
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addMetaItem(name: String, type: ItemType, colorHex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            metaItemDao.insertMetaItem(MetaItem(name = name, type = type, colorHex = colorHex))
        }
    }

    fun updateMetaItem(item: MetaItem) {
        viewModelScope.launch(Dispatchers.IO) {
            metaItemDao.updateMetaItem(item)
        }
    }

    fun deleteMetaItem(item: MetaItem) {
        viewModelScope.launch(Dispatchers.IO) {
            metaItemDao.deleteMetaItem(item)
        }
    }

    // Budget Limits
    val budgetLimits: StateFlow<List<BudgetLimit>> = budgetLimitDao.getAllBudgetLimits()
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setBudgetLimit(methodName: String, amount: Double?, percentage: Double?) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = budgetLimitDao.getLimitByMethod(methodName)
            if (existing != null) {
                budgetLimitDao.updateBudgetLimit(existing.copy(limitAmount = amount, limitPercentage = percentage))
            } else {
                budgetLimitDao.insertBudgetLimit(BudgetLimit(methodName = methodName, limitAmount = amount, limitPercentage = percentage))
            }
        }
    }

    fun deleteBudgetLimit(limit: BudgetLimit) {
        viewModelScope.launch(Dispatchers.IO) {
            budgetLimitDao.deleteBudgetLimit(limit)
        }
    }

    // State: All Transactions from Repository
    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allSavingsGoals: StateFlow<List<SavingsGoal>> = savingsRepository.allGoals
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSavingsGoal(name: String, targetAmount: Double, targetDateMillis: Long, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            savingsRepository.insert(SavingsGoal(name = name, targetAmount = targetAmount, targetDateMillis = targetDateMillis, category = category))
        }
    }

    fun addFundsToGoal(goal: SavingsGoal, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = goal.copy(currentAmount = goal.currentAmount + amount)
            savingsRepository.update(updated)
            // Log as an expense in transactions indicating transfer to savings
            val transaction = Transaction(
                type = TransactionType.PENGELUARAN,
                amount = amount,
                paymentMethod = "Lainnya",
                category = "Tabungan",
                notes = "Alokasi ke tabungan: ${goal.name}",
                timestamp = System.currentTimeMillis()
            )
            repository.insert(transaction)
        }
    }

    fun subtractFundsFromGoal(goal: SavingsGoal, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = goal.copy(currentAmount = goal.currentAmount - amount)
            savingsRepository.update(updated)
            // Log as an income in transactions indicating transfer back to balance
            val transaction = Transaction(
                type = TransactionType.PEMASUKAN,
                amount = amount,
                paymentMethod = "Lainnya",
                category = "Tabungan",
                notes = "Menarik dana tabungan: ${goal.name}",
                timestamp = System.currentTimeMillis()
            )
            repository.insert(transaction)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch(Dispatchers.IO) {
            if (goal.currentAmount > 0) {
                val transaction = Transaction(
                    type = TransactionType.PEMASUKAN,
                    amount = goal.currentAmount,
                    paymentMethod = "Lainnya",
                    category = "Tabungan",
                    notes = "Pencairan tabungan yang dihapus: ${goal.name}",
                    timestamp = System.currentTimeMillis()
                )
                repository.insert(transaction)
            }
            savingsRepository.delete(goal)
        }
    }

    fun updateSavingsGoal(goal: SavingsGoal, newName: String, newTarget: Double, newDateMillis: Long?, newCategory: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = goal.copy(
                name = newName,
                targetAmount = newTarget,
                targetDateMillis = newDateMillis ?: goal.targetDateMillis,
                category = newCategory
            )
            savingsRepository.update(updated)
        }
    }

    fun deleteSavingsGoalDirectly(goal: SavingsGoal) {
        viewModelScope.launch(Dispatchers.IO) {
            savingsRepository.delete(goal)
        }
    }

    // State: Balance visibility configuration (true = visible, false = masked like "Rp ••••••")
    var isBalanceVisible = mutableStateOf(false)
        private set

    fun toggleBalanceVisibility() {
        isBalanceVisible.value = !isBalanceVisible.value
    }

    // State: Gmail Backup Configuration
    var gmailAccount = mutableStateOf<String?>(null)
        private set
    var lastBackupTime = mutableStateOf<String>("Belum pernah dicadangkan")
        private set
    var isBackupInProgress = mutableStateOf(false)
        private set

    init {
        // Observe Firebase Auth state
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                // Not signed in at all, sign in anonymously
                viewModelScope.launch {
                    try {
                        auth.signInAnonymously()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                // Set the email if it's a permanent account (not anonymous)
                gmailAccount.value = if (user.isAnonymous) null else user.email
            }
        }
    }

    fun handleLoginMerge() {
        val user = auth.currentUser ?: return
        if (user.isAnonymous) return
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Pull everything from Cloud (Restore)
                restoreFromCloudInternal()
                
                // 2. Push everything back to Cloud (Backup)
                backupToCloudInternal()
                
                // 3. Update status
                val timeSdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                lastBackupTime.value = "Sinkronisasi Berhasil: " + timeSdf.format(Date())
            } catch (e: Exception) {
                Log.e("MergeDebug", "Error during login merge", e)
            }
        }
    }

    fun handleRegisterMigration(oldAnonymousUid: String?) {
        val user = auth.currentUser ?: return
        if (user.isAnonymous) return
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Sync local data (which was from anonymous) to the new email account in cloud
                backupToCloudInternal()
                
                // 2. Clear space: Delete the old anonymous data from Firestore if we have the UID
                if (oldAnonymousUid != null) {
                    deleteOtherUserDataFromCloud(oldAnonymousUid)
                }
                
                val timeSdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                lastBackupTime.value = "Migrasi Berhasil: " + timeSdf.format(Date())
            } catch (e: Exception) {
                Log.e("MigrationDebug", "Error during register migration", e)
            }
        }
    }

    private suspend fun deleteOtherUserDataFromCloud(uid: String) {
        val collections = listOf("transactions", "savingsGoals", "metaItems")
        for (collectionName in collections) {
            val snapshot = firestore.collection("users").document(uid)
                .collection(collectionName).get().await()
            if (!snapshot.isEmpty) {
                val batch = firestore.batch()
                snapshot.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }
        }
    }

    private suspend fun restoreFromCloudInternal() {
        val user = auth.currentUser ?: return
        if (user.isAnonymous) return
        val uid = user.uid

        // Restore Transactions
        val txSnapshot = firestore.collection("users").document(uid)
            .collection("transactions").get().await()
        txSnapshot.documents.forEach { doc ->
            val tx = Transaction(
                id = doc.getLong("id") ?: 0,
                type = TransactionType.valueOf(doc.getString("type") ?: "PENGELUARAN"),
                amount = doc.getDouble("amount") ?: 0.0,
                paymentMethod = doc.getString("paymentMethod") ?: "",
                category = doc.getString("category") ?: "",
                notes = doc.getString("notes") ?: "",
                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
            )
            repository.insert(tx)
        }

        // Restore Savings Goals
        val goalSnapshot = firestore.collection("users").document(uid)
            .collection("savingsGoals").get().await()
        goalSnapshot.documents.forEach { doc ->
            val goal = SavingsGoal(
                id = doc.getLong("id") ?: 0,
                name = doc.getString("name") ?: "",
                targetAmount = doc.getDouble("targetAmount") ?: 0.0,
                currentAmount = doc.getDouble("currentAmount") ?: 0.0,
                targetDateMillis = doc.getLong("targetDateMillis") ?: 0,
                category = doc.getString("category") ?: "Lainnya"
            )
            savingsRepository.insert(goal)
        }

        // Restore Meta Items
        val metaSnapshot = firestore.collection("users").document(uid)
            .collection("metaItems").get().await()
        metaSnapshot.documents.forEach { doc ->
            val item = MetaItem(
                id = doc.getLong("id")?.toInt() ?: 0,
                name = doc.getString("name") ?: "",
                type = ItemType.valueOf(doc.getString("type") ?: "METHOD"),
                colorHex = doc.getString("colorHex") ?: "#9E9E9E"
            )
            metaItemDao.insertMetaItem(item)
        }
    }

    private suspend fun backupToCloudInternal() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        val batch = firestore.batch()

        allTransactions.value.forEach { tx ->
            val docRef = firestore.collection("users").document(uid)
                .collection("transactions").document(tx.id.toString())
            batch.set(docRef, tx)
        }

        allSavingsGoals.value.forEach { goal ->
            val docRef = firestore.collection("users").document(uid)
                .collection("savingsGoals").document(goal.id.toString())
            batch.set(docRef, goal)
        }

        metaItems.value.forEach { item ->
            val docRef = firestore.collection("users").document(uid)
                .collection("metaItems").document("${item.type}_${item.id}")
            batch.set(docRef, item)
        }

        batch.commit().await()
    }

    fun performGmailBackup() {
        val user = auth.currentUser ?: return
        if (user.isAnonymous) return

        viewModelScope.launch(Dispatchers.IO) {
            isBackupInProgress.value = true
            try {
                backupToCloudInternal()
                
                // Update timestamp state
                val timeSdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                lastBackupTime.value = "Berhasil: " + timeSdf.format(Date())
            } catch (e: Exception) {
                Log.e("BackupDebug", "Error during backup", e)
                lastBackupTime.value = "Gagal mencadangkan data"
            } finally {
                isBackupInProgress.value = false
            }
        }
    }

    // CORE TRANSACTION CRUD OPERATIONS
    fun addTransaction(
        type: TransactionType,
        amount: Double,
        paymentMethod: String,
        category: String,
        notes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = Transaction(
                type = type,
                amount = amount,
                paymentMethod = paymentMethod,
                category = category,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
            repository.insert(transaction)
        }
    }

    fun updateTransaction(
        id: Long,
        type: TransactionType,
        amount: Double,
        paymentMethod: String,
        category: String,
        notes: String,
        timestamp: Long
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = Transaction(
                id = id,
                type = type,
                amount = amount,
                paymentMethod = paymentMethod,
                category = category,
                notes = notes,
                timestamp = timestamp
            )
            repository.update(transaction)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteById(id)
        }
    }

    fun deleteAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Delete locally
                repository.deleteAll()
                savingsRepository.deleteAll()
                
                // Delete from cloud
                deleteCloudData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun deleteCloudData() {
        val user = auth.currentUser ?: return
        if (user.isAnonymous) return
        val uid = user.uid

        val collections = listOf("transactions", "savingsGoals", "metaItems")
        for (collectionName in collections) {
            val snapshot = firestore.collection("users").document(uid)
                .collection(collectionName).get().await()
            if (!snapshot.isEmpty) {
                val batch = firestore.batch()
                snapshot.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }
        }
    }

    fun restoreFromCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            isBackupInProgress.value = true
            try {
                restoreFromCloudInternal()
                val timeSdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                lastBackupTime.value = "Pemulihan Berhasil: " + timeSdf.format(Date())
            } catch (e: Exception) {
                Log.e("RestoreDebug", "Error restoring data", e)
                lastBackupTime.value = "Gagal memulihkan data"
            } finally {
                isBackupInProgress.value = false
            }
        }
    }
}

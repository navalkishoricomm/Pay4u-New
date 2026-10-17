package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BrandVoucherEntity
import com.example.data.model.ServiceProviderEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserVoucherEntity
import com.example.data.network.LiveBillResult
import com.example.data.repository.Pay4uRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
  HOME,
  MOBILE_RECHARGE,
  DTH_RECHARGE,
  BILL_PAYMENT,
  BRAND_VOUCHERS,
  CREDIT_CARD_PAYMENT,
  TRANSACTION_HISTORY,
  PROFILE,
  ADMIN_DASHBOARD,
  ADMIN_TRANSACTIONS,
  ADMIN_USERS,
  ADMIN_COMMISSIONS
}

sealed class UiEvent {
  data class ShowSnackbar(val message: String) : UiEvent()
  data class TransactionCreated(val transactionId: Long) : UiEvent()
  data class AdminProcessed(val transactionId: Long, val trId: String) : UiEvent()
}

class Pay4uViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: Pay4uRepository

  private val _currentScreen = MutableStateFlow(Screen.HOME)
  val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

  private val _navigationStack = mutableListOf(Screen.HOME)

  private val _currentUser = MutableStateFlow<UserEntity?>(null)
  val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

  private val _isAdminMode = MutableStateFlow(false)
  val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

  private val _uiEvents = MutableSharedFlow<UiEvent>()
  val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

  val allTransactions: StateFlow<List<TransactionEntity>>
  val allUsers: StateFlow<List<UserEntity>>
  val allProviders: StateFlow<List<ServiceProviderEntity>>
  val brandVouchers: StateFlow<List<BrandVoucherEntity>>
  val allUserVouchers: StateFlow<List<UserVoucherEntity>>

  val userTransactions: StateFlow<List<TransactionEntity>>
  val userVouchers: StateFlow<List<UserVoucherEntity>>

  init {
    val db = AppDatabase.getDatabase(application, viewModelScope)
    repository = Pay4uRepository(db)

    allTransactions = repository.getAllTransactions()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    allUsers = repository.getAllUsers()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    allProviders = repository.getAllProviders()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    brandVouchers = repository.getAllBrandVouchers()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    allUserVouchers = repository.getAllUserVouchers()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    userTransactions = _currentUser.flatMapLatest { user ->
      if (user != null) repository.getUserTransactions(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    userVouchers = _currentUser.flatMapLatest { user ->
      if (user != null) repository.getUserVouchers(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User session starts unauthenticated to require login/signup on launch

    // Keep currentUser synchronized with DB updates (e.g. wallet balance changes)
    viewModelScope.launch {
      _currentUser.collect { user ->
        if (user != null) {
          repository.getUser(user.id).collect { fresh ->
            if (fresh != null && (fresh.walletBalance != user.walletBalance || fresh.name != user.name)) {
              _currentUser.value = fresh
            }
          }
        }
      }
    }
  }

  fun navigateTo(screen: Screen) {
    if (screen in listOf(Screen.ADMIN_DASHBOARD, Screen.ADMIN_TRANSACTIONS, Screen.ADMIN_USERS, Screen.ADMIN_COMMISSIONS)) {
      if (_currentUser.value?.role != "ADMIN") {
        return // Non-admin cannot navigate to admin screens
      }
    }
    if (_currentScreen.value != screen) {
      _navigationStack.add(screen)
      _currentScreen.value = screen
    }
  }

  fun navigateBack(): Boolean {
    if (_navigationStack.size > 1) {
      _navigationStack.removeAt(_navigationStack.lastIndex)
      _currentScreen.value = _navigationStack.last()
      return true
    }
    return false
  }

  fun toggleAdminMode(enableAdmin: Boolean) {
    if (enableAdmin && _currentUser.value?.role != "ADMIN") {
      // Non-admin cannot enable admin mode
      return
    }
    _isAdminMode.value = enableAdmin
    if (enableAdmin) {
      navigateTo(Screen.ADMIN_DASHBOARD)
    } else {
      navigateTo(Screen.HOME)
    }
  }

  fun login(phone: String, pass: String, onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val user = repository.login(phone, pass)
      if (user != null) {
        _currentUser.value = user
        if (user.role == "ADMIN") {
          _isAdminMode.value = true
          navigateTo(Screen.ADMIN_DASHBOARD)
        } else {
          _isAdminMode.value = false
          navigateTo(Screen.HOME)
        }
        onResult(true, "Welcome back, ${user.name}!")
      } else {
        onResult(false, "Invalid mobile number or password")
      }
    }
  }

  fun register(name: String, phone: String, email: String, pass: String, onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val result = repository.register(name, phone, email, pass)
      if (result.isSuccess) {
        val user = result.getOrNull()
        _currentUser.value = user
        navigateTo(Screen.HOME)
        onResult(true, "Account created successfully! Welcome to pay4u.")
      } else {
        onResult(false, result.exceptionOrNull()?.message ?: "Registration failed")
      }
    }
  }

  fun logout() {
    _currentUser.value = null
    _isAdminMode.value = false
    _navigationStack.clear()
    _navigationStack.add(Screen.HOME)
    _currentScreen.value = Screen.HOME
  }

  fun switchUser(user: UserEntity) {
    _currentUser.value = user
    if (user.role == "ADMIN") {
      _isAdminMode.value = true
      navigateTo(Screen.ADMIN_DASHBOARD)
    } else {
      _isAdminMode.value = false
      navigateTo(Screen.HOME)
    }
  }

  // Submit Recharge or Bill payment
  fun submitRechargeOrBill(
    serviceType: String, // MOBILE, DTH, BILL
    category: String,
    providerName: String,
    targetNumber: String,
    circle: String,
    planDescription: String,
    amount: Double,
    paymentMethod: String, // "WALLET", "UPI", "CREDIT_CARD"
    paymentRef: String
  ) {
    viewModelScope.launch {
      val user = _currentUser.value ?: return@launch
      val isWalletShortfall = (paymentMethod == "WALLET" && user.walletBalance < amount)

      val provider = allProviders.value.find { it.name.equals(providerName, ignoreCase = true) }
      val commRate = provider?.commissionPercent ?: 2.0
      val flat = provider?.flatCommission ?: 0.0
      val commission = (amount * (commRate / 100.0)) + flat

      val tx = TransactionEntity(
        userId = user.id,
        userName = user.name,
        userPhone = user.phone,
        serviceType = serviceType,
        category = category,
        providerName = providerName,
        targetNumber = targetNumber,
        circle = circle,
        planDescription = planDescription,
        amount = amount,
        discount = 0.0,
        netAmount = amount,
        commissionAmount = commission,
        paymentMethod = paymentMethod,
        userUpiUtr = if (paymentMethod == "UPI") paymentRef else "",
        upiPaymentMethod = paymentMethod,
        status = if (isWalletShortfall) "FAILED" else "PENDING",
        operatorTrId = if (isWalletShortfall) "FAILED" else "",
        adminNotes = if (isWalletShortfall)
          "Payment Failed: Insufficient Wallet Balance. Available: ₹${"%.2f".format(user.walletBalance)}, Required: ₹${"%.2f".format(amount)}"
          else if (paymentMethod == "WALLET") "Paid via Wallet (Auto-debited)" else "Payment Ref: $paymentRef",
        processedBy = "",
        createdAt = System.currentTimeMillis()
      )

      try {
        val txId = repository.createTransaction(tx)
        _uiEvents.emit(UiEvent.TransactionCreated(txId))
        if (isWalletShortfall) {
          _uiEvents.emit(UiEvent.ShowSnackbar("Payment Failed: Insufficient Wallet Balance! FAILED entry recorded in passbook."))
        } else if (paymentMethod == "WALLET") {
          _uiEvents.emit(UiEvent.ShowSnackbar("₹${"%.2f".format(amount)} debited from wallet. Order submitted!"))
        } else {
          _uiEvents.emit(UiEvent.ShowSnackbar("Order submitted via UPI! Admin will verify and process."))
        }
        navigateTo(Screen.TRANSACTION_HISTORY)
      } catch (e: Exception) {
        _uiEvents.emit(UiEvent.ShowSnackbar(e.message ?: "Transaction failed"))
      }
    }
  }

  // Brand Voucher Purchase with strict wallet check and manual voucher upload workflow
  fun purchaseBrandVoucher(
    voucher: BrandVoucherEntity,
    denomination: Double,
    paymentMethod: String, // "WALLET", "UPI", "CREDIT_CARD"
    paymentRef: String
  ) {
    viewModelScope.launch {
      val user = _currentUser.value ?: return@launch
      val result = repository.purchaseBrandVoucher(user, voucher, denomination, paymentMethod, paymentRef)
      if (result.isSuccess) {
        _uiEvents.emit(UiEvent.TransactionCreated(result.getOrNull() ?: 0L))
        _uiEvents.emit(UiEvent.ShowSnackbar("${voucher.brandName} order placed! Admin will verify payment and upload the genuine voucher."))
        navigateTo(Screen.BRAND_VOUCHERS)
      } else {
        _uiEvents.emit(UiEvent.ShowSnackbar(result.exceptionOrNull()?.message ?: "Order failed"))
      }
    }
  }

  fun redeemVoucher(userVoucherId: Long) {
    viewModelScope.launch {
      repository.markVoucherUsed(userVoucherId)
      _uiEvents.emit(UiEvent.ShowSnackbar("Voucher marked as redeemed."))
    }
  }

  // Live Bill Fetch Network Call
  fun fetchLiveBill(
    category: String,
    billerName: String,
    consumerNumber: String,
    onResult: (LiveBillResult) -> Unit
  ) {
    viewModelScope.launch {
      val result = repository.fetchLiveBill(category, billerName, consumerNumber)
      onResult(result)
    }
  }

  // Admin Actions
  fun adminUpdateTransaction(
    transactionId: Long,
    operatorTrId: String,
    status: String,
    adminNotes: String
  ) {
    viewModelScope.launch {
      repository.updateOperatorTrId(transactionId, operatorTrId, status, adminNotes)
      _uiEvents.emit(UiEvent.AdminProcessed(transactionId, operatorTrId))
      _uiEvents.emit(UiEvent.ShowSnackbar("Transaction #$transactionId updated to $status! TR ID: $operatorTrId saved."))
    }
  }

  fun adminUploadGenuineVoucher(
    voucherId: Long,
    genuineCode: String,
    pinCode: String,
    expiryDate: String,
    notes: String
  ) {
    viewModelScope.launch {
      repository.uploadGenuineVoucher(voucherId, genuineCode, pinCode, expiryDate, notes)
      _uiEvents.emit(UiEvent.ShowSnackbar("Genuine voucher uploaded! Code: $genuineCode is now active for user."))
    }
  }

  fun adminRejectVoucher(voucherId: Long, reason: String) {
    viewModelScope.launch {
      repository.rejectVoucherOrder(voucherId, reason)
      _uiEvents.emit(UiEvent.ShowSnackbar("Voucher order rejected. Refund processed if paid via wallet."))
    }
  }

  fun adminUpdateCommission(
    providerId: Long,
    commissionPercent: Double,
    flatCommission: Double,
    isActive: Boolean
  ) {
    viewModelScope.launch {
      repository.updateCommission(providerId, commissionPercent, flatCommission, isActive)
      _uiEvents.emit(UiEvent.ShowSnackbar("Provider commission updated successfully."))
    }
  }

  fun adminAddProvider(
    serviceType: String,
    name: String,
    code: String,
    commissionPercent: Double,
    flatCommission: Double,
    note: String
  ) {
    viewModelScope.launch {
      val provider = ServiceProviderEntity(
        serviceType = serviceType,
        name = name,
        code = code,
        commissionPercent = commissionPercent,
        flatCommission = flatCommission,
        isActive = true,
        helperNote = note
      )
      repository.addProvider(provider)
      _uiEvents.emit(UiEvent.ShowSnackbar("New service provider $name added."))
    }
  }

  fun adminAdjustUserBalance(userId: Long, addedAmount: Double) {
    viewModelScope.launch {
      val user = allUsers.value.find { it.id == userId } ?: return@launch
      val newBalance = (user.walletBalance + addedAmount).coerceAtLeast(0.0)
      repository.updateWalletBalance(userId, newBalance)
      _uiEvents.emit(UiEvent.ShowSnackbar("Updated wallet balance for ${user.name} to ₹${"%.2f".format(newBalance)}"))
    }
  }
}

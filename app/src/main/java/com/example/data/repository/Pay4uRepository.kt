package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.BrandVoucherEntity
import com.example.data.model.ServiceProviderEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserVoucherEntity
import com.example.data.network.BillFetchService
import com.example.data.network.LiveBillResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class Pay4uRepository(private val database: AppDatabase) {
  private val userDao = database.userDao()
  private val transactionDao = database.transactionDao()
  private val providerDao = database.serviceProviderDao()
  private val brandVoucherDao = database.brandVoucherDao()
  private val userVoucherDao = database.userVoucherDao()
  private val billFetchService = BillFetchService()

  // User
  fun getUser(userId: Long): Flow<UserEntity?> = userDao.getUserById(userId)
  fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

  suspend fun login(phone: String, pinOrPass: String): UserEntity? {
    return userDao.authenticate(phone.trim(), pinOrPass.trim())
  }

  suspend fun register(name: String, phone: String, email: String, pass: String): Result<UserEntity> {
    val existing = userDao.findByPhone(phone.trim())
    if (existing != null) {
      return Result.failure(Exception("Mobile number is already registered"))
    }
    val newUser = UserEntity(
      name = name.trim(),
      phone = phone.trim(),
      email = email.trim(),
      password = pass.trim(),
      walletBalance = 1250.0,
      role = if (phone == "9999999999") "ADMIN" else "USER"
    )
    val id = userDao.insertUser(newUser)
    return Result.success(newUser.copy(id = id))
  }

  suspend fun updateWalletBalance(userId: Long, newBalance: Double) {
    userDao.updateWalletBalance(userId, newBalance)
  }

  // Transactions
  fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
  fun getUserTransactions(userId: Long): Flow<List<TransactionEntity>> = transactionDao.getTransactionsByUser(userId)
  fun getTransactionsByStatus(status: String): Flow<List<TransactionEntity>> = transactionDao.getTransactionsByStatus(status)

  suspend fun createTransaction(transaction: TransactionEntity): Long {
    // If paying from Wallet, strictly validate and deduct
    if (transaction.paymentMethod == "WALLET") {
      val user = userDao.getUserById(transaction.userId).firstOrNull()
        ?: throw Exception("User not found")
      if (user.walletBalance < transaction.netAmount) {
        throw Exception("Insufficient Wallet Balance! Available: ₹${"%.2f".format(user.walletBalance)}, Required: ₹${"%.2f".format(transaction.netAmount)}")
      }
      val updatedBalance = user.walletBalance - transaction.netAmount
      userDao.updateWalletBalance(user.id, updatedBalance)
    }

    return transactionDao.insertTransaction(transaction)
  }

  suspend fun updateOperatorTrId(
    transactionId: Long,
    operatorTrId: String,
    status: String,
    adminNotes: String
  ) {
    transactionDao.updateOperatorTrId(
      id = transactionId,
      operatorTrId = operatorTrId.trim(),
      status = status,
      adminNotes = adminNotes.trim(),
      processedBy = "Admin",
      processedAt = System.currentTimeMillis()
    )

    // If transaction is FAILED and was paid from WALLET, refund the user!
    if (status == "FAILED") {
      val tx = transactionDao.getTransactionById(transactionId).firstOrNull()
      if (tx != null && tx.paymentMethod == "WALLET") {
        val user = userDao.getUserById(tx.userId).firstOrNull()
        if (user != null) {
          userDao.updateWalletBalance(user.id, user.walletBalance + tx.netAmount)
        }
      }
    }
  }

  // Service Providers & Commission Settings
  fun getAllProviders(): Flow<List<ServiceProviderEntity>> = providerDao.getAllProviders()
  fun getActiveProviders(serviceType: String): Flow<List<ServiceProviderEntity>> = providerDao.getActiveProvidersByType(serviceType)

  suspend fun updateCommission(id: Long, commissionPercent: Double, flatCommission: Double, isActive: Boolean) {
    providerDao.updateCommission(id, commissionPercent, flatCommission, isActive)
  }

  suspend fun addProvider(provider: ServiceProviderEntity): Long {
    return providerDao.insertProvider(provider)
  }

  // Brand Vouchers
  fun getAllBrandVouchers(): Flow<List<BrandVoucherEntity>> = brandVoucherDao.getAllBrandVouchers()
  fun getUserVouchers(userId: Long): Flow<List<UserVoucherEntity>> = userVoucherDao.getVouchersByUser(userId)
  fun getAllUserVouchers(): Flow<List<UserVoucherEntity>> = userVoucherDao.getAllUserVouchers()

  suspend fun purchaseBrandVoucher(
    user: UserEntity,
    voucher: BrandVoucherEntity,
    denomination: Double,
    paymentMethod: String, // "WALLET", "UPI", "CREDIT_CARD"
    paymentRef: String
  ): Result<Long> {
    val discountAmt = denomination * (voucher.discountPercent / 100.0)
    val finalPayable = denomination - discountAmt

    // 1. Strict Wallet Balance Validation
    if (paymentMethod == "WALLET") {
      val freshUser = userDao.getUserById(user.id).firstOrNull() ?: user
      if (freshUser.walletBalance < finalPayable) {
        return Result.failure(
          Exception("Insufficient Wallet Balance! Available: ₹${"%.2f".format(freshUser.walletBalance)}, Required: ₹${"%.2f".format(finalPayable)}. Please choose UPI or Credit Card.")
        )
      }
      // Deduct from wallet
      userDao.updateWalletBalance(user.id, freshUser.walletBalance - finalPayable)
    }

    // 2. Create Transaction in PENDING state
    val tx = TransactionEntity(
      userId = user.id,
      userName = user.name,
      userPhone = user.phone,
      serviceType = "VOUCHER",
      category = "Brand Voucher",
      providerName = voucher.brandName,
      targetNumber = "${voucher.brandName} (₹${denomination.toInt()})",
      planDescription = "${voucher.brandName} ₹${denomination.toInt()} - Paid via $paymentMethod",
      amount = denomination,
      discount = discountAmt,
      netAmount = finalPayable,
      commissionAmount = discountAmt,
      paymentMethod = paymentMethod,
      userUpiUtr = if (paymentMethod == "UPI") paymentRef else "",
      upiPaymentMethod = paymentMethod,
      status = "PENDING", // PENDING ADMIN MANUAL VERIFICATION & GENUINE VOUCHER UPLOAD!
      operatorTrId = "",
      adminNotes = "Awaiting admin payment review & genuine voucher code upload",
      processedBy = "",
      createdAt = System.currentTimeMillis()
    )
    val txId = transactionDao.insertTransaction(tx)

    // 3. Create Pending User Voucher Record (No fake generated code!)
    val userVoucher = UserVoucherEntity(
      userId = user.id,
      userName = user.name,
      userPhone = user.phone,
      transactionId = txId,
      brandName = voucher.brandName,
      denomination = denomination,
      finalPaid = finalPayable,
      paymentMethod = paymentMethod,
      voucherCode = "", // Admin will enter the genuine code!
      pinCode = "",
      expiryDate = "",
      status = "PENDING", // PENDING ADMIN UPLOAD
      adminNotes = "Order placed. Awaiting admin upload.",
      purchasedAt = System.currentTimeMillis(),
      approvedAt = null,
      isUsed = false
    )
    userVoucherDao.insertUserVoucher(userVoucher)
    return Result.success(txId)
  }

  suspend fun uploadGenuineVoucher(
    userVoucherId: Long,
    genuineCode: String,
    pinCode: String,
    expiryDate: String,
    adminNotes: String
  ) {
    userVoucherDao.uploadGenuineVoucher(
      id = userVoucherId,
      voucherCode = genuineCode.trim(),
      pinCode = pinCode.trim(),
      expiryDate = expiryDate.trim(),
      adminNotes = adminNotes.trim(),
      approvedAt = System.currentTimeMillis()
    )

    // Also update transaction status to SUCCESS with the voucher code as TR ID
    val vch = userVoucherDao.getVoucherById(userVoucherId).firstOrNull()
    if (vch != null) {
      transactionDao.updateOperatorTrId(
        id = vch.transactionId,
        operatorTrId = genuineCode.trim(),
        status = "SUCCESS",
        adminNotes = "Genuine voucher uploaded by Admin",
        processedBy = "Admin",
        processedAt = System.currentTimeMillis()
      )
    }
  }

  suspend fun rejectVoucherOrder(userVoucherId: Long, reason: String) {
    userVoucherDao.rejectVoucher(userVoucherId, reason)
    val vch = userVoucherDao.getVoucherById(userVoucherId).firstOrNull()
    if (vch != null) {
      transactionDao.updateOperatorTrId(
        id = vch.transactionId,
        operatorTrId = "REJECTED",
        status = "FAILED",
        adminNotes = reason,
        processedBy = "Admin",
        processedAt = System.currentTimeMillis()
      )
      // Refund if paid by wallet
      if (vch.paymentMethod == "WALLET") {
        val user = userDao.getUserById(vch.userId).firstOrNull()
        if (user != null) {
          userDao.updateWalletBalance(user.id, user.walletBalance + vch.finalPaid)
        }
      }
    }
  }

  suspend fun markVoucherUsed(userVoucherId: Long) {
    userVoucherDao.markAsUsed(userVoucherId)
  }

  // Live Bill Fetch Network Call
  suspend fun fetchLiveBill(category: String, billerName: String, consumerNumber: String): LiveBillResult {
    return billFetchService.fetchLiveBill(category, billerName, consumerNumber)
  }
}

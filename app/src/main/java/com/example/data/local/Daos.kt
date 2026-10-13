package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BrandVoucherEntity
import com.example.data.model.ServiceProviderEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserVoucherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Query("SELECT * FROM users ORDER BY createdAt DESC")
  fun getAllUsers(): Flow<List<UserEntity>>

  @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
  fun getUserById(userId: Long): Flow<UserEntity?>

  @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
  suspend fun findByPhone(phone: String): UserEntity?

  @Query("SELECT * FROM users WHERE phone = :phone AND password = :password LIMIT 1")
  suspend fun authenticate(phone: String, password: String): UserEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity): Long

  @Update
  suspend fun updateUser(user: UserEntity)

  @Query("UPDATE users SET walletBalance = :newBalance WHERE id = :userId")
  suspend fun updateWalletBalance(userId: Long, newBalance: Double)
}

@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
  fun getAllTransactions(): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC")
  fun getTransactionsByUser(userId: Long): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE status = :status ORDER BY createdAt DESC")
  fun getTransactionsByStatus(status: String): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
  fun getTransactionById(id: Long): Flow<TransactionEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: TransactionEntity): Long

  @Update
  suspend fun updateTransaction(transaction: TransactionEntity)

  @Query("UPDATE transactions SET operatorTrId = :operatorTrId, status = :status, adminNotes = :adminNotes, processedBy = :processedBy, processedAt = :processedAt WHERE id = :id")
  suspend fun updateOperatorTrId(
    id: Long,
    operatorTrId: String,
    status: String,
    adminNotes: String,
    processedBy: String = "Admin",
    processedAt: Long = System.currentTimeMillis()
  )
}

@Dao
interface ServiceProviderDao {
  @Query("SELECT * FROM service_providers ORDER BY serviceType ASC, name ASC")
  fun getAllProviders(): Flow<List<ServiceProviderEntity>>

  @Query("SELECT * FROM service_providers WHERE serviceType = :serviceType AND isActive = 1 ORDER BY name ASC")
  fun getActiveProvidersByType(serviceType: String): Flow<List<ServiceProviderEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProvider(provider: ServiceProviderEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(providers: List<ServiceProviderEntity>)

  @Update
  suspend fun updateProvider(provider: ServiceProviderEntity)

  @Query("UPDATE service_providers SET commissionPercent = :commissionPercent, flatCommission = :flatCommission, isActive = :isActive WHERE id = :id")
  suspend fun updateCommission(id: Long, commissionPercent: Double, flatCommission: Double, isActive: Boolean)
}

@Dao
interface BrandVoucherDao {
  @Query("SELECT * FROM brand_vouchers ORDER BY id ASC")
  fun getAllBrandVouchers(): Flow<List<BrandVoucherEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(vouchers: List<BrandVoucherEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVoucher(voucher: BrandVoucherEntity): Long

  @Update
  suspend fun updateVoucher(voucher: BrandVoucherEntity)
}

@Dao
interface UserVoucherDao {
  @Query("SELECT * FROM user_vouchers WHERE userId = :userId ORDER BY purchasedAt DESC")
  fun getVouchersByUser(userId: Long): Flow<List<UserVoucherEntity>>

  @Query("SELECT * FROM user_vouchers ORDER BY purchasedAt DESC")
  fun getAllUserVouchers(): Flow<List<UserVoucherEntity>>

  @Query("SELECT * FROM user_vouchers WHERE id = :id LIMIT 1")
  fun getVoucherById(id: Long): Flow<UserVoucherEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUserVoucher(voucher: UserVoucherEntity): Long

  @Query("UPDATE user_vouchers SET voucherCode = :voucherCode, pinCode = :pinCode, expiryDate = :expiryDate, status = 'ACTIVE', adminNotes = :adminNotes, approvedAt = :approvedAt WHERE id = :id")
  suspend fun uploadGenuineVoucher(
    id: Long,
    voucherCode: String,
    pinCode: String,
    expiryDate: String,
    adminNotes: String,
    approvedAt: Long = System.currentTimeMillis()
  )

  @Query("UPDATE user_vouchers SET status = 'REJECTED', adminNotes = :adminNotes WHERE id = :id")
  suspend fun rejectVoucher(id: Long, adminNotes: String)

  @Query("UPDATE user_vouchers SET isUsed = 1, status = 'REDEEMED' WHERE id = :id")
  suspend fun markAsUsed(id: Long)
}

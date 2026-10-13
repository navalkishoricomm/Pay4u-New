package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val phone: String,
  val email: String,
  val password: String = "123456",
  val walletBalance: Double = 1250.0,
  val role: String = "USER", // "USER" or "ADMIN"
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val userId: Long,
  val userName: String,
  val userPhone: String,
  val serviceType: String, // "MOBILE", "DTH", "BILL", "VOUCHER"
  val category: String, // "Prepaid", "Postpaid", "Electricity", "Water", "LPG Gas", "Broadband", "Fastag", "Brand Voucher"
  val providerName: String,
  val targetNumber: String, // Mobile number, Smart Card ID, Consumer ID, or Voucher Code
  val circle: String = "Pan India",
  val planDescription: String = "",
  val amount: Double,
  val discount: Double = 0.0,
  val netAmount: Double,
  val commissionAmount: Double = 0.0,
  val paymentMethod: String = "UPI", // "WALLET", "UPI", "CREDIT_CARD"
  val userUpiUtr: String = "",
  val upiPaymentMethod: String = "UPI",
  val status: String = "PENDING", // "PENDING", "SUCCESS", "FAILED"
  val operatorTrId: String = "", // ENTERED BY ADMIN
  val adminNotes: String = "",
  val processedBy: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val processedAt: Long? = null
)

@Entity(tableName = "service_providers")
data class ServiceProviderEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val serviceType: String, // "MOBILE", "DTH", "BILL"
  val name: String,
  val code: String,
  val commissionPercent: Double, // e.g. 2.0%
  val flatCommission: Double = 0.0, // e.g. Rs 2 flat
  val isActive: Boolean = true,
  val helperNote: String = ""
)

@Entity(tableName = "brand_vouchers")
data class BrandVoucherEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val brandName: String,
  val category: String, // "Shopping", "Food", "Fashion", "Entertainment", "Gaming"
  val discountPercent: Double,
  val description: String,
  val denominationsCsv: String, // "100,250,500,1000,2000,5000"
  val terms: String,
  val badge: String = "Popular"
)

@Entity(tableName = "user_vouchers")
data class UserVoucherEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val userId: Long,
  val userName: String = "",
  val userPhone: String = "",
  val transactionId: Long,
  val brandName: String,
  val denomination: Double,
  val finalPaid: Double,
  val paymentMethod: String = "UPI",
  val voucherCode: String = "", // UPLOADED BY ADMIN
  val pinCode: String = "",     // UPLOADED BY ADMIN
  val expiryDate: String = "",  // UPLOADED BY ADMIN
  val status: String = "PENDING", // "PENDING", "ACTIVE", "REJECTED", "REDEEMED"
  val adminNotes: String = "",
  val purchasedAt: Long = System.currentTimeMillis(),
  val approvedAt: Long? = null,
  val isUsed: Boolean = false
)

data class RechargePlan(
  val id: String,
  val operator: String,
  val amount: Double,
  val validity: String,
  val data: String,
  val voice: String,
  val sms: String = "100 SMS/day",
  val category: String,
  val description: String
)

package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BrandVoucherEntity
import com.example.data.model.ServiceProviderEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserVoucherEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    UserEntity::class,
    TransactionEntity::class,
    ServiceProviderEntity::class,
    BrandVoucherEntity::class,
    UserVoucherEntity::class
  ],
  version = 3,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
  abstract fun transactionDao(): TransactionDao
  abstract fun serviceProviderDao(): ServiceProviderDao
  abstract fun brandVoucherDao(): BrandVoucherDao
  abstract fun userVoucherDao(): UserVoucherDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "rechargepay_database"
        )
          .fallbackToDestructiveMigration()
          .addCallback(DatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }
  }

  private class DatabaseCallback(
    private val scope: CoroutineScope
  ) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
      super.onCreate(db)
      INSTANCE?.let { database ->
        scope.launch(Dispatchers.IO) {
          populateInitialData(database)
        }
      }
    }

    private suspend fun populateInitialData(database: AppDatabase) {
      val userDao = database.userDao()
      val providerDao = database.serviceProviderDao()
      val voucherDao = database.brandVoucherDao()
      val transactionDao = database.transactionDao()

      // Seed Admin User
      userDao.insertUser(
        UserEntity(
          id = 1,
          name = "Master Admin",
          phone = "9999999999",
          email = "admin@rechargepay.in",
          password = "admin",
          walletBalance = 25000.0,
          role = "ADMIN"
        )
      )

      // Seed Regular User
      val defaultUserId = userDao.insertUser(
        UserEntity(
          id = 2,
          name = "Mukesh Garg",
          phone = "9876543210",
          email = "mukgarg11@gmail.com",
          password = "user123",
          walletBalance = 1250.0,
          role = "USER"
        )
      )

      // Seed Service Providers with Commissions
      val initialProviders = listOf(
        // Mobile Operators
        ServiceProviderEntity(serviceType = "MOBILE", name = "Jio", code = "JIO", commissionPercent = 2.2, flatCommission = 0.0, isActive = true, helperNote = "Instant automated TR generation via Jio Partner API"),
        ServiceProviderEntity(serviceType = "MOBILE", name = "Airtel", code = "AIRTEL", commissionPercent = 1.8, flatCommission = 0.0, isActive = true, helperNote = "Airtel Direct operator processing"),
        ServiceProviderEntity(serviceType = "MOBILE", name = "Vi (Vodafone Idea)", code = "VI", commissionPercent = 3.0, flatCommission = 0.0, isActive = true, helperNote = "Vi Express operator route"),
        ServiceProviderEntity(serviceType = "MOBILE", name = "BSNL", code = "BSNL", commissionPercent = 4.5, flatCommission = 0.0, isActive = true, helperNote = "BSNL GSM / Special Tariff route"),

        // DTH Operators
        ServiceProviderEntity(serviceType = "DTH", name = "Tata Play (Tata Sky)", code = "TATASKY", commissionPercent = 2.5, flatCommission = 0.0, isActive = true, helperNote = "Subscriber ID 10-digit validation"),
        ServiceProviderEntity(serviceType = "DTH", name = "Airtel Digital TV", code = "AIRTELDTH", commissionPercent = 2.8, flatCommission = 0.0, isActive = true, helperNote = "Smart Card 10-digit validation"),
        ServiceProviderEntity(serviceType = "DTH", name = "Dish TV", code = "DISHTV", commissionPercent = 3.2, flatCommission = 0.0, isActive = true, helperNote = "Viewing Card Number 11-digit"),
        ServiceProviderEntity(serviceType = "DTH", name = "Sun Direct", code = "SUNDIRECT", commissionPercent = 3.5, flatCommission = 0.0, isActive = true, helperNote = "Smart Card Number 11-digit"),
        ServiceProviderEntity(serviceType = "DTH", name = "D2H (Videocon)", code = "D2H", commissionPercent = 3.0, flatCommission = 0.0, isActive = true, helperNote = "Customer ID 8-10 digits"),

        // Bill Payments - Electricity & Utilities
        ServiceProviderEntity(serviceType = "BILL", name = "Electricity (All BBPS Boards)", code = "ELECTRICITY", commissionPercent = 0.5, flatCommission = 2.0, isActive = true, helperNote = "BBPS Bill payment hub"),
        ServiceProviderEntity(serviceType = "BILL", name = "BSES Rajdhani Power (BRPL Delhi)", code = "BSES_DELHI", commissionPercent = 0.5, flatCommission = 2.5, isActive = true, helperNote = "Delhi NCR Electricity"),
        ServiceProviderEntity(serviceType = "BILL", name = "BSES Yamuna Power (BYPL Delhi)", code = "BSES_YAMUNA", commissionPercent = 0.5, flatCommission = 2.5, isActive = true, helperNote = "Delhi NCR Electricity"),
        ServiceProviderEntity(serviceType = "BILL", name = "Tata Power Delhi (TPDDL)", code = "TPDDL", commissionPercent = 0.5, flatCommission = 2.5, isActive = true, helperNote = "Delhi Electricity"),
        ServiceProviderEntity(serviceType = "BILL", name = "Maharashtra State Electricity (MSEDCL)", code = "MSEDCL", commissionPercent = 0.4, flatCommission = 2.0, isActive = true, helperNote = "Mahavitaran Maharashtra"),
        ServiceProviderEntity(serviceType = "BILL", name = "Adani Electricity Mumbai", code = "ADANI_ELEC", commissionPercent = 0.4, flatCommission = 2.5, isActive = true, helperNote = "Mumbai Electricity"),
        ServiceProviderEntity(serviceType = "BILL", name = "UPPCL - Uttar Pradesh Power Corp", code = "UPPCL", commissionPercent = 0.5, flatCommission = 2.0, isActive = true, helperNote = "UP Urban & Rural"),
        ServiceProviderEntity(serviceType = "BILL", name = "Bangalore Electricity Supply (BESCOM)", code = "BESCOM", commissionPercent = 0.5, flatCommission = 2.0, isActive = true, helperNote = "Karnataka Electricity"),
        ServiceProviderEntity(serviceType = "BILL", name = "Tamil Nadu Generation & Dist (TANGEDCO)", code = "TANGEDCO", commissionPercent = 0.5, flatCommission = 2.0, isActive = true, helperNote = "Tamil Nadu Electricity"),
        ServiceProviderEntity(serviceType = "BILL", name = "Punjab State Power Corp (PSPCL)", code = "PSPCL", commissionPercent = 0.5, flatCommission = 2.0, isActive = true, helperNote = "Punjab Electricity"),
        ServiceProviderEntity(serviceType = "BILL", name = "Jaipur Vidyut Vitran (JVVNL)", code = "JVVNL", commissionPercent = 0.5, flatCommission = 2.0, isActive = true, helperNote = "Rajasthan Electricity"),
        ServiceProviderEntity(serviceType = "BILL", name = "West Bengal State Electricity (WBSEDCL)", code = "WBSEDCL", commissionPercent = 0.5, flatCommission = 2.0, isActive = true, helperNote = "West Bengal Electricity"),
        ServiceProviderEntity(serviceType = "BILL", name = "Kerala State Electricity Board (KSEBL)", code = "KSEBL", commissionPercent = 0.5, flatCommission = 2.0, isActive = true, helperNote = "Kerala Electricity"),
        ServiceProviderEntity(serviceType = "BILL", name = "Water Bill (Municipal Boards)", code = "WATER", commissionPercent = 0.0, flatCommission = 3.0, isActive = true, helperNote = "Municipal water authority hub"),
        ServiceProviderEntity(serviceType = "BILL", name = "LPG Gas Cylinder / Piped Gas", code = "GAS", commissionPercent = 0.0, flatCommission = 2.5, isActive = true, helperNote = "Indane, Bharat, HP & IGL"),
        ServiceProviderEntity(serviceType = "BILL", name = "Broadband & Fiber", code = "BROADBAND", commissionPercent = 1.0, flatCommission = 0.0, isActive = true, helperNote = "JioFiber, Airtel Xstream, ACT"),
        ServiceProviderEntity(serviceType = "BILL", name = "Fastag Recharge", code = "FASTAG", commissionPercent = 0.3, flatCommission = 1.0, isActive = true, helperNote = "NETC Fastag toll payments")
      )
      providerDao.insertAll(initialProviders)

      // Seed Brand Vouchers
      val initialVouchers = listOf(
        BrandVoucherEntity(
          brandName = "Amazon Pay Gift Card",
          category = "Shopping",
          discountPercent = 2.5,
          description = "Use on Amazon.in for shopping, recharges, bill payments and partner merchants.",
          denominationsCsv = "250,500,1000,2000,5000",
          terms = "Valid for 1 year from issuance. Non-refundable. Cannot be transferred to bank.",
          badge = "Best Seller"
        ),
        BrandVoucherEntity(
          brandName = "Flipkart Gift Card",
          category = "Shopping",
          discountPercent = 2.0,
          description = "Instant voucher for electronics, fashion, groceries and home appliances.",
          denominationsCsv = "100,500,1000,2500,5000",
          terms = "Valid across all items on Flipkart website and app. Valid for 12 months.",
          badge = "Popular"
        ),
        BrandVoucherEntity(
          brandName = "Swiggy Food & Instamart",
          category = "Food",
          discountPercent = 5.0,
          description = "Order food, groceries via Instamart and dine-out with instant savings.",
          denominationsCsv = "100,250,500,1000",
          terms = "Add to Swiggy Money wallet. Redeemable across all Swiggy services.",
          badge = "5% OFF"
        ),
        BrandVoucherEntity(
          brandName = "Zomato Voucher",
          category = "Food",
          discountPercent = 4.0,
          description = "Flat discount on restaurant food deliveries and Zomato Dining across India.",
          denominationsCsv = "150,250,500,1000",
          terms = "Can be clubbed with restaurant discounts. Valid for 1 year.",
          badge = "Hot Deal"
        ),
        BrandVoucherEntity(
          brandName = "Google Play Code",
          category = "Gaming",
          discountPercent = 3.0,
          description = "Purchase games, in-app purchases, subscriptions, books and movies.",
          denominationsCsv = "100,300,500,1000,1500",
          terms = "Redeem via Play Store Redeem Code menu. No expiration date.",
          badge = "Gaming"
        ),
        BrandVoucherEntity(
          brandName = "Myntra E-Gift Card",
          category = "Fashion",
          discountPercent = 6.0,
          description = "Shop top fashion brands, footwear, accessories and beauty products.",
          denominationsCsv = "500,1000,2000,5000",
          terms = "Valid for 12 months. Can be combined with ongoing sales.",
          badge = "6% OFF"
        ),
        BrandVoucherEntity(
          brandName = "BookMyShow Voucher",
          category = "Entertainment",
          discountPercent = 8.0,
          description = "Book movie tickets, concerts, live events and sports matches.",
          denominationsCsv = "250,500,1000",
          terms = "Applicable on movie and event tickets. Valid for 6 months.",
          badge = "8% OFF"
        ),
        BrandVoucherEntity(
          brandName = "Uber Rides & Eats",
          category = "Travel",
          discountPercent = 4.0,
          description = "Add credits to your Uber wallet for daily cab, auto and moto commutes.",
          denominationsCsv = "250,500,1000",
          terms = "Credits valid across all Uber rides in India.",
          badge = "Travel"
        )
      )
      voucherDao.insertAll(initialVouchers)

      // Seed Sample Transactions showing both Pending and Completed (with Operator TR ID)
      val sampleTx1 = TransactionEntity(
        userId = defaultUserId,
        userName = "Mukesh Garg",
        userPhone = "9876543210",
        serviceType = "MOBILE",
        category = "Prepaid",
        providerName = "Jio",
        targetNumber = "9876543210",
        circle = "Delhi NCR",
        planDescription = "₹299 - 2GB/Day + Unlimited 5G (28 Days)",
        amount = 299.0,
        discount = 0.0,
        netAmount = 299.0,
        commissionAmount = 6.57,
        userUpiUtr = "428710928374",
        upiPaymentMethod = "GPay",
        status = "SUCCESS",
        operatorTrId = "JIO-TR-88492019",
        adminNotes = "Processed via Jio Core Gateway",
        processedBy = "Admin",
        createdAt = System.currentTimeMillis() - 86400000L,
        processedAt = System.currentTimeMillis() - 86000000L
      )

      val sampleTx2 = TransactionEntity(
        userId = defaultUserId,
        userName = "Mukesh Garg",
        userPhone = "9876543210",
        serviceType = "DTH",
        category = "Monthly Pack",
        providerName = "Tata Play",
        targetNumber = "1098472910",
        circle = "Pan India",
        planDescription = "Hindi Super Value Pack (30 Days)",
        amount = 450.0,
        discount = 0.0,
        netAmount = 450.0,
        commissionAmount = 11.25,
        userUpiUtr = "428799102847",
        upiPaymentMethod = "PhonePe",
        status = "PENDING",
        operatorTrId = "",
        adminNotes = "",
        processedBy = "",
        createdAt = System.currentTimeMillis() - 1800000L,
        processedAt = null
      )

      val sampleTx3 = TransactionEntity(
        userId = defaultUserId,
        userName = "Mukesh Garg",
        userPhone = "9876543210",
        serviceType = "BILL",
        category = "Electricity",
        providerName = "Electricity (Tata / BSES / State Boards)",
        targetNumber = "CA-9028174092",
        circle = "BSES Rajdhani Delhi",
        planDescription = "Monthly Household Consumption Bill",
        amount = 1420.0,
        discount = 0.0,
        netAmount = 1420.0,
        commissionAmount = 2.0,
        userUpiUtr = "428800119283",
        upiPaymentMethod = "Paytm UPI",
        status = "PENDING",
        operatorTrId = "",
        adminNotes = "",
        processedBy = "",
        createdAt = System.currentTimeMillis() - 600000L,
        processedAt = null
      )

      transactionDao.insertTransaction(sampleTx1)
      transactionDao.insertTransaction(sampleTx2)
      transactionDao.insertTransaction(sampleTx3)
    }
  }
}

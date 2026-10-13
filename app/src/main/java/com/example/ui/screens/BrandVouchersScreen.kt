package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BrandVoucherEntity
import com.example.data.model.UserVoucherEntity
import com.example.ui.components.CheckoutPaymentSheet
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.StatusFailed
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.Pay4uViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandVouchersScreen(
  viewModel: Pay4uViewModel,
  onBack: () -> Unit
) {
  BackHandler { onBack() }

  val brandVouchers by viewModel.brandVouchers.collectAsStateWithLifecycle()
  val userVouchers by viewModel.userVouchers.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val clipboardManager = LocalClipboardManager.current

  var selectedTab by remember { mutableStateOf(0) }
  val tabs = listOf("Buy Gift Vouchers", "My Vouchers (${userVouchers.size})")

  val categories = listOf("All", "Shopping", "Food", "Fashion", "Gaming", "Entertainment", "Travel")
  var selectedCategory by remember { mutableStateOf("All") }

  // Purchase modal state
  var purchasingVoucher by remember { mutableStateOf<BrandVoucherEntity?>(null) }
  var chosenDenomination by remember { mutableStateOf(500.0) }
  var showPaymentSheet by remember { mutableStateOf(false) }

  val filteredBrandVouchers = remember(brandVouchers, selectedCategory) {
    if (selectedCategory == "All") brandVouchers
    else brandVouchers.filter { it.category.equals(selectedCategory, ignoreCase = true) }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Brand Gift Vouchers", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("back_button_vouchers")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
    ) {
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("voucher_tab_$index")
          )
        }
      }

      if (selectedTab == 0) {
        // Buy Vouchers Tab
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("buy_vouchers_list"),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            // Category Chips
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.padding(bottom = 8.dp)
            ) {
              items(categories) { cat ->
                FilterChip(
                  selected = selectedCategory == cat,
                  onClick = { selectedCategory = cat },
                  label = { Text(cat) }
                )
              }
            }
          }

          items(filteredBrandVouchers) { voucher ->
            BrandVoucherCard(
              voucher = voucher,
              onSelectToBuy = { denomination ->
                purchasingVoucher = voucher
                chosenDenomination = denomination
                showPaymentSheet = true
              }
            )
          }
        }
      } else {
        // My Purchased Vouchers Tab
        if (userVouchers.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.CardGiftcard,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "No Vouchers Purchased Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Choose your desired brand voucher from 'Buy Gift Vouchers'. Admin will verify payment and upload the genuine voucher code.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(onClick = { selectedTab = 0 }) {
                Text("Browse Brand Vouchers")
              }
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .testTag("my_vouchers_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            items(userVouchers) { voucher ->
              UserVoucherCard(
                voucher = voucher,
                onCopyCode = { code ->
                  clipboardManager.setText(AnnotatedString(code))
                },
                onRedeem = { viewModel.redeemVoucher(voucher.id) }
              )
            }
          }
        }
      }
    }
  }

  // Multi-tab Checkout Sheet (Wallet with strict check, UPI, Credit Card)
  if (showPaymentSheet && purchasingVoucher != null) {
    val vch = purchasingVoucher!!
    val discountAmt = chosenDenomination * (vch.discountPercent / 100.0)
    val finalPayable = chosenDenomination - discountAmt

    CheckoutPaymentSheet(
      amount = finalPayable,
      serviceTitle = "${vch.brandName} Voucher (₹${chosenDenomination.toInt()})",
      targetDetails = "Flat ${vch.discountPercent}% Off • Genuine code uploaded manually by Admin",
      walletBalance = currentUser?.walletBalance ?: 0.0,
      onDismiss = { showPaymentSheet = false },
      onConfirmPayment = { method, ref ->
        showPaymentSheet = false
        viewModel.purchaseBrandVoucher(
          voucher = vch,
          denomination = chosenDenomination,
          paymentMethod = method,
          paymentRef = ref
        )
        selectedTab = 1 // Switch to My Vouchers tab
      }
    )
  }
}

@Composable
fun BrandVoucherCard(
  voucher: BrandVoucherEntity,
  onSelectToBuy: (denomination: Double) -> Unit
) {
  val denominations = remember(voucher.denominationsCsv) {
    voucher.denominationsCsv.split(",").mapNotNull { it.trim().toDoubleOrNull() }
  }
  var selectedDenom by remember { mutableStateOf(denominations.firstOrNull() ?: 500.0) }

  val discountAmount = selectedDenom * (voucher.discountPercent / 100.0)
  val finalAmount = selectedDenom - discountAmount

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("brand_voucher_card_${voucher.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = voucher.brandName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = voucher.category,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = BrandAccent.copy(alpha = 0.15f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.LocalOffer,
              contentDescription = null,
              tint = BrandAccent,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${voucher.discountPercent}% OFF",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = BrandAccent
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = voucher.description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Select Denomination:",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(6.dp))

      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(denominations) { denom ->
          val isSelected = selectedDenom == denom
          Surface(
            onClick = { selectedDenom = denom },
            shape = RoundedCornerShape(8.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStrokeSafe(
              1.dp,
              if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
          ) {
            Text(
              text = "₹${denom.toInt()}",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Price Breakup & Buy Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "₹${"%.0f".format(finalAmount)}",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "₹${selectedDenom.toInt()}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
            )
          }
          Text(
            text = "Save ₹${"%.0f".format(discountAmount)} (${voucher.discountPercent}%)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = BrandSecondary
          )
        }

        Button(
          onClick = { onSelectToBuy(selectedDenom) },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.height(40.dp)
        ) {
          Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Proceed to Pay", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun UserVoucherCard(
  voucher: UserVoucherEntity,
  onCopyCode: (String) -> Unit,
  onRedeem: () -> Unit
) {
  var isCopied by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("user_voucher_${voucher.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = when (voucher.status) {
        "PENDING" -> StatusPending.copy(alpha = 0.08f)
        "REJECTED" -> StatusFailed.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surface
      }
    ),
    border = BorderStrokeSafe(
      1.dp,
      when (voucher.status) {
        "PENDING" -> StatusPending.copy(alpha = 0.4f)
        "REJECTED" -> StatusFailed.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
      }
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = voucher.brandName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Value: ₹${voucher.denomination.toInt()} • Paid: ₹${"%.2f".format(voucher.finalPaid)} via ${voucher.paymentMethod}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = when (voucher.status) {
            "PENDING" -> StatusPending.copy(alpha = 0.15f)
            "ACTIVE" -> StatusSuccess.copy(alpha = 0.15f)
            "REDEEMED" -> MaterialTheme.colorScheme.surfaceVariant
            else -> StatusFailed.copy(alpha = 0.15f)
          }
        ) {
          Text(
            text = when (voucher.status) {
              "PENDING" -> "AWAITING ADMIN UPLOAD"
              "ACTIVE" -> "ACTIVE VOUCHER"
              "REDEEMED" -> "REDEEMED"
              else -> "REJECTED"
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = when (voucher.status) {
              "PENDING" -> StatusPending
              "ACTIVE" -> StatusSuccess
              "REDEEMED" -> MaterialTheme.colorScheme.onSurfaceVariant
              else -> StatusFailed
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (voucher.status == "PENDING") {
        // Pending admin verification banner
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          color = StatusPending.copy(alpha = 0.12f),
          border = BorderStrokeSafe(1.dp, StatusPending.copy(alpha = 0.3f))
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = StatusPending, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Order Under Admin Review",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = StatusPending
              )
              Text(
                text = "Admin will verify your payment and upload the genuine brand voucher code & secret PIN. It will appear here once approved.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      } else if (voucher.status == "ACTIVE" || voucher.status == "REDEEMED") {
        // Active Genuine Code Box
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
          border = BorderStrokeSafe(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Genuine Voucher Code (Verified by Admin)",
                style = MaterialTheme.typography.labelSmall,
                color = BrandSecondary,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = voucher.voucherCode,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
              )
              if (voucher.pinCode.isNotBlank()) {
                Text(
                  text = "PIN: ${voucher.pinCode}",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            IconButton(
              onClick = {
                onCopyCode(voucher.voucherCode)
                isCopied = true
              }
            ) {
              Icon(
                imageVector = if (isCopied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                contentDescription = "Copy Code",
                tint = if (isCopied) BrandSecondary else MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Valid till: ${voucher.expiryDate.ifBlank { "1 Year from issue" }}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          if (voucher.status == "ACTIVE") {
            OutlinedButton(
              onClick = onRedeem,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(34.dp)
            ) {
              Text("Mark as Redeemed", fontSize = 11.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BorderStrokeSafe(width: androidx.compose.ui.unit.Dp, color: Color) =
  androidx.compose.foundation.BorderStroke(width, color)

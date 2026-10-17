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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.StatusFailed
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.Pay4uViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
  viewModel: Pay4uViewModel,
  onBack: () -> Unit
) {
  BackHandler { onBack() }

  val transactions by viewModel.userTransactions.collectAsStateWithLifecycle()
  val clipboardManager = LocalClipboardManager.current

  val filterCategories = listOf("All", "Pending", "Success", "Failed", "Mobile", "DTH", "Bills", "Vouchers")
  var selectedFilter by remember { mutableStateOf("All") }

  var selectedTransactionForReceipt by remember { mutableStateOf<TransactionEntity?>(null) }

  val filteredTransactions = remember(transactions, selectedFilter) {
    when (selectedFilter) {
      "Pending" -> transactions.filter { it.status == "PENDING" }
      "Success" -> transactions.filter { it.status == "SUCCESS" }
      "Failed" -> transactions.filter { it.status == "FAILED" }
      "Mobile" -> transactions.filter { it.serviceType == "MOBILE" }
      "DTH" -> transactions.filter { it.serviceType == "DTH" }
      "Bills" -> transactions.filter { it.serviceType == "BILL" }
      "Vouchers" -> transactions.filter { it.serviceType == "VOUCHER" }
      else -> transactions
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Transaction Passbook", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("back_button_history")) {
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
        .testTag("transaction_history_screen")
    ) {
      // Filter Chips
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(filterCategories) { filter ->
          FilterChip(
            selected = selectedFilter == filter,
            onClick = { selectedFilter = filter },
            label = { Text(filter) }
          )
        }
      }

      if (filteredTransactions.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.ReceiptLong,
              contentDescription = null,
              modifier = Modifier.size(56.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No Transactions Found",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Transactions matching '$selectedFilter' will appear here.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredTransactions) { tx ->
            TransactionDetailCard(
              transaction = tx,
              onCopyTrId = { trId ->
                clipboardManager.setText(AnnotatedString(trId))
              },
              onClick = { selectedTransactionForReceipt = tx }
            )
          }
        }
      }
    }
  }

  // Receipt Modal
  selectedTransactionForReceipt?.let { tx ->
    ReceiptModalSheet(
      transaction = tx,
      onDismiss = { selectedTransactionForReceipt = null },
      onCopyTrId = { trId ->
        clipboardManager.setText(AnnotatedString(trId))
      }
    )
  }
}

@Composable
fun TransactionDetailCard(
  transaction: TransactionEntity,
  onCopyTrId: (String) -> Unit,
  onClick: () -> Unit
) {
  val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
  val formattedDate = dateFormat.format(Date(transaction.createdAt))
  var copiedNotice by remember { mutableStateOf(false) }

  Card(
    onClick = onClick,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("tx_card_${transaction.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Top row: Type Icon, Provider & Target, Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(
                when (transaction.serviceType) {
                  "MOBILE" -> Color(0xFF0F52BA).copy(alpha = 0.12f)
                  "DTH" -> Color(0xFFE60000).copy(alpha = 0.12f)
                  "BILL" -> Color(0xFF00A86B).copy(alpha = 0.12f)
                  else -> Color(0xFFFF9900).copy(alpha = 0.12f)
                }
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = when (transaction.serviceType) {
                "MOBILE" -> Icons.Default.PhoneAndroid
                "DTH" -> Icons.Default.Tv
                "BILL" -> Icons.Default.ElectricalServices
                else -> Icons.Default.CardGiftcard
              },
              contentDescription = null,
              tint = when (transaction.serviceType) {
                "MOBILE" -> Color(0xFF0F52BA)
                "DTH" -> Color(0xFFE60000)
                "BILL" -> Color(0xFF00A86B)
                else -> Color(0xFFFF9900)
              },
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "${transaction.providerName} - ${transaction.targetNumber}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${transaction.category} • $formattedDate",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "₹${"%.2f".format(transaction.amount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = when (transaction.status) {
              "SUCCESS" -> StatusSuccess.copy(alpha = 0.15f)
              "PENDING" -> StatusPending.copy(alpha = 0.15f)
              else -> StatusFailed.copy(alpha = 0.15f)
            }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = when (transaction.status) {
                  "SUCCESS" -> Icons.Default.CheckCircle
                  "PENDING" -> Icons.Default.HourglassTop
                  else -> Icons.Default.Error
                },
                contentDescription = null,
                tint = when (transaction.status) {
                  "SUCCESS" -> StatusSuccess
                  "PENDING" -> StatusPending
                  else -> StatusFailed
                },
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = transaction.status,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = when (transaction.status) {
                  "SUCCESS" -> StatusSuccess
                  "PENDING" -> StatusPending
                  else -> StatusFailed
                },
                fontSize = 10.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Payment Source Pill (Wallet vs UPI with UTR)
      Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (transaction.paymentMethod == "WALLET") StatusSuccess.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (transaction.paymentMethod == "WALLET") Icons.Default.AccountBalanceWallet else Icons.Default.QrCode2,
            contentDescription = null,
            tint = if (transaction.paymentMethod == "WALLET") StatusSuccess else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = if (transaction.paymentMethod == "WALLET") "Paid via Wallet (Auto-debited)"
                   else "Paid via UPI • UTR: ${transaction.userUpiUtr.ifBlank { "Recorded" }}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (transaction.paymentMethod == "WALLET") StatusSuccess else MaterialTheme.colorScheme.primary,
            fontSize = 11.sp
          )
        }
      }

      if (transaction.planDescription.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = transaction.planDescription,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // High-Visibility Operator TR ID Box (entered by Admin!)
      if (transaction.operatorTrId.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = BrandSecondary.copy(alpha = 0.1f),
          border = BorderStrokeSafe(1.dp, BrandSecondary.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "OPERATOR TR ID (PROCESSED BY ADMIN)",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = BrandSecondary
              )
              Text(
                text = transaction.operatorTrId,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            IconButton(
              onClick = {
                onCopyTrId(transaction.operatorTrId)
                copiedNotice = true
              },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = if (copiedNotice) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                contentDescription = "Copy TR ID",
                tint = BrandSecondary,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      } else if (transaction.status == "PENDING") {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = StatusPending.copy(alpha = 0.1f),
          border = BorderStrokeSafe(1.dp, StatusPending.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.HourglassTop,
              contentDescription = null,
              tint = StatusPending,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Under Manual Admin Verification",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = StatusPending
              )
              Text(
                text = "Submitted UPI UTR: ${transaction.userUpiUtr.ifBlank { "Verified via UPI" }}. Admin will enter operator TR ID shortly.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptModalSheet(
  transaction: TransactionEntity,
  onDismiss: () -> Unit,
  onCopyTrId: (String) -> Unit
) {
  val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm:ss a", Locale.getDefault())
  val formattedDate = dateFormat.format(Date(transaction.createdAt))

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Digital Receipt",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.ExtraBold
        )
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Box(
        modifier = Modifier
          .size(60.dp)
          .clip(CircleShape)
          .background(
            when (transaction.status) {
              "SUCCESS" -> StatusSuccess.copy(alpha = 0.15f)
              "PENDING" -> StatusPending.copy(alpha = 0.15f)
              else -> StatusFailed.copy(alpha = 0.15f)
            }
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = when (transaction.status) {
            "SUCCESS" -> Icons.Default.CheckCircle
            "PENDING" -> Icons.Default.HourglassTop
            else -> Icons.Default.Error
          },
          contentDescription = null,
          tint = when (transaction.status) {
            "SUCCESS" -> StatusSuccess
            "PENDING" -> StatusPending
            else -> StatusFailed
          },
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "₹${"%.2f".format(transaction.amount)}",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold
      )

      Text(
        text = "Transaction ID: #TXN-${transaction.id}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Receipt Breakdown
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          ReceiptRow(label = "Service", value = "${transaction.serviceType} (${transaction.category})")
          ReceiptRow(label = "Provider", value = transaction.providerName)
          ReceiptRow(label = "Target Number / ID", value = transaction.targetNumber)
          ReceiptRow(label = "User UPI UTR", value = transaction.userUpiUtr.ifBlank { "Direct UPI" })
          if (transaction.operatorTrId.isNotBlank()) {
            ReceiptRow(label = "Operator TR ID", value = transaction.operatorTrId, isBold = true, highlightColor = BrandSecondary)
          }
          ReceiptRow(label = "Date & Time", value = formattedDate)
          ReceiptRow(label = "Status", value = transaction.status, isBold = true)
          if (transaction.adminNotes.isNotBlank()) {
            ReceiptRow(label = "Admin Remark", value = transaction.adminNotes)
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      if (transaction.operatorTrId.isNotBlank()) {
        OutlinedButton(
          onClick = { onCopyTrId(transaction.operatorTrId) },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Copy Operator TR ID")
        }
      }
    }
  }
}

@Composable
fun ReceiptRow(
  label: String,
  value: String,
  isBold: Boolean = false,
  highlightColor: Color? = null
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium,
      color = highlightColor ?: MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
private fun BorderStrokeSafe(width: androidx.compose.ui.unit.Dp, color: Color) =
  androidx.compose.foundation.BorderStroke(width, color)

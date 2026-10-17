package com.example.ui.screens.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ServiceProviderEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserVoucherEntity
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
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
fun AdminDashboardScreen(
  viewModel: Pay4uViewModel,
  onExitAdmin: () -> Unit
) {
  BackHandler { onExitAdmin() }

  val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
  val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
  val allProviders by viewModel.allProviders.collectAsStateWithLifecycle()
  val allUserVouchers by viewModel.allUserVouchers.collectAsStateWithLifecycle()

  var selectedTab by remember { mutableStateOf(0) }
  val pendingVouchersCount = allUserVouchers.count { it.status == "PENDING" }
  val pendingTxCount = allTransactions.count { it.status == "PENDING" }

  val tabs = listOf(
    "Recharges ($pendingTxCount)",
    "Vouchers ($pendingVouchersCount)",
    "Users",
    "Operators & Margins"
  )

  // Dialog states
  var txToProcess by remember { mutableStateOf<TransactionEntity?>(null) }
  var voucherToUpload by remember { mutableStateOf<UserVoucherEntity?>(null) }
  var userToAdjustBalance by remember { mutableStateOf<UserEntity?>(null) }
  var providerToEdit by remember { mutableStateOf<ServiceProviderEntity?>(null) }
  var showAddProviderDialog by remember { mutableStateOf(false) }

  // KPI Metrics
  val totalVolume = allTransactions.sumOf { it.amount }
  val successCount = allTransactions.count { it.status == "SUCCESS" }
  val totalCommissions = allTransactions.sumOf { it.commissionAmount }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AdminPanelSettings,
              contentDescription = null,
              tint = BrandAccent,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("Admin Command Center", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
              Text("pay4u Settlements & Voucher Uploads", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        },
        navigationIcon = {
          IconButton(onClick = onExitAdmin, modifier = Modifier.testTag("admin_exit_btn")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Admin")
          }
        },
        actions = {
          Surface(
            onClick = onExitAdmin,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
              .padding(end = 12.dp)
              .testTag("admin_switch_to_user_app_btn")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Switch to User App",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
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
        .testTag("admin_dashboard_screen")
    ) {
      // 1. KPI Stats Summary Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        AdminKpiTile(
          title = "Total Volume",
          value = "₹${"%.0f".format(totalVolume)}",
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.weight(1f)
        )
        AdminKpiTile(
          title = "Pending TRs",
          value = pendingTxCount.toString(),
          color = StatusPending,
          badge = if (pendingTxCount > 0) "Action" else null,
          modifier = Modifier.weight(1f)
        )
        AdminKpiTile(
          title = "Vouchers Due",
          value = pendingVouchersCount.toString(),
          color = BrandAccent,
          badge = if (pendingVouchersCount > 0) "Upload" else null,
          modifier = Modifier.weight(1f)
        )
        AdminKpiTile(
          title = "Commission",
          value = "₹${"%.1f".format(totalCommissions)}",
          color = StatusSuccess,
          modifier = Modifier.weight(1f)
        )
      }

      // 2. Navigation Tabs
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = {
              Text(
                text = title,
                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp,
                maxLines = 1
              )
            },
            modifier = Modifier.testTag("admin_tab_$index")
          )
        }
      }

      // 3. Tab Contents
      when (selectedTab) {
        0 -> AdminTransactionsTab(
          transactions = allTransactions,
          onProcessTransaction = { txToProcess = it },
          onQuickSuccess = { tx ->
            val autoTrId = "OPR-" + (System.currentTimeMillis() % 100000000).toString()
            viewModel.adminUpdateTransaction(tx.id, autoTrId, "SUCCESS", "Approved by Admin instantly")
          },
          onQuickFail = { tx ->
            viewModel.adminUpdateTransaction(tx.id, "FAILED", "FAILED", "Marked Failed by Admin. Refund processed if paid via wallet.")
          }
        )
        1 -> AdminVoucherOrdersTab(
          vouchers = allUserVouchers,
          onUploadVoucher = { voucherToUpload = it }
        )
        2 -> AdminUsersTab(
          users = allUsers,
          onAdjustBalance = { userToAdjustBalance = it }
        )
        3 -> AdminCommissionsTab(
          providers = allProviders,
          onEditProvider = { providerToEdit = it },
          onAddProvider = { showAddProviderDialog = true },
          onToggleActive = { provider, active -> viewModel.adminToggleProvider(provider.id, active, provider.name) },
          onDeleteProvider = { provider -> viewModel.adminDeleteProvider(provider.id, provider.name) }
        )
      }
    }
  }

  // Dialog: Manual TR ID Entry
  txToProcess?.let { tx ->
    AdminProcessTransactionDialog(
      transaction = tx,
      onDismiss = { txToProcess = null },
      onConfirm = { trId, status, notes ->
        viewModel.adminUpdateTransaction(tx.id, trId, status, notes)
        txToProcess = null
      }
    )
  }

  // Dialog: Upload Genuine Voucher Code & PIN
  voucherToUpload?.let { vch ->
    AdminUploadGenuineVoucherDialog(
      voucher = vch,
      onDismiss = { voucherToUpload = null },
      onConfirm = { code, pin, expiry, notes ->
        viewModel.adminUploadGenuineVoucher(vch.id, code, pin, expiry, notes)
        voucherToUpload = null
      },
      onReject = { reason ->
        viewModel.adminRejectVoucher(vch.id, reason)
        voucherToUpload = null
      }
    )
  }

  // Dialog: Adjust Balance
  userToAdjustBalance?.let { user ->
    AdminAdjustBalanceDialog(
      user = user,
      onDismiss = { userToAdjustBalance = null },
      onConfirm = { addedAmount ->
        viewModel.adminAdjustUserBalance(user.id, addedAmount)
        userToAdjustBalance = null
      }
    )
  }

  // Dialog: Edit Operator Details & Margins
  providerToEdit?.let { provider ->
    AdminEditProviderDialog(
      provider = provider,
      onDismiss = { providerToEdit = null },
      onConfirm = { name, code, serviceType, commPercent, flatComm, isActive, note ->
        viewModel.adminUpdateProviderFull(provider.id, name, code, serviceType, commPercent, flatComm, isActive, note)
        providerToEdit = null
      }
    )
  }

  // Dialog: Add Service Provider
  if (showAddProviderDialog) {
    AdminAddProviderDialog(
      onDismiss = { showAddProviderDialog = false },
      onConfirm = { serviceType, name, code, comm, flat, note ->
        viewModel.adminAddProvider(serviceType, name, code, comm, flat, note)
        showAddProviderDialog = false
      }
    )
  }
}

@Composable
private fun AdminKpiTile(
  title: String,
  value: String,
  color: Color,
  badge: String? = null,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = title, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
      Spacer(modifier = Modifier.height(2.dp))
      Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color, maxLines = 1)
      if (badge != null) {
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = color.copy(alpha = 0.15f)
        ) {
          Text(text = badge, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
        }
      }
    }
  }
}

// TAB 1: Transactions
@Composable
private fun AdminTransactionsTab(
  transactions: List<TransactionEntity>,
  onProcessTransaction: (TransactionEntity) -> Unit,
  onQuickSuccess: (TransactionEntity) -> Unit,
  onQuickFail: (TransactionEntity) -> Unit
) {
  var filterStatus by remember { mutableStateOf("PENDING") }
  val statusOptions = listOf("PENDING", "ALL", "SUCCESS", "FAILED")
  var searchQuery by remember { mutableStateOf("") }

  val filteredList = remember(transactions, filterStatus, searchQuery) {
    transactions.filter { tx ->
      val matchesStatus = (filterStatus == "ALL") || tx.status == filterStatus
      val matchesSearch = searchQuery.isBlank() ||
        tx.userName.contains(searchQuery, ignoreCase = true) ||
        tx.userPhone.contains(searchQuery) ||
        tx.targetNumber.contains(searchQuery) ||
        tx.userUpiUtr.contains(searchQuery) ||
        tx.operatorTrId.contains(searchQuery)
      matchesStatus && matchesSearch
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      statusOptions.forEach { status ->
        FilterChip(
          selected = filterStatus == status,
          onClick = { filterStatus = status },
          label = {
            Text(
              text = if (status == "PENDING") "Pending (${transactions.count { it.status == "PENDING" }})" else status,
              fontSize = 11.sp
            )
          }
        )
      }
    }

    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search by phone, target ID, UTR or TR ID...") },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
      singleLine = true,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp)
        .testTag("admin_tx_search_input")
    )

    if (filteredList.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Text("No transactions found in this view.")
      }
    } else {
      LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredList) { tx ->
          AdminTransactionItemCard(
            transaction = tx,
            onProcess = { onProcessTransaction(tx) },
            onQuickSuccess = { onQuickSuccess(tx) },
            onQuickFail = { onQuickFail(tx) }
          )
        }
      }
    }
  }
}

@Composable
fun AdminTransactionItemCard(
  transaction: TransactionEntity,
  onProcess: () -> Unit,
  onQuickSuccess: () -> Unit,
  onQuickFail: () -> Unit
) {
  val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
  val dateStr = dateFormat.format(Date(transaction.createdAt))

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("admin_tx_card_${transaction.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
              Text(
                text = "#${transaction.id} • ${transaction.serviceType}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = dateStr,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "${transaction.providerName} - ${transaction.targetNumber}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "User: ${transaction.userName} (${transaction.userPhone})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "₹${"%.2f".format(transaction.amount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
          )
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = when (transaction.status) {
              "SUCCESS" -> StatusSuccess.copy(alpha = 0.15f)
              "PENDING" -> StatusPending.copy(alpha = 0.15f)
              else -> StatusFailed.copy(alpha = 0.15f)
            }
          ) {
            Text(
              text = transaction.status,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = when (transaction.status) {
                "SUCCESS" -> StatusSuccess
                "PENDING" -> StatusPending
                else -> StatusFailed
              },
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(8.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Method: ${transaction.paymentMethod} ${if (transaction.userUpiUtr.isNotBlank()) "• UTR: ${transaction.userUpiUtr}" else ""}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
          )
          if (transaction.operatorTrId.isNotBlank()) {
            Text(
              text = "TR: ${transaction.operatorTrId}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = BrandSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (transaction.status == "PENDING") {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Button(
            onClick = onQuickSuccess,
            modifier = Modifier
              .weight(1f)
              .height(38.dp)
              .testTag("admin_quick_success_${transaction.id}"),
            colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Success", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }

          Button(
            onClick = onQuickFail,
            modifier = Modifier
              .weight(1f)
              .height(38.dp)
              .testTag("admin_quick_fail_${transaction.id}"),
            colors = ButtonDefaults.buttonColors(containerColor = StatusFailed),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Fail", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }

          OutlinedButton(
            onClick = onProcess,
            modifier = Modifier
              .height(38.dp)
              .testTag("admin_process_btn_${transaction.id}"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("TR ID", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
          }
        }
      } else {
        OutlinedButton(
          onClick = onProcess,
          modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .testTag("admin_process_btn_${transaction.id}"),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Edit TR ID / Status Notes", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }
  }
}

// TAB 2: VOUCHER ORDERS & GENUINE VOUCHER UPLOAD
@Composable
private fun AdminVoucherOrdersTab(
  vouchers: List<UserVoucherEntity>,
  onUploadVoucher: (UserVoucherEntity) -> Unit
) {
  var filterStatus by remember { mutableStateOf("PENDING") }
  val statusOptions = listOf("PENDING", "ALL", "ACTIVE", "REJECTED")

  val filteredList = remember(vouchers, filterStatus) {
    if (filterStatus == "ALL") vouchers
    else vouchers.filter { it.status == filterStatus }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      statusOptions.forEach { status ->
        FilterChip(
          selected = filterStatus == status,
          onClick = { filterStatus = status },
          label = {
            Text(
              text = if (status == "PENDING") "Pending Upload (${vouchers.count { it.status == "PENDING" }})" else status,
              fontSize = 11.sp
            )
          }
        )
      }
    }

    if (filteredList.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Text("No voucher orders found in this view.")
      }
    } else {
      LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredList) { vch ->
          Card(
            modifier = Modifier.fillMaxWidth().testTag("admin_vch_card_${vch.id}"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
              ) {
                Column {
                  Text(
                    text = vch.brandName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Customer: ${vch.userName.ifBlank { "User #${vch.userId}" }} (${vch.userPhone.ifBlank { "9876543210" }})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "Denomination: ₹${vch.denomination.toInt()} • Paid: ₹${"%.2f".format(vch.finalPaid)} via ${vch.paymentMethod}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                  )
                }

                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = when (vch.status) {
                    "ACTIVE" -> StatusSuccess.copy(alpha = 0.15f)
                    "PENDING" -> StatusPending.copy(alpha = 0.15f)
                    else -> StatusFailed.copy(alpha = 0.15f)
                  }
                ) {
                  Text(
                    text = when (vch.status) {
                      "ACTIVE" -> "UPLOADED"
                      "PENDING" -> "UPLOAD PENDING"
                      else -> "REJECTED"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (vch.status) {
                      "ACTIVE" -> StatusSuccess
                      "PENDING" -> StatusPending
                      else -> StatusFailed
                    },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              if (vch.status == "ACTIVE") {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                  color = BrandSecondary.copy(alpha = 0.1f),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                      text = "Code: ${vch.voucherCode} • PIN: ${vch.pinCode}",
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace,
                      color = BrandSecondary
                    )
                    Text("Expiry: ${vch.expiryDate}", fontSize = 11.sp)
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              Button(
                onClick = { onUploadVoucher(vch) },
                modifier = Modifier.fillMaxWidth().height(38.dp).testTag("upload_voucher_btn_${vch.id}"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (vch.status == "PENDING") BrandAccent else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (vch.status == "PENDING") "Upload Genuine Voucher & Activate" else "Edit Voucher Details",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }
            }
          }
        }
      }
    }
  }
}

// Dialog: Upload Genuine Voucher Code
@Composable
fun AdminUploadGenuineVoucherDialog(
  voucher: UserVoucherEntity,
  onDismiss: () -> Unit,
  onConfirm: (code: String, pin: String, expiry: String, notes: String) -> Unit,
  onReject: (reason: String) -> Unit
) {
  var voucherCode by remember { mutableStateOf(voucher.voucherCode) }
  var pinCode by remember { mutableStateOf(voucher.pinCode.ifBlank { "4921" }) }
  var expiryDate by remember { mutableStateOf(voucher.expiryDate.ifBlank { "12 Oct 2027" }) }
  var notes by remember { mutableStateOf(voucher.adminNotes) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = BrandAccent)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Upload Genuine Brand Voucher", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Customer: ${voucher.userName.ifBlank { "Mukesh Garg" }}",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${voucher.brandName} (₹${voucher.denomination.toInt()}) • Paid via ${voucher.paymentMethod}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = voucherCode,
          onValueChange = { voucherCode = it },
          label = { Text("Genuine Voucher / Gift Card Code *") },
          placeholder = { Text("e.g. AMZN-4921-9920-LKP8") },
          trailingIcon = {
            Text(
              text = "GENERATE",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = BrandPrimary,
              modifier = Modifier
                .clickable {
                  voucherCode = "${voucher.brandName.take(3).uppercase()}-${(1000..9999).random()}-${(1000..9999).random()}-${(1000..9999).random()}"
                }
                .padding(8.dp)
            )
          },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("genuine_voucher_code_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = pinCode,
            onValueChange = { pinCode = it },
            label = { Text("Secret PIN") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = expiryDate,
            onValueChange = { expiryDate = it },
            label = { Text("Expiry Date") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Admin Remarks") },
          placeholder = { Text("e.g. Issued from brand corporate portal") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val finalCode = if (voucherCode.isBlank()) {
            "${voucher.brandName.take(3).uppercase()}-${(1000..9999).random()}-${(1000..9999).random()}-${(1000..9999).random()}"
          } else voucherCode
          onConfirm(finalCode, pinCode, expiryDate, notes)
        },
        modifier = Modifier.testTag("confirm_upload_voucher_btn")
      ) {
        Text("Upload & Activate")
      }
    },
    dismissButton = {
      Row {
        OutlinedButton(onClick = { onReject("Payment verification failed") }, colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusFailed)) {
          Text("Reject Order")
        }
        Spacer(modifier = Modifier.width(6.dp))
        OutlinedButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
    }
  )
}

// Dialog for manual admin TR ID submission
@Composable
fun AdminProcessTransactionDialog(
  transaction: TransactionEntity,
  onDismiss: () -> Unit,
  onConfirm: (trId: String, status: String, notes: String) -> Unit
) {
  var operatorTrId by remember { mutableStateOf(transaction.operatorTrId) }
  var status by remember { mutableStateOf(if (transaction.status == "PENDING") "SUCCESS" else transaction.status) }
  var notes by remember { mutableStateOf(transaction.adminNotes) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Manual Operator Processing", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Recharge of ₹${transaction.amount.toInt()} for ${transaction.providerName} (${transaction.targetNumber})",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = "Paid via: ${transaction.paymentMethod} ${if (transaction.userUpiUtr.isNotBlank()) "• UTR: ${transaction.userUpiUtr}" else ""}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = operatorTrId,
          onValueChange = { operatorTrId = it },
          label = { Text("Operator TR ID / UTR / Reference ID *") },
          placeholder = { Text("e.g. JIO-TR-90284719") },
          trailingIcon = {
            Text(
              text = "GENERATE",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = BrandPrimary,
              modifier = Modifier
                .clickable {
                  operatorTrId = "${transaction.providerName.take(3).uppercase()}-TR-" + (10000000..99999999).random().toString()
                }
                .padding(8.dp)
            )
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_operator_tr_id_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Status Outcome:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically) {
          RadioButton(selected = status == "SUCCESS", onClick = { status = "SUCCESS" })
          Text("SUCCESS (Completed)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = StatusSuccess)
          Spacer(modifier = Modifier.width(12.dp))
          RadioButton(selected = status == "FAILED", onClick = { status = "FAILED" })
          Text("FAILED (Refund)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = StatusFailed)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Admin Remarks / Note") },
          placeholder = { Text("e.g. Processed via partner API") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val finalTr = if (operatorTrId.isBlank() && status == "SUCCESS") {
            "${transaction.providerName.take(3).uppercase()}-TR-" + (10000000..99999999).random().toString()
          } else operatorTrId

          onConfirm(finalTr, status, notes)
        },
        modifier = Modifier.testTag("admin_save_tr_id_confirm_btn")
      ) {
        Text("Save & Update in System")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

// TAB 3: User Management
@Composable
private fun AdminUsersTab(
  users: List<UserEntity>,
  onAdjustBalance: (UserEntity) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  val filteredUsers = remember(users, searchQuery) {
    if (searchQuery.isBlank()) users
    else users.filter { it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery) }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search users by name or mobile number...") },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
      singleLine = true,
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
        .testTag("admin_search_user_input")
    )

    LazyColumn(
      contentPadding = PaddingValues(12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(filteredUsers) { user ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_user_card_${user.id}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(1.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(user.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = if (user.role == "ADMIN") BrandAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer
                ) {
                  Text(
                    text = user.role,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (user.role == "ADMIN") BrandAccent else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                  )
                }
              }
              Text(
                text = "Phone: ${user.phone} • Email: ${user.email}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "Wallet Balance: ₹${"%.2f".format(user.walletBalance)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = BrandSecondary
              )
            }

            Button(
              onClick = { onAdjustBalance(user) },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.height(36.dp)
            ) {
              Icon(Icons.Default.Paid, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Wallet ±", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

// Dialog: Adjust Balance
@Composable
fun AdminAdjustBalanceDialog(
  user: UserEntity,
  onDismiss: () -> Unit,
  onConfirm: (amountDelta: Double) -> Unit
) {
  var amountText by remember { mutableStateOf("500") }
  var isAdding by remember { mutableStateOf(true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Adjust Wallet Balance for ${user.name}") },
    text = {
      Column {
        Text("Current Wallet Balance: ₹${"%.2f".format(user.walletBalance)}")
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = isAdding,
            onClick = { isAdding = true },
            label = { Text("+ Add Funds") }
          )
          FilterChip(
            selected = !isAdding,
            onClick = { isAdding = false },
            label = { Text("- Deduct Funds") }
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it },
          label = { Text("Amount (₹)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(onClick = {
        val amount = amountText.toDoubleOrNull() ?: 0.0
        val delta = if (isAdding) amount else -amount
        onConfirm(delta)
      }) {
        Text("Apply Balance Update")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

// TAB 4: Commission Settings
@Composable
private fun AdminCommissionsTab(
  providers: List<ServiceProviderEntity>,
  onEditProvider: (ServiceProviderEntity) -> Unit,
  onAddProvider: () -> Unit,
  onToggleActive: (ServiceProviderEntity, Boolean) -> Unit,
  onDeleteProvider: (ServiceProviderEntity) -> Unit
) {
  var providerToDelete by remember { mutableStateOf<ServiceProviderEntity?>(null) }

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Operators & Commission (${providers.size})",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
      )

      Button(
        onClick = onAddProvider,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.height(36.dp).testTag("admin_add_provider_btn")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Add Operator", fontSize = 11.sp)
      }
    }

    LazyColumn(
      contentPadding = PaddingValues(12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(providers) { provider ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_provider_card_${provider.id}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (provider.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ),
          elevation = CardDefaults.cardElevation(1.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = provider.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (provider.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                  ) {
                    Text(
                      text = "${provider.serviceType} • ${provider.code}",
                      style = MaterialTheme.typography.labelSmall,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (provider.isActive) StatusSuccess.copy(alpha = 0.15f) else StatusFailed.copy(alpha = 0.15f)
                  ) {
                    Text(
                      text = if (provider.isActive) "ACTIVE" else "DISABLED",
                      style = MaterialTheme.typography.labelSmall,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (provider.isActive) StatusSuccess else StatusFailed,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Margin: ${provider.commissionPercent}% ${if (provider.flatCommission > 0) "+ ₹${provider.flatCommission} Flat" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandSecondary
                  )
                }
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                // Quick Toggle Active Switch
                Switch(
                  checked = provider.isActive,
                  onCheckedChange = { onToggleActive(provider, it) },
                  modifier = Modifier.testTag("admin_toggle_provider_${provider.id}")
                )
                IconButton(onClick = { onEditProvider(provider) }, modifier = Modifier.testTag("admin_edit_provider_${provider.id}")) {
                  Icon(Icons.Default.Edit, contentDescription = "Edit Operator", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { providerToDelete = provider }, modifier = Modifier.testTag("admin_delete_provider_${provider.id}")) {
                  Icon(Icons.Default.Delete, contentDescription = "Remove Operator", tint = MaterialTheme.colorScheme.error)
                }
              }
            }

            if (provider.helperNote.isNotBlank()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = provider.helperNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              )
            }
          }
        }
      }
    }
  }

  // Delete Confirmation Dialog
  providerToDelete?.let { prov ->
    AlertDialog(
      onDismissRequest = { providerToDelete = null },
      title = { Text("Delete Operator?") },
      text = { Text("Are you sure you want to remove operator '${prov.name}' (${prov.code})? Users will no longer see this operator in recharge or bill payment lists.") },
      confirmButton = {
        Button(
          onClick = {
            onDeleteProvider(prov)
            providerToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete Operator")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { providerToDelete = null }) { Text("Cancel") }
      }
    )
  }
}

// Dialog: Edit Operator Details, Margins/Discounts & Status
@Composable
fun AdminEditProviderDialog(
  provider: ServiceProviderEntity,
  onDismiss: () -> Unit,
  onConfirm: (name: String, code: String, serviceType: String, percent: Double, flat: Double, isActive: Boolean, note: String) -> Unit
) {
  var name by remember { mutableStateOf(provider.name) }
  var code by remember { mutableStateOf(provider.code) }
  var serviceType by remember { mutableStateOf(provider.serviceType) }
  var percentText by remember { mutableStateOf(provider.commissionPercent.toString()) }
  var flatText by remember { mutableStateOf(provider.flatCommission.toString()) }
  var isActive by remember { mutableStateOf(provider.isActive) }
  var note by remember { mutableStateOf(provider.helperNote) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit Operator: ${provider.name}") },
    text = {
      Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Operator / Biller Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = code,
            onValueChange = { code = it.uppercase() },
            label = { Text("Operator Code") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = serviceType,
            onValueChange = { serviceType = it.uppercase() },
            label = { Text("Type (MOBILE/DTH/BILL)") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = percentText,
          onValueChange = { percentText = it },
          label = { Text("Margin / Discount Percentage (%)") },
          placeholder = { Text("e.g. 2.5") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = flatText,
          onValueChange = { flatText = it },
          label = { Text("Flat Margin / Surcharge (₹)") },
          placeholder = { Text("e.g. 2.0") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text("Admin Note / Info") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Operator Active (Enabled):", fontWeight = FontWeight.SemiBold)
          Switch(checked = isActive, onCheckedChange = { isActive = it })
        }
      }
    },
    confirmButton = {
      Button(onClick = {
        val pct = percentText.toDoubleOrNull() ?: provider.commissionPercent
        val flat = flatText.toDoubleOrNull() ?: provider.flatCommission
        onConfirm(name.trim(), code.trim(), serviceType.trim(), pct, flat, isActive, note.trim())
      }) {
        Text("Save Operator")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

// Dialog: Add Service Provider
@Composable
fun AdminAddProviderDialog(
  onDismiss: () -> Unit,
  onConfirm: (serviceType: String, name: String, code: String, comm: Double, flat: Double, note: String) -> Unit
) {
  var serviceType by remember { mutableStateOf("MOBILE") }
  var name by remember { mutableStateOf("") }
  var code by remember { mutableStateOf("") }
  var commText by remember { mutableStateOf("2.0") }
  var flatText by remember { mutableStateOf("0.0") }
  var note by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Service Provider") },
    text = {
      Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("MOBILE", "DTH", "BILL").forEach { type ->
            FilterChip(
              selected = serviceType == type,
              onClick = { serviceType = type },
              label = { Text(type, fontSize = 11.sp) }
            )
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Provider Name (e.g. BSNL Special)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = code,
          onValueChange = { code = it },
          label = { Text("Provider Code (e.g. BSNL_SPL)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = commText,
          onValueChange = { commText = it },
          label = { Text("Commission (%)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        enabled = name.isNotBlank() && code.isNotBlank(),
        onClick = {
          val comm = commText.toDoubleOrNull() ?: 2.0
          val flat = flatText.toDoubleOrNull() ?: 0.0
          onConfirm(serviceType, name, code, comm, flat, note)
        }
      ) {
        Text("Add Provider")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.network.LiveBillResult
import com.example.ui.components.CheckoutPaymentSheet
import com.example.ui.theme.BrandSecondary
import com.example.ui.viewmodel.Pay4uViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillPaymentScreen(
  viewModel: Pay4uViewModel,
  onBack: () -> Unit
) {
  BackHandler { onBack() }

  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

  val billCategories = listOf(
    "Electricity" to Icons.Default.ElectricalServices,
    "Water" to Icons.Default.WaterDrop,
    "LPG Gas" to Icons.Default.LocalGasStation,
    "Broadband" to Icons.Default.Wifi,
    "Fastag" to Icons.Default.ReceiptLong
  )

  var selectedCategory by remember { mutableStateOf("Electricity") }

  val electricityBillers = listOf("BSES Rajdhani Delhi", "Tata Power Delhi", "Adani Electricity Mumbai", "BESCOM Bengaluru", "UPPCL Uttar Pradesh")
  val waterBillers = listOf("Delhi Jal Board (DJB)", "Bangalore Water Supply (BWSSB)", "Hyderabad Water (HMWSSB)")
  val gasBillers = listOf("Indane LPG Cylinder", "Bharat Gas (BPCL)", "HP Gas (HPCL)", "Indraprastha Gas (IGL Piped)")
  val broadbandBillers = listOf("JioFiber Broadband", "Airtel Xstream Fiber", "ACT Fibernet", "Hathway Fiber")
  val fastagBillers = listOf("Paytm Payments Bank Fastag", "ICICI Bank Fastag", "SBI Fastag", "HDFC Fastag")

  val currentBillers = remember(selectedCategory) {
    when (selectedCategory) {
      "Electricity" -> electricityBillers
      "Water" -> waterBillers
      "LPG Gas" -> gasBillers
      "Broadband" -> broadbandBillers
      else -> fastagBillers
    }
  }

  var selectedBiller by remember { mutableStateOf(electricityBillers.first()) }
  var billerDropdownOpen by remember { mutableStateOf(false) }
  var consumerId by remember { mutableStateOf("1002948194") }

  var isFetchingLive by remember { mutableStateOf(false) }
  var fetchedBill by remember { mutableStateOf<LiveBillResult?>(null) }
  var showPaymentSheet by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Bill Payment (Live BBPS)", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("back_button_bills")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
        .testTag("bill_payment_column")
    ) {
      // 1. Category Selector
      item {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Select Utility Category",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(10.dp))

          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(billCategories) { (cat, icon) ->
              val isSelected = selectedCategory == cat
              FilterChip(
                selected = isSelected,
                onClick = {
                  selectedCategory = cat
                  selectedBiller = when (cat) {
                    "Electricity" -> electricityBillers.first()
                    "Water" -> waterBillers.first()
                    "LPG Gas" -> gasBillers.first()
                    "Broadband" -> broadbandBillers.first()
                    else -> fastagBillers.first()
                  }
                  fetchedBill = null
                },
                label = { Text(cat) },
                leadingIcon = {
                  Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                  )
                }
              )
            }
          }
        }
      }

      // 2. Biller & Consumer ID Form
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Select Provider / Biller",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            Box {
              Surface(
                onClick = { billerDropdownOpen = true },
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStrokeSafe(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = selectedBiller,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
              }

              DropdownMenu(
                expanded = billerDropdownOpen,
                onDismissRequest = { billerDropdownOpen = false }
              ) {
                currentBillers.forEach { biller ->
                  DropdownMenuItem(
                    text = { Text(biller) },
                    onClick = {
                      selectedBiller = biller
                      billerDropdownOpen = false
                      fetchedBill = null
                    }
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
              value = consumerId,
              onValueChange = {
                consumerId = it
                fetchedBill = null
              },
              label = {
                Text(
                  when (selectedCategory) {
                    "Electricity" -> "Consumer CA Number / Account No."
                    "Water" -> "K-Number / Consumer ID"
                    "LPG Gas" -> "Consumer ID / Registered Mobile"
                    "Broadband" -> "Broadband Account ID / Landline"
                    else -> "Vehicle Registration No. / Fastag ID"
                  }
                )
              },
              placeholder = { Text("e.g. 1002948194") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("consumer_id_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = {
                isFetchingLive = true
                viewModel.fetchLiveBill(
                  category = selectedCategory,
                  billerName = selectedBiller,
                  consumerNumber = consumerId
                ) { result ->
                  isFetchingLive = false
                  fetchedBill = result
                }
              },
              enabled = consumerId.isNotBlank() && !isFetchingLive,
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("fetch_bill_btn"),
              shape = RoundedCornerShape(12.dp)
            ) {
              if (isFetchingLive) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  color = Color.White,
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calling Live Bill API...")
              } else {
                Icon(Icons.Default.Sensors, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fetch Real Bill via Network API", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // 3. Fetched Live Bill Details Card
      fetchedBill?.let { bill ->
        item {
          Spacer(modifier = Modifier.height(16.dp))
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp)
              .testTag("bill_details_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            ),
            border = BorderStrokeSafe(1.5.dp, MaterialTheme.colorScheme.primary)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = BrandSecondary.copy(alpha = 0.15f)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = null,
                      tint = BrandSecondary,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "API Verified (${bill.networkLatencyMs}ms)",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = BrandSecondary
                    )
                  }
                }

                Text(
                  text = "Due: ${bill.dueDate}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.error
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text("Consumer Name", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(bill.consumerName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text("Bill Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(
                    text = "₹${"%.2f".format(bill.amount)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Consumption: ${bill.unitsConsumed}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = bill.apiResponseStatus,
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 10.sp,
                  color = BrandSecondary
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                onClick = { showPaymentSheet = true },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("pay_bill_now_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.Bolt, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Proceed to Pay via Wallet / UPI / Card", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }

  if (showPaymentSheet && fetchedBill != null) {
    val currentBill = fetchedBill!!
    CheckoutPaymentSheet(
      amount = currentBill.amount,
      serviceTitle = "$selectedCategory Bill - $selectedBiller",
      targetDetails = "Consumer ID: ${currentBill.consumerNumber} (${currentBill.consumerName})",
      walletBalance = currentUser?.walletBalance ?: 0.0,
      onDismiss = { showPaymentSheet = false },
      onConfirmPayment = { method, ref ->
        showPaymentSheet = false
        viewModel.submitRechargeOrBill(
          serviceType = "BILL",
          category = selectedCategory,
          providerName = selectedBiller,
          targetNumber = currentBill.consumerNumber,
          circle = "Pan India",
          planDescription = "$selectedCategory Bill for ${currentBill.consumerName} (Due: ${currentBill.dueDate})",
          amount = currentBill.amount,
          paymentMethod = method,
          paymentRef = ref
        )
      }
    )
  }
}

@Composable
private fun BorderStrokeSafe(width: androidx.compose.ui.unit.Dp, color: Color) =
  androidx.compose.foundation.BorderStroke(width, color)

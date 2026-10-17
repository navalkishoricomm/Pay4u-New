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
import androidx.compose.runtime.LaunchedEffect
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
    "Piped Gas" to Icons.Default.LocalGasStation,
    "Broadband" to Icons.Default.Wifi,
    "FASTag" to Icons.Default.ReceiptLong,
    "Credit Card" to Icons.Default.ReceiptLong,
    "Loan Repayment" to Icons.Default.ReceiptLong,
    "Insurance" to Icons.Default.ReceiptLong,
    "Municipal Taxes" to Icons.Default.ReceiptLong,
    "Cable TV" to Icons.Default.Sensors
  )

  var selectedCategory by remember { mutableStateOf("Electricity") }

  val electricityBillers = listOf(
    // Delhi NCR
    "BSES Rajdhani Power Limited (BRPL) - Delhi",
    "BSES Yamuna Power Limited (BYPL) - Delhi",
    "Tata Power Delhi Distribution (TPDDL)",
    "New Delhi Municipal Council (NDMC) Electricity",
    "Military Engineer Services (MES) - Delhi Cantt",
    // Maharashtra
    "Maharashtra State Electricity Dist. (MSEDCL / Mahavitaran)",
    "Adani Electricity Mumbai Ltd",
    "Tata Power - Mumbai",
    "Brihanmumbai Electric Supply & Transport (BEST)",
    "Torrent Power - Bhiwandi / Shil / Kalwa",
    // Uttar Pradesh
    "UPPCL - Urban (Uttar Pradesh Power Corp)",
    "UPPCL - Rural (Uttar Pradesh Power Corp)",
    "Torrent Power - Agra",
    "Noida Power Company Ltd (NPCL)",
    "Kanpur Electricity Supply Co (KESCO)",
    // Karnataka
    "Bangalore Electricity Supply (BESCOM)",
    "Chamundeshwari Electricity (CHESCOM)",
    "Hubli Electricity Supply (HESCOM)",
    "Mangalore Electricity Supply (MESCOM)",
    "Gulbarga Electricity Supply (GESCOM)",
    "Hukkeri Rural Electric Co-operative Society (HRECS)",
    // Gujarat
    "Dakshin Gujarat Vij Company (DGVCL)",
    "Madhya Gujarat Vij Company (MGVCL)",
    "Paschim Gujarat Vij Company (PGVCL)",
    "Uttar Gujarat Vij Company (UGVCL)",
    "Torrent Power - Ahmedabad / Gandhinagar / Surat",
    "Torrent Power - Dahej",
    "GIFT Power Company Limited (GIFT City)",
    // Tamil Nadu & Puducherry
    "Tamil Nadu Generation & Dist. (TANGEDCO)",
    "Electricity Department Puducherry",
    // West Bengal
    "CESC Kolkata",
    "West Bengal State Electricity (WBSEDCL)",
    "India Power Corporation Limited (IPCL - Asansol)",
    // Punjab & Haryana & Chandigarh
    "Punjab State Power Corporation (PSPCL)",
    "Dakshin Haryana Bijli Vitran Nigam (DHBVN)",
    "Uttar Haryana Bijli Vitran Nigam (UHBVN)",
    "Chandigarh Electricity Department",
    // Rajasthan
    "Jaipur Vidyut Vitran Nigam (JVVNL)",
    "Jodhpur Vidyut Vitran Nigam (JdVVNL)",
    "Ajmer Vidyut Vitran Nigam (AVVNL)",
    "Kota Electricity Distribution Limited (KEDL)",
    "Bharatpur Electricity Services Limited (BESL)",
    "Bikaner Electricity Supply Limited (BKESL)",
    // Telangana & Andhra Pradesh
    "Telangana State Southern Power (TSSPDCL)",
    "Telangana State Northern Power (TSNPDCL)",
    "Andhra Pradesh Eastern Power (APEPDCL)",
    "Andhra Pradesh Southern Power (APSPDCL)",
    "Andhra Pradesh Central Power (APCPDCL)",
    // Bihar & Jharkhand
    "North Bihar Power Distribution (NBPDCL)",
    "South Bihar Power Distribution (SBPDCL)",
    "Jharkhand Bijli Vitran Nigam (JBVNL)",
    "Jamshedpur Utilities & Services Company (JUSCO / Tata Steel)",
    "Damodar Valley Corporation (DVC)",
    // Madhya Pradesh & Chhattisgarh
    "MP Poorv Kshetra Vidyut Vitran (Jabalpur)",
    "MP Paschim Kshetra Vidyut Vitran (Indore)",
    "MP Madhya Kshetra Vidyut Vitran (Bhopal)",
    "Chhattisgarh State Power Distribution (CSPDCL)",
    // Odisha
    "TP Central Odisha Distribution (TPCODL)",
    "TP Western Odisha Distribution (TPWODL)",
    "TP Southern Odisha Distribution (TPSODL)",
    "TP Northern Odisha Distribution (TPNODL)",
    // Kerala & Goa
    "Kerala State Electricity Board (KSEBL)",
    "Goa Electricity Department",
    // North East & Hills
    "Assam Power Distribution Company (APDCL)",
    "Uttarakhand Power Corporation (UPCL)",
    "Himachal Pradesh State Electricity Board (HPSEBL)",
    "Jammu Power Distribution Corp (JPDCL)",
    "Kashmir Power Distribution Corp (KPDCL)",
    "Ladakh Power Development Department (LPDD)",
    "Tripura State Electricity Corporation (TSECL)",
    "Meghalaya Power Distribution Corp (MePDCL)",
    "Manipur State Power Distribution (MSPDCL)",
    "Sikkim Power Distribution (SPDC)",
    "Nagaland Department of Power (DoPN)",
    "Power & Electricity Department Mizoram (PEDM)",
    "Department of Power Arunachal Pradesh (DoPAP)",
    // Union Territories
    "DNH Power Distribution Company (DNHPDCL)",
    "Daman and Diu Electricity Department",
    "Andaman and Nicobar Electricity Department",
    "Lakshadweep Electricity Department"
  )
  val waterBillers = listOf(
    "Delhi Jal Board (DJB)",
    "Bangalore Water Supply and Sewerage (BWSSB)",
    "Hyderabad Metropolitan Water (HMWSSB)",
    "Municipal Corp. of Greater Mumbai (MCGM)",
    "Chennai Metropolitan Water (CMWSSB)",
    "Pune Municipal Corporation (PMC) Water",
    "Kerala Water Authority (KWA)"
  )
  val lpgBillers = listOf(
    "Bharat Gas (BPCL)",
    "Indane Gas (Indian Oil Corporation)",
    "HP Gas (HPCL)"
  )
  val pipedGasBillers = listOf(
    "Indraprastha Gas Limited (IGL) - Delhi NCR",
    "Mahanagar Gas Limited (MGL) - Mumbai",
    "Adani Total Gas Limited",
    "Gujarat Gas Limited (GGL)",
    "Central UP Gas Limited (CUGL)",
    "Maharashtra Natural Gas Limited (MNGL)"
  )
  val broadbandBillers = listOf(
    "JioFiber Broadband",
    "Airtel Xstream Fiber",
    "ACT Fibernet",
    "BSNL Broadband & FTTH",
    "Hathway Fiber Broadband",
    "Excitel Broadband",
    "Tata Play Fiber"
  )
  val fastagBillers = listOf(
    "ICICI Bank FASTag",
    "State Bank of India (SBI) FASTag",
    "HDFC Bank FASTag",
    "Paytm Payments Bank FASTag",
    "Axis Bank FASTag",
    "Kotak Mahindra Bank FASTag",
    "IDFC FIRST Bank FASTag"
  )
  val creditCardBillers = listOf(
    "HDFC Bank Credit Card",
    "SBI Card",
    "ICICI Bank Credit Card",
    "Axis Bank Credit Card",
    "Kotak Mahindra Bank Credit Card",
    "IndusInd Bank Credit Card",
    "RBL Bank Credit Card"
  )
  val loanBillers = listOf(
    "Bajaj Finance Limited",
    "Muthoot Finance",
    "Tata Capital Financial Services",
    "HDB Financial Services",
    "Mahindra & Mahindra Finance",
    "L&T Financial Services",
    "Hero FinCorp",
    "DMI Finance"
  )
  val insuranceBillers = listOf(
    "Life Insurance Corporation of India (LIC)",
    "HDFC Life Insurance",
    "ICICI Prudential Life Insurance",
    "SBI Life Insurance",
    "Max Life Insurance",
    "Star Health and Allied Insurance"
  )
  val municipalBillers = listOf(
    "Municipal Corporation of Delhi (MCD)",
    "Greater Chennai Corporation Property Tax",
    "Bruhat Bengaluru Mahanagara Palike (BBMP)",
    "Brihanmumbai Municipal Corp (BMC / MCGM)",
    "Pune Municipal Corporation (PMC)"
  )
  val cableTvBillers = listOf(
    "Hathway Digital Cable TV",
    "Den Networks",
    "Siti Digital Cable TV",
    "GTPL Hathway Cable",
    "Asianet Digital Network"
  )

  val allProviders by viewModel.allProviders.collectAsStateWithLifecycle()
  val disabledProviderNames = remember(allProviders) {
    allProviders.filter { !it.isActive }.map { it.name.lowercase() }.toSet()
  }

  val currentBillers = remember(selectedCategory, disabledProviderNames) {
    val rawList = when (selectedCategory) {
      "Electricity" -> electricityBillers
      "Water" -> waterBillers
      "LPG Gas" -> lpgBillers
      "Piped Gas" -> pipedGasBillers
      "Broadband" -> broadbandBillers
      "FASTag" -> fastagBillers
      "Credit Card" -> creditCardBillers
      "Loan Repayment" -> loanBillers
      "Insurance" -> insuranceBillers
      "Municipal Taxes" -> municipalBillers
      "Cable TV" -> cableTvBillers
      else -> electricityBillers
    }
    val activeOnly = rawList.filter { it.lowercase() !in disabledProviderNames }
    if (activeOnly.isEmpty()) rawList else activeOnly
  }

  var selectedBiller by remember { mutableStateOf(electricityBillers.first()) }

  LaunchedEffect(currentBillers) {
    if (currentBillers.isNotEmpty() && selectedBiller !in currentBillers) {
      selectedBiller = currentBillers.first()
    }
  }

  var billerDropdownOpen by remember { mutableStateOf(false) }
  var consumerId by remember { mutableStateOf("") }

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
                    "LPG Gas" -> lpgBillers.first()
                    "Piped Gas" -> pipedGasBillers.first()
                    "Broadband" -> broadbandBillers.first()
                    "FASTag" -> fastagBillers.first()
                    "Credit Card" -> creditCardBillers.first()
                    "Loan Repayment" -> loanBillers.first()
                    "Insurance" -> insuranceBillers.first()
                    "Municipal Taxes" -> municipalBillers.first()
                    "Cable TV" -> cableTvBillers.first()
                    else -> electricityBillers.first()
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
                    "Piped Gas" -> "BP Number / Account ID"
                    "Broadband" -> "Broadband Account ID / Landline"
                    "FASTag" -> "Vehicle Registration No."
                    "Credit Card" -> "Last 4 Digits & Mobile"
                    "Loan Repayment" -> "Loan Account Number (LAN)"
                    "Insurance" -> "Policy Number"
                    "Municipal Taxes" -> "Property Tax Assessment ID"
                    "Cable TV" -> "Subscriber Code / MAC ID"
                    else -> "Account / Consumer ID"
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

              if (bill.billerId.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = "BBPS ID: ${bill.billerId}",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                      text = bill.billPeriod,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
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

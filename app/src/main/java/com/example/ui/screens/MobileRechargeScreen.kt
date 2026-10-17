package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RechargePlan
import com.example.data.repository.PlansData
import com.example.ui.components.UpiPaymentSheet
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.viewmodel.Pay4uViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileRechargeScreen(
  viewModel: Pay4uViewModel,
  onBack: () -> Unit
) {
  BackHandler { onBack() }

  var mobileNumber by remember { mutableStateOf("9876543210") }
  var selectedOperator by remember { mutableStateOf("Jio") }
  var selectedCircle by remember { mutableStateOf("Delhi NCR") }
  var isPrepaid by remember { mutableStateOf(true) }
  var circleMenuExpanded by remember { mutableStateOf(false) }

  var selectedCategoryIndex by remember { mutableStateOf(0) }
  var customAmountText by remember { mutableStateOf("") }
  var selectedPlan by remember { mutableStateOf<RechargePlan?>(null) }

  var showPaymentSheet by remember { mutableStateOf(false) }

  val allProviders by viewModel.allProviders.collectAsStateWithLifecycle()
  val operators = remember(allProviders) {
    val dbMobile = allProviders.filter { it.serviceType == "MOBILE" }
    if (dbMobile.isEmpty()) listOf("Jio", "Airtel", "Vi", "BSNL")
    else {
      val activeNames = dbMobile.filter { it.isActive }.map { it.name }
      if (activeNames.isEmpty()) listOf("Jio", "Airtel", "Vi", "BSNL") else activeNames
    }
  }

  LaunchedEffect(operators) {
    if (operators.isNotEmpty() && selectedOperator !in operators) {
      selectedOperator = operators.first()
    }
  }

  val plans = remember(selectedOperator) { PlansData.getPlansForOperator(selectedOperator) }
  val planCategories = listOf("All Plans", "Popular", "True 5G Unlimited", "Data Booster", "Annual")

  val filteredPlans = remember(plans, selectedCategoryIndex) {
    if (selectedCategoryIndex == 0) plans
    else {
      val cat = planCategories[selectedCategoryIndex]
      plans.filter { it.category.equals(cat, ignoreCase = true) }
    }
  }

  val finalPayableAmount = selectedPlan?.amount ?: customAmountText.toDoubleOrNull() ?: 0.0

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("Mobile Recharge", fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("back_button_mobile")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      if (finalPayableAmount > 0) {
        Surface(
          shadowElevation = 8.dp,
          color = MaterialTheme.colorScheme.surface
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "Payable Amount",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "₹${"%.0f".format(finalPayableAmount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
              )
              if (selectedPlan != null) {
                Text(
                  text = "${selectedPlan?.validity} | ${selectedPlan?.data}",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  color = BrandSecondary
                )
              }
            }

            Button(
              onClick = { showPaymentSheet = true },
              modifier = Modifier
                .height(48.dp)
                .testTag("proceed_mobile_recharge_btn"),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Bolt, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Pay via UPI", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
        .testTag("mobile_recharge_column")
    ) {
      // 1. Mobile Number Input
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              FilterChip(
                selected = isPrepaid,
                onClick = { isPrepaid = true },
                label = { Text("Prepaid") },
                leadingIcon = if (isPrepaid) {
                  { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                modifier = Modifier.weight(1f)
              )
              FilterChip(
                selected = !isPrepaid,
                onClick = { isPrepaid = false },
                label = { Text("Postpaid") },
                leadingIcon = if (!isPrepaid) {
                  { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                modifier = Modifier.weight(1f)
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
              value = mobileNumber,
              onValueChange = {
                if (it.length <= 10) {
                  mobileNumber = it
                  // Auto detect operator from prefix
                  if (it.startsWith("98") || it.startsWith("99") || it.startsWith("70")) selectedOperator = "Airtel"
                  else if (it.startsWith("6") || it.startsWith("7") || it.startsWith("8")) selectedOperator = "Jio"
                  else if (it.startsWith("91") || it.startsWith("92")) selectedOperator = "Vi"
                }
              },
              label = { Text("Mobile Number (10 digits)") },
              leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
              trailingIcon = {
                Icon(
                  Icons.Default.ContactPhone,
                  contentDescription = "Contacts",
                  tint = MaterialTheme.colorScheme.primary
                )
              },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("mobile_number_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Operator & Circle Selectors
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Circle Dropdown
              Box(modifier = Modifier.weight(1f)) {
                Surface(
                  onClick = { circleMenuExpanded = true },
                  shape = RoundedCornerShape(10.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  border = BorderStrokeSafe(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text("Circle", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                      Text(selectedCircle, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                  }
                }

                DropdownMenu(
                  expanded = circleMenuExpanded,
                  onDismissRequest = { circleMenuExpanded = false }
                ) {
                  PlansData.circles.forEach { circle ->
                    DropdownMenuItem(
                      text = { Text(circle) },
                      onClick = {
                        selectedCircle = circle
                        circleMenuExpanded = false
                      }
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "Select Operator:",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              operators.forEach { op ->
                val isOpSelected = selectedOperator.equals(op, ignoreCase = true)
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                      if (isOpSelected) MaterialTheme.colorScheme.primaryContainer
                      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                    .border(
                      width = if (isOpSelected) 1.5.dp else 1.dp,
                      color = if (isOpSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                      shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
                      selectedOperator = op
                      selectedPlan = null
                    }
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = op,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isOpSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isOpSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }
      }

      // 2. Custom Amount Input Option
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = customAmountText,
              onValueChange = {
                customAmountText = it
                if (it.isNotBlank()) selectedPlan = null
              },
              label = { Text("Or Enter Amount (₹)") },
              placeholder = { Text("e.g. 199, 299, 499") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .testTag("custom_amount_input")
            )
          }
        }
      }

      // 3. Plan Category Tabs
      item {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "Browse Plans for $selectedOperator ($selectedCircle)",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))

        ScrollableTabRow(
          selectedTabIndex = selectedCategoryIndex,
          edgePadding = 16.dp,
          containerColor = MaterialTheme.colorScheme.background,
          contentColor = MaterialTheme.colorScheme.primary
        ) {
          planCategories.forEachIndexed { idx, category ->
            Tab(
              selected = selectedCategoryIndex == idx,
              onClick = { selectedCategoryIndex = idx },
              text = {
                Text(
                  text = category,
                  fontWeight = if (selectedCategoryIndex == idx) FontWeight.Bold else FontWeight.Normal
                )
              }
            )
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
      }

      // 4. Plan Items List
      items(filteredPlans) { plan ->
        val isChosen = selectedPlan?.id == plan.id
        Card(
          onClick = {
            selectedPlan = plan
            customAmountText = ""
          },
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("plan_card_${plan.id}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isChosen) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
          ),
          border = if (isChosen) BorderStrokeSafe(1.5.dp, MaterialTheme.colorScheme.primary) else null,
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "₹${"%.0f".format(plan.amount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
              )
              Surface(
                color = BrandSecondary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = plan.category,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = BrandSecondary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              Column {
                Text("Validity", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(plan.validity, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
              }
              Column {
                Text("Data", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(plan.data, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
              }
              Column {
                Text("Voice", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(plan.voice, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = plan.description,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }

  // Checkout Payment Sheet Modal
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  if (showPaymentSheet) {
    com.example.ui.components.CheckoutPaymentSheet(
      amount = finalPayableAmount,
      serviceTitle = "Mobile Recharge ($selectedOperator - $mobileNumber)",
      targetDetails = "Recharge of ₹${finalPayableAmount.toInt()} for $mobileNumber ($selectedOperator)",
      walletBalance = currentUser?.walletBalance ?: 0.0,
      onDismiss = { showPaymentSheet = false },
      onConfirmPayment = { method, ref ->
        showPaymentSheet = false
        val planDesc = selectedPlan?.let { "₹${it.amount.toInt()} - ${it.data} (${it.validity})" } ?: "Custom Recharge ₹${finalPayableAmount.toInt()}"
        viewModel.submitRechargeOrBill(
          serviceType = "MOBILE",
          category = if (isPrepaid) "Prepaid" else "Postpaid",
          providerName = selectedOperator,
          targetNumber = mobileNumber,
          circle = selectedCircle,
          planDescription = planDesc,
          amount = finalPayableAmount,
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

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.ui.theme.BrandSecondary
import com.example.ui.viewmodel.Pay4uViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DthRechargeScreen(
  viewModel: Pay4uViewModel,
  onBack: () -> Unit
) {
  BackHandler { onBack() }

  var selectedOperator by remember { mutableStateOf("Tata Play") }
  var subscriberId by remember { mutableStateOf("1098472910") }
  var amountText by remember { mutableStateOf("399") }
  var selectedPlan by remember { mutableStateOf<RechargePlan?>(null) }
  var showPaymentSheet by remember { mutableStateOf(false) }

  val dthOperators = listOf(
    "Tata Play",
    "Airtel Digital TV",
    "Dish TV",
    "Sun Direct",
    "D2H"
  )

  val quickAmounts = listOf("249", "399", "499", "799", "1200", "2499")
  val plans = remember(selectedOperator) { PlansData.getPlansForOperator(selectedOperator) }
  val payableAmount = selectedPlan?.amount ?: amountText.toDoubleOrNull() ?: 0.0

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("DTH Recharge", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("back_button_dth")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    },
    bottomBar = {
      if (payableAmount > 0 && subscriberId.isNotBlank()) {
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
                text = "₹${"%.0f".format(payableAmount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = "$selectedOperator • Sub ID: $subscriberId",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = BrandSecondary
              )
            }

            Button(
              onClick = { showPaymentSheet = true },
              modifier = Modifier
                .height(48.dp)
                .testTag("proceed_dth_pay_btn"),
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
        .testTag("dth_recharge_column")
    ) {
      // 1. Operator Selection
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Select DTH Service Provider",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(dthOperators) { op ->
                val isSelected = selectedOperator.equals(op, ignoreCase = true)
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                      if (isSelected) MaterialTheme.colorScheme.primaryContainer
                      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                    .border(
                      width = if (isSelected) 1.5.dp else 1.dp,
                      color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                      shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                      selectedOperator = op
                      selectedPlan = null
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                  Text(
                    text = op,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subscriber ID Input
            OutlinedTextField(
              value = subscriberId,
              onValueChange = { subscriberId = it },
              label = { Text("Subscriber ID / Smart Card / VC Number") },
              placeholder = { Text("e.g. 1098472910") },
              leadingIcon = { Icon(Icons.Default.Tv, contentDescription = null) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("dth_subscriber_id_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recharge Amount Input
            OutlinedTextField(
              value = amountText,
              onValueChange = {
                amountText = it
                selectedPlan = null
              },
              label = { Text("Recharge Amount (₹)") },
              placeholder = { Text("e.g. 399") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("dth_amount_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "Quick Amounts:",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              quickAmounts.forEach { amt ->
                Surface(
                  onClick = {
                    amountText = amt
                    selectedPlan = null
                  },
                  shape = RoundedCornerShape(8.dp),
                  color = if (amountText == amt) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  border = BorderStrokeSafe(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                  modifier = Modifier.weight(1f)
                ) {
                  Text(
                    text = "₹$amt",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (amountText == amt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 6.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                  )
                }
              }
            }
          }
        }
      }

      // 2. Recommended DTH Plans & Packages
      item {
        Text(
          text = "Recommended Packs for $selectedOperator",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
      }

      items(plans) { plan ->
        val isChosen = selectedPlan?.id == plan.id
        Card(
          onClick = {
            selectedPlan = plan
            amountText = plan.amount.toInt().toString()
          },
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("dth_plan_${plan.id}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isChosen) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
          ),
          border = if (isChosen) BorderStrokeSafe(1.5.dp, MaterialTheme.colorScheme.primary) else null
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
                  text = plan.validity,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = BrandSecondary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = plan.voice, // Contains pack title
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold
            )
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

  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  if (showPaymentSheet) {
    com.example.ui.components.CheckoutPaymentSheet(
      amount = payableAmount,
      serviceTitle = "DTH Recharge ($selectedOperator)",
      targetDetails = "DTH ID: $subscriberId | Operator: $selectedOperator",
      walletBalance = currentUser?.walletBalance ?: 0.0,
      onDismiss = { showPaymentSheet = false },
      onConfirmPayment = { method, ref ->
        showPaymentSheet = false
        val planDesc = selectedPlan?.description ?: "DTH Recharge of ₹${payableAmount.toInt()}"
        viewModel.submitRechargeOrBill(
          serviceType = "DTH",
          category = "TV Recharge",
          providerName = selectedOperator,
          targetNumber = subscriberId,
          circle = "Pan India",
          planDescription = planDesc,
          amount = payableAmount,
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

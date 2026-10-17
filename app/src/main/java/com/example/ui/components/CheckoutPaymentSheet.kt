package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.StatusFailed
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutPaymentSheet(
  amount: Double,
  serviceTitle: String,
  targetDetails: String,
  walletBalance: Double,
  onDismiss: () -> Unit,
  onConfirmPayment: (paymentMethod: String, paymentRef: String) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val clipboardManager = LocalClipboardManager.current
  val merchantUpiId = "pay4u.recharge@icici"

  var selectedTab by remember { mutableStateOf(0) }
  val tabs = listOf("UPI Payment", "Wallet (₹${"%.0f".format(walletBalance)})")

  // UPI State
  var selectedUpiApp by remember { mutableStateOf("Google Pay") }
  var userUtr by remember { mutableStateOf("") }
  var copiedNotice by remember { mutableStateOf(false) }
  val upiApps = listOf("Google Pay", "PhonePe", "Paytm", "BHIM UPI", "Cred")

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier.testTag("checkout_payment_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Select Payment Method",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = serviceTitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = RoundedCornerShape(12.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "256-Bit Secure",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Amount Banner
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Total Payable Amount",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "₹${"%.2f".format(amount)}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
          )
          if (targetDetails.isNotBlank()) {
            Text(
              text = targetDetails,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Payment Tabs: UPI, Wallet, Credit Card
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
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
            modifier = Modifier.testTag("payment_tab_$index")
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      when (selectedTab) {
        0 -> {
          // TAB 0: UPI PAYMENT
          Column(modifier = Modifier.fillMaxWidth()) {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = CardDefaults.outlinedCardBorder()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text("Merchant UPI ID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(
                    text = merchantUpiId,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                  )
                }
                IconButton(
                  onClick = {
                    clipboardManager.setText(AnnotatedString(merchantUpiId))
                    copiedNotice = true
                  }
                ) {
                  Icon(
                    imageVector = if (copiedNotice) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                    contentDescription = "Copy UPI ID",
                    tint = if (copiedNotice) BrandSecondary else MaterialTheme.colorScheme.primary
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Select UPI App:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              upiApps.forEach { app ->
                val isSelected = selectedUpiApp == app
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier
                    .weight(1f)
                    .clickable { selectedUpiApp = app }
                    .padding(4.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(44.dp)
                      .clip(CircleShape)
                      .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                      .border(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.FlashOn,
                      contentDescription = null,
                      tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = app.split(" ").first(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
              value = userUtr,
              onValueChange = { if (it.length <= 16) userUtr = it },
              label = { Text("12-Digit UPI Ref / UTR Number *") },
              placeholder = { Text("Enter UTR from payment app (e.g. 428901847291)") },
              leadingIcon = { Icon(Icons.Default.QrCode2, contentDescription = null) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              isError = userUtr.isNotBlank() && userUtr.trim().length < 6,
              modifier = Modifier.fillMaxWidth().testTag("upi_utr_input_field")
            )

            if (userUtr.isNotBlank() && userUtr.trim().length < 6) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Please enter valid UPI Reference / UTR Number (minimum 6 digits)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
              color = StatusPending.copy(alpha = 0.12f),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Info, contentDescription = null, tint = StatusPending, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Enter the authentic UPI Transaction ID (UTR) generated in your payment app. Admin will verify and process your order.",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val isUtrValid = userUtr.trim().length >= 6
            Button(
              onClick = {
                if (isUtrValid) {
                  onConfirmPayment("UPI", "$selectedUpiApp (UTR: ${userUtr.trim()})")
                }
              },
              enabled = isUtrValid,
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_upi_checkout_btn"),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isUtrValid) "Submit UPI Payment (₹${"%.0f".format(amount)})" else "Enter UPI UTR Number to Submit",
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        1 -> {
          // TAB 1: WALLET BALANCE WITH STRICT VALIDATION
          val isBalanceSufficient = walletBalance >= amount
          val shortfall = (amount - walletBalance).coerceAtLeast(0.0)

          Column(modifier = Modifier.fillMaxWidth()) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isBalanceSufficient) StatusSuccess.copy(alpha = 0.1f)
                else StatusFailed.copy(alpha = 0.1f)
              ),
              border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (isBalanceSufficient) StatusSuccess else StatusFailed
              )
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.AccountBalanceWallet,
                      contentDescription = null,
                      tint = if (isBalanceSufficient) StatusSuccess else StatusFailed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "pay4u Wallet Balance",
                      fontWeight = FontWeight.Bold,
                      style = MaterialTheme.typography.titleSmall
                    )
                  }
                  Text(
                    text = "₹${"%.2f".format(walletBalance)}",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isBalanceSufficient) StatusSuccess else StatusFailed
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isBalanceSufficient) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Sufficient balance. Remaining after debit: ₹${"%.2f".format(walletBalance - amount)}",
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.SemiBold,
                      color = StatusSuccess
                    )
                  }
                } else {
                  Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = StatusFailed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                      Text(
                        text = "Insufficient Wallet Balance!",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = StatusFailed
                      )
                      Text(
                        text = "You need ₹${"%.2f".format(amount)} to purchase this item, but your wallet only has ₹${"%.2f".format(walletBalance)} (Short by ₹${"%.2f".format(shortfall)}). Please switch to UPI tab above to complete payment.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
              onClick = {
                if (isBalanceSufficient) {
                  onConfirmPayment("WALLET", "Wallet Debit (Balance Remaining: ₹${"%.2f".format(walletBalance - amount)})")
                } else {
                  onConfirmPayment("WALLET", "Wallet Debit Failed (Shortfall: ₹${"%.2f".format(shortfall)})")
                }
              },
              enabled = true,
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("pay_from_wallet_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isBalanceSufficient) StatusSuccess else StatusFailed
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isBalanceSufficient) "Pay ₹${"%.0f".format(amount)} from Wallet" else "Attempt Wallet Payment (Records FAILED)",
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}

// Compatibility wrapper for UpiPaymentSheet
@Composable
fun UpiPaymentSheet(
  amount: Double,
  serviceTitle: String,
  targetDetails: String,
  onDismiss: () -> Unit,
  onConfirmPayment: (upiUtr: String, upiApp: String) -> Unit
) {
  CheckoutPaymentSheet(
    amount = amount,
    serviceTitle = serviceTitle,
    targetDetails = targetDetails,
    walletBalance = 1250.0,
    onDismiss = onDismiss,
    onConfirmPayment = { method, ref ->
      onConfirmPayment(ref, method)
    }
  )
}

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
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
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
  val context = LocalContext.current

  // PhonePe Payment Gateway Credentials
  val phonePeMerchantPhone = "9810610554"
  val phonePeVpa = "9810610554@ybl"
  val phonePeColor = Color(0xFF5F259F)

  var selectedTab by remember { mutableStateOf(0) }
  val tabs = listOf("PhonePe / UPI Gateway", "Wallet (₹${"%.0f".format(walletBalance)})")

  // UPI / PhonePe State
  var selectedUpiApp by remember { mutableStateOf("PhonePe") }
  var userUtr by remember { mutableStateOf("") }
  var copiedNotice by remember { mutableStateOf(false) }
  val upiApps = listOf("PhonePe", "Google Pay", "Paytm", "BHIM UPI", "Cred")

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
          color = phonePeColor.copy(alpha = 0.12f),
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
              tint = phonePeColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "PhonePe Gateway",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = phonePeColor
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

      // Payment Tabs: PhonePe / UPI vs Wallet
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
          // TAB 0: PHONEPE & UPI GATEWAY
          Column(modifier = Modifier.fillMaxWidth()) {
            // PhonePe Merchant Gateway Card
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = phonePeColor.copy(alpha = 0.08f)
              ),
              border = androidx.compose.foundation.BorderStroke(1.5.dp, phonePeColor.copy(alpha = 0.35f))
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(phonePeColor),
                      contentAlignment = Alignment.Center
                    ) {
                      Text("पे", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = "PhonePe Gateway",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = phonePeColor
                      )
                      Text(
                        text = "Merchant A/C: $phonePeMerchantPhone",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }

                  Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = StatusSuccess.copy(alpha = 0.15f)
                  ) {
                    Text(
                      text = "VERIFIED",
                      style = MaterialTheme.typography.labelSmall,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = StatusSuccess,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // VPA and Copy Row
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surface,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text("Official Merchant VPA", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                      Text(
                        text = phonePeVpa,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = phonePeColor
                      )
                    }

                    IconButton(
                      onClick = {
                        clipboardManager.setText(AnnotatedString(phonePeVpa))
                        copiedNotice = true
                      },
                      modifier = Modifier.size(28.dp)
                    ) {
                      Icon(
                        imageVector = if (copiedNotice) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                        contentDescription = "Copy VPA",
                        tint = if (copiedNotice) BrandSecondary else phonePeColor,
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // One-click Launch PhonePe Button
                Button(
                  onClick = {
                    val cleanTarget = targetDetails.filter { it.isLetterOrDigit() }.take(15)
                    val note = if (cleanTarget.isNotBlank()) "Pay4u-$cleanTarget" else "Pay4u-Recharge"
                    val intentUri = "upi://pay?pa=$phonePeVpa&pn=Pay4u&mc=0000&tr=TXN${System.currentTimeMillis()}&tn=$note&am=${"%.2f".format(amount)}&cu=INR"

                    val phonePeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(intentUri)).apply {
                      setPackage("com.phonepe.app")
                    }

                    try {
                      context.startActivity(phonePeIntent)
                    } catch (e: Exception) {
                      val chooserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(intentUri))
                      try {
                        context.startActivity(Intent.createChooser(chooserIntent, "Pay via PhonePe or other UPI App"))
                      } catch (ex: Exception) {
                        Toast.makeText(context, "Please copy UPI VPA $phonePeVpa to pay.", Toast.LENGTH_LONG).show()
                      }
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = phonePeColor),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("launch_phonepe_btn")
                ) {
                  Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "⚡ Launch & Pay via PhonePe App",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Or select installed UPI App:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              upiApps.forEach { app ->
                val isSelected = selectedUpiApp == app
                val isPhonePeApp = app == "PhonePe"
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
                      .background(if (isSelected) (if (isPhonePeApp) phonePeColor else MaterialTheme.colorScheme.primary) else MaterialTheme.colorScheme.surfaceVariant)
                      .border(if (isSelected) 2.dp else 1.dp, if (isSelected) (if (isPhonePeApp) phonePeColor else MaterialTheme.colorScheme.primary) else Color.Transparent, CircleShape),
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
                    color = if (isSelected) (if (isPhonePeApp) phonePeColor else MaterialTheme.colorScheme.primary) else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
              value = userUtr,
              onValueChange = { if (it.length <= 16) userUtr = it },
              label = { Text("12-Digit PhonePe / UPI UTR Number *") },
              placeholder = { Text("e.g. 428901847291 (from payment details)") },
              leadingIcon = { Icon(Icons.Default.QrCode2, contentDescription = null) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              isError = userUtr.isNotBlank() && userUtr.trim().length < 6,
              modifier = Modifier.fillMaxWidth().testTag("upi_utr_input_field")
            )

            if (userUtr.isNotBlank() && userUtr.trim().length < 6) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Please enter valid UTR / Transaction ID (minimum 6 digits)",
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
                  text = "Enter the authentic UTR generated in PhonePe or your UPI app. Verified through PhonePe Gateway.",
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
                  onConfirmPayment("UPI", "PhonePe Gateway: $selectedUpiApp (UTR: ${userUtr.trim()})")
                }
              },
              enabled = isUtrValid,
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_upi_checkout_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedUpiApp == "PhonePe") phonePeColor else MaterialTheme.colorScheme.primary
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isUtrValid) "Submit Payment via PhonePe Gateway (₹${"%.0f".format(amount)})" else "Enter UTR Number to Confirm",
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

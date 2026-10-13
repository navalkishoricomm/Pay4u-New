package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.TransactionEntity
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.Pay4uViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun HomeScreen(
  viewModel: Pay4uViewModel,
  onNavigateToMobile: () -> Unit,
  onNavigateToDth: () -> Unit,
  onNavigateToBills: () -> Unit,
  onNavigateToCreditCard: () -> Unit,
  onNavigateToVouchers: () -> Unit,
  onNavigateToHistory: () -> Unit,
  onNavigateToAdmin: () -> Unit
) {
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val userTransactions by viewModel.userTransactions.collectAsStateWithLifecycle()
  val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
  val pendingCount = allTransactions.count { it.status == "PENDING" }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("home_screen_content"),
    contentPadding = PaddingValues(bottom = 80.dp)
  ) {
    // 1. Top Bar & Branding
    item {
      TopBrandBar(
        userName = currentUser?.name ?: "Guest User",
        isAdmin = currentUser?.role == "ADMIN",
        pendingAdminCount = pendingCount,
        onToggleAdmin = onNavigateToAdmin,
        onProfileClick = { viewModel.navigateTo(Screen.PROFILE) }
      )
    }

    // 2. Wallet & Balance Card
    item {
      WalletOverviewCard(
        balance = currentUser?.walletBalance ?: 0.0,
        userName = currentUser?.name ?: "User",
        userPhone = currentUser?.phone ?: "9876543210",
        onAddFunds = { viewModel.navigateTo(Screen.PROFILE) },
        onHistory = onNavigateToHistory
      )
    }

    // 3. Hero Promo Banner
    item {
      HeroPromoCard(onExploreVouchers = onNavigateToVouchers)
    }

    // 4. Primary Service Grid (Recharge & Bill Payment)
    item {
      Text(
        text = "Recharge & Bill Pay",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
      )
      ServiceGrid(
        onMobile = onNavigateToMobile,
        onDth = onNavigateToDth,
        onBills = onNavigateToBills,
        onCreditCard = onNavigateToCreditCard,
        onVouchers = onNavigateToVouchers
      )
    }

    // 5. Brand Vouchers Spotlight
    item {
      Spacer(modifier = Modifier.height(16.dp))
      BrandVoucherTeaser(onViewAll = onNavigateToVouchers)
    }

    // 6. Recent Recharges & Transactions
    item {
      Spacer(modifier = Modifier.height(16.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Recent Transactions",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "View All",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.clickable { onNavigateToHistory() }
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
    }

    if (userTransactions.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.ReceiptLong,
              contentDescription = null,
              modifier = Modifier.size(40.dp),
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "No recharges yet",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "Your mobile, DTH and bill payments will appear here with Operator TR IDs.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
          }
        }
      }
    } else {
      items(userTransactions.take(3)) { tx ->
        RecentTransactionItem(transaction = tx, onClick = onNavigateToHistory)
      }
    }
  }
}

@Composable
private fun TopBrandBar(
  userName: String,
  isAdmin: Boolean,
  pendingAdminCount: Int,
  onToggleAdmin: () -> Unit,
  onProfileClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              listOf(Color(0xFF0F52BA), Color(0xFF00C853))
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.FlashOn,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "pay",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "4u",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = BrandSecondary
          )
        }
        Text(
          text = "Hi, $userName",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      // Switch to Admin Button - strictly visible ONLY to ADMIN users
      if (isAdmin) {
        Surface(
          onClick = onToggleAdmin,
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
          border = BorderStrokeSafe(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
          modifier = Modifier.testTag("admin_portal_chip")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.AdminPanelSettings,
              contentDescription = "Admin Portal",
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Admin Panel",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            if (pendingAdminCount > 0) {
              Spacer(modifier = Modifier.width(4.dp))
              Box(
                modifier = Modifier
                  .size(18.dp)
                  .clip(CircleShape)
                  .background(StatusPending),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = pendingAdminCount.toString(),
                  color = Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
        Spacer(modifier = Modifier.width(8.dp))
      }

      IconButton(
        onClick = onProfileClick,
        modifier = Modifier
          .size(40.dp)
          .testTag("profile_icon_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = "Profile",
          tint = MaterialTheme.colorScheme.onSurface
        )
      }
    }
  }
}

@Composable
private fun BorderStrokeSafe(width: androidx.compose.ui.unit.Dp, color: Color) =
  androidx.compose.foundation.BorderStroke(width, color)

@Composable
private fun WalletOverviewCard(
  balance: Double,
  userName: String,
  userPhone: String,
  onAddFunds: () -> Unit,
  onHistory: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp)
      .testTag("wallet_overview_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(
            colors = listOf(
              Color(0xFF0F2042),
              Color(0xFF133E87),
              Color(0xFF0F52BA)
            )
          )
        )
        .padding(20.dp)
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Column {
            Text(
              text = "pay4u Wallet Balance",
              style = MaterialTheme.typography.labelMedium,
              color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "₹${"%.2f".format(balance)}",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
          }
          Surface(
            color = Color.White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "UPI Enabled",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Acc: $userPhone",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
          )

          Row {
            Button(
              onClick = onHistory,
              colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.2f),
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.height(36.dp)
            ) {
              Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Passbook", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun HeroPromoCard(onExploreVouchers: () -> Unit) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 10.dp)
      .clickable { onExploreVouchers() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(2.dp)
  ) {
    Column {
      Image(
        painter = painterResource(id = R.drawable.img_hero_banner),
        contentDescription = "Promotional Banner",
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(16f / 9f)
          .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        contentScale = ContentScale.Crop
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Fast Recharges + Brand Vouchers",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Instant 2% to 10% discount on top brand gift cards",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Icon(
          imageVector = Icons.Default.ArrowForward,
          contentDescription = "Explore",
          tint = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}

@Composable
private fun ServiceGrid(
  onMobile: () -> Unit,
  onDth: () -> Unit,
  onBills: () -> Unit,
  onCreditCard: () -> Unit,
  onVouchers: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      ServiceGridTile(
        title = "Mobile",
        subtitle = "Prepaid/Postpaid",
        icon = Icons.Default.PhoneAndroid,
        color = Color(0xFF0F52BA),
        tag = "mobile_recharge_tile",
        modifier = Modifier.weight(1f),
        onClick = onMobile
      )
      ServiceGridTile(
        title = "DTH",
        subtitle = "TV Recharge",
        icon = Icons.Default.Tv,
        color = Color(0xFFE60000),
        tag = "dth_recharge_tile",
        modifier = Modifier.weight(1f),
        onClick = onDth
      )
      ServiceGridTile(
        title = "Bill Pay",
        subtitle = "Electricity/Gas",
        icon = Icons.Default.ElectricalServices,
        color = Color(0xFF00A86B),
        tag = "bill_payment_tile",
        modifier = Modifier.weight(1f),
        onClick = onBills
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      ServiceGridTile(
        title = "Credit Card",
        subtitle = "BBPS Bill Pay",
        icon = Icons.Default.CreditCard,
        color = Color(0xFF6200EA),
        tag = "credit_card_bill_tile",
        modifier = Modifier.weight(1f),
        onClick = onCreditCard
      )
      ServiceGridTile(
        title = "Brand Vouchers",
        subtitle = "Gift Cards (2-10% Off)",
        icon = Icons.Default.CardGiftcard,
        color = Color(0xFFFF9900),
        tag = "brand_vouchers_tile",
        modifier = Modifier.weight(1f),
        onClick = onVouchers
      )
    }
  }
}

@Composable
private fun ServiceGridTile(
  title: String,
  subtitle: String,
  icon: ImageVector,
  color: Color,
  tag: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Card(
    onClick = onClick,
    modifier = modifier.testTag(tag),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 14.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(color.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = color,
          modifier = Modifier.size(24.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        maxLines = 1
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 9.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
      )
    }
  }
}

@Composable
private fun BrandVoucherTeaser(onViewAll: () -> Unit) {
  Column(modifier = Modifier.padding(horizontal = 16.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Brand Vouchers & Gift Cards",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Instant codes with extra savings for frequent users",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Text(
        text = "See All",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clickable { onViewAll() }
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    val sampleBrands = listOf(
      Triple("Amazon Pay", "2.5% OFF", Color(0xFFFF9900)),
      Triple("Flipkart", "2.0% OFF", Color(0xFF2874F0)),
      Triple("Swiggy", "5.0% OFF", Color(0xFFFC8019)),
      Triple("Zomato", "4.0% OFF", Color(0xFFE23744)),
      Triple("Myntra", "6.0% OFF", Color(0xFFFF3F6C))
    )

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(vertical = 4.dp)
    ) {
      items(sampleBrands) { (name, discount, brandColor) ->
        Card(
          onClick = onViewAll,
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(1.dp),
          modifier = Modifier.width(130.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = brandColor.copy(alpha = 0.15f)
            ) {
              Text(
                text = discount,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = brandColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = name,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              maxLines = 1
            )
            Text(
              text = "Instant Delivery",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp
            )
          }
        }
      }
    }
  }
}

@Composable
fun RecentTransactionItem(
  transaction: TransactionEntity,
  onClick: () -> Unit
) {
  Card(
    onClick = onClick,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(0.5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Box(
          modifier = Modifier
            .size(40.dp)
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
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "${transaction.providerName} - ${transaction.targetNumber}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
          if (transaction.status == "SUCCESS" && transaction.operatorTrId.isNotBlank()) {
            Text(
              text = "TR ID: ${transaction.operatorTrId}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = BrandSecondary,
              fontSize = 11.sp
            )
          } else {
            Text(
              text = if (transaction.status == "PENDING") "Awaiting Admin TR ID" else "Failed",
              style = MaterialTheme.typography.labelSmall,
              color = if (transaction.status == "PENDING") StatusPending else Color.Red,
              fontSize = 11.sp
            )
          }
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "₹${"%.0f".format(transaction.amount)}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = when (transaction.status) {
            "SUCCESS" -> StatusSuccess.copy(alpha = 0.15f)
            "PENDING" -> StatusPending.copy(alpha = 0.15f)
            else -> Color.Red.copy(alpha = 0.15f)
          }
        ) {
          Text(
            text = transaction.status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = when (transaction.status) {
              "SUCCESS" -> StatusSuccess
              "PENDING" -> StatusPending
              else -> Color.Red
            },
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp
          )
        }
      }
    }
  }
}

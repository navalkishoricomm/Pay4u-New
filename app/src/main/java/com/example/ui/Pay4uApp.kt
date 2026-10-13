package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BillPaymentScreen
import com.example.ui.screens.BrandVouchersScreen
import com.example.ui.screens.DthRechargeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MobileRechargeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.viewmodel.Pay4uViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.UiEvent

@Composable
fun Pay4uApp(viewModel: Pay4uViewModel = viewModel()) {
  val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
  val pendingCount = allTransactions.count { it.status == "PENDING" }

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(Unit) {
    viewModel.uiEvents.collect { event ->
      when (event) {
        is UiEvent.ShowSnackbar -> {
          snackbarHostState.showSnackbar(event.message)
        }
        else -> Unit
      }
    }
  }

  // If user is completely logged out and not on AuthScreen, redirect to AuthScreen
  if (currentUser == null) {
    AuthScreen(
      viewModel = viewModel,
      onLoginSuccess = {
        if (viewModel.currentUser.value?.role == "ADMIN") {
          viewModel.navigateTo(Screen.ADMIN_DASHBOARD)
        } else {
          viewModel.navigateTo(Screen.HOME)
        }
      }
    )
    return
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      // Show bottom navigation bar on primary screens
      if (currentScreen in listOf(Screen.HOME, Screen.BRAND_VOUCHERS, Screen.TRANSACTION_HISTORY, Screen.PROFILE)) {
        val isAdminUser = currentUser?.role == "ADMIN"
        NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
          NavigationBarItem(
            selected = currentScreen == Screen.HOME,
            onClick = { viewModel.navigateTo(Screen.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            modifier = Modifier.testTag("nav_home_tab")
          )
          NavigationBarItem(
            selected = currentScreen == Screen.BRAND_VOUCHERS,
            onClick = { viewModel.navigateTo(Screen.BRAND_VOUCHERS) },
            icon = { Icon(Icons.Default.CardGiftcard, contentDescription = "Vouchers") },
            label = { Text("Vouchers") },
            modifier = Modifier.testTag("nav_vouchers_tab")
          )
          NavigationBarItem(
            selected = currentScreen == Screen.TRANSACTION_HISTORY,
            onClick = { viewModel.navigateTo(Screen.TRANSACTION_HISTORY) },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Passbook") },
            label = { Text("Passbook") },
            modifier = Modifier.testTag("nav_passbook_tab")
          )
          // Admin option is strictly visible ONLY to ADMIN users
          if (isAdminUser) {
            NavigationBarItem(
              selected = currentScreen in listOf(Screen.ADMIN_DASHBOARD, Screen.ADMIN_TRANSACTIONS, Screen.ADMIN_USERS, Screen.ADMIN_COMMISSIONS),
              onClick = { viewModel.navigateTo(Screen.ADMIN_DASHBOARD) },
              icon = {
                if (pendingCount > 0) {
                  BadgedBox(badge = { Badge { Text(pendingCount.toString()) } }) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin")
                  }
                } else {
                  Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin")
                }
              },
              label = { Text("Admin") },
              modifier = Modifier.testTag("nav_admin_tab")
            )
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentScreen) {
        Screen.HOME -> HomeScreen(
          viewModel = viewModel,
          onNavigateToMobile = { viewModel.navigateTo(Screen.MOBILE_RECHARGE) },
          onNavigateToDth = { viewModel.navigateTo(Screen.DTH_RECHARGE) },
          onNavigateToBills = { viewModel.navigateTo(Screen.BILL_PAYMENT) },
          onNavigateToCreditCard = { viewModel.navigateTo(Screen.CREDIT_CARD_PAYMENT) },
          onNavigateToVouchers = { viewModel.navigateTo(Screen.BRAND_VOUCHERS) },
          onNavigateToHistory = { viewModel.navigateTo(Screen.TRANSACTION_HISTORY) },
          onNavigateToAdmin = {
            if (currentUser?.role == "ADMIN") {
              viewModel.navigateTo(Screen.ADMIN_DASHBOARD)
            }
          }
        )

        Screen.MOBILE_RECHARGE -> MobileRechargeScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateBack() }
        )

        Screen.DTH_RECHARGE -> DthRechargeScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateBack() }
        )

        Screen.BILL_PAYMENT -> BillPaymentScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateBack() }
        )

        Screen.CREDIT_CARD_PAYMENT -> com.example.ui.screens.CreditCardBillScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateBack() }
        )

        Screen.BRAND_VOUCHERS -> BrandVouchersScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateBack() }
        )

        Screen.TRANSACTION_HISTORY -> TransactionHistoryScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateBack() }
        )

        Screen.PROFILE -> ProfileScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateBack() }
        )

        Screen.ADMIN_DASHBOARD,
        Screen.ADMIN_TRANSACTIONS,
        Screen.ADMIN_USERS,
        Screen.ADMIN_COMMISSIONS -> {
          // Security check: non-admin users (such as Mukesh Garg) cannot see or open admin screens
          if (currentUser?.role != "ADMIN") {
            LaunchedEffect(Unit) {
              viewModel.navigateTo(Screen.HOME)
            }
          } else {
            AdminDashboardScreen(
              viewModel = viewModel,
              onExitAdmin = { viewModel.navigateTo(Screen.HOME) }
            )
          }
        }
      }
    }
  }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.UserRole
import com.example.ui.components.NotificationCenterSheet
import com.example.ui.components.RealTimeNotificationBanner
import com.example.ui.components.RoleSwitcherHeader
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.CustomerScreen
import com.example.ui.screens.TvQueueScreen
import com.example.ui.theme.AntriYukTheme
import com.example.ui.theme.WarmIvory
import com.example.viewmodel.QueueViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AntriYukTheme {
                AntriYukAppRoot()
            }
        }
    }
}

@Composable
fun AntriYukAppRoot(
    viewModel: QueueViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showNotificationsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.statusSnackbarMessage) {
        val msg = uiState.statusSnackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    LaunchedEffect(uiState.activeBannerNotification?.id) {
        if (uiState.activeBannerNotification != null) {
            delay(4500)
            viewModel.dismissBannerNotification()
        }
    }

    if (uiState.currentRole == UserRole.LAYAR_TV) {
        TvQueueScreen(
            uiState = uiState,
            viewModel = viewModel,
            onExitTvMode = { viewModel.switchRole(UserRole.PELANGGAN) },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    val unreadCount = uiState.notifications.count { !it.isRead }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = WarmIvory,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            RoleSwitcherHeader(
                currentRole = uiState.currentRole,
                unreadCount = unreadCount,
                onSelectRole = { role -> viewModel.switchRole(role) },
                onOpenNotifications = {
                    viewModel.markAllNotificationsRead()
                    showNotificationsSheet = true
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                RealTimeNotificationBanner(
                    notification = uiState.activeBannerNotification,
                    onDismiss = { viewModel.dismissBannerNotification() },
                    onOpenInbox = {
                        viewModel.dismissBannerNotification()
                        viewModel.markAllNotificationsRead()
                        showNotificationsSheet = true
                    }
                )

                when (uiState.currentRole) {
                    UserRole.PELANGGAN -> {
                        CustomerScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    UserRole.ADMIN_SALON -> {
                        AdminScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            isOwnerMode = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    UserRole.PEMILIK_SALON -> {
                        AdminScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            isOwnerMode = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    UserRole.LAYAR_TV -> Unit
                }
            }
        }
    }

    if (showNotificationsSheet) {
        NotificationCenterSheet(
            notifications = uiState.notifications,
            onDismiss = { showNotificationsSheet = false }
        )
    }
}

package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.model.InAppNotification
import com.example.model.NotificationChannelType
import com.example.model.PrinterWidth
import com.example.model.QrVerificationStatus
import com.example.model.QueueStatus
import com.example.model.QueueTicket
import com.example.model.UserRole
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AmberWarm
import com.example.ui.theme.AppBrandTitleStyle
import com.example.ui.theme.BlushMist
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRose
import com.example.ui.theme.CrimsonSoft
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.EmeraldSage
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.PurePorcelain
import com.example.ui.theme.RoseGoldTerracotta
import com.example.ui.theme.SilkHairline
import com.example.ui.theme.WarmAlabaster
import com.example.ui.theme.WhatsAppGreen
import com.example.util.QrAndPrinterHelper
import kotlinx.coroutines.launch

@Composable
fun AntriYukBrandLogo(
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.img_app_logo),
        contentDescription = "Logo Antri Yuk",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.24f))
    )
}

@Composable
fun RoleSwitcherHeader(
    currentRole: UserRole,
    unreadCount: Int,
    onSelectRole: (UserRole) -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AntriYukBrandLogo(size = 38.dp)
                    Column {
                        Text(
                            text = "Antri Yuk",
                            style = AppBrandTitleStyle,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Salon Kecantikan & Reservasi Antrean",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier.testTag("notifications_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(containerColor = RoseGoldTerracotta) {
                                    Text(unreadCount.coerceAtMost(9).toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (unreadCount > 0) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                            contentDescription = "Pusat Notifikasi",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserRole.entries.forEach { role ->
                    val selected = role == currentRole
                    val icon = when (role) {
                        UserRole.PELANGGAN -> Icons.Outlined.Person
                        UserRole.ADMIN_SALON -> Icons.Outlined.AdminPanelSettings
                        UserRole.PEMILIK_SALON -> Icons.Outlined.Storefront
                        UserRole.LAYAR_TV -> Icons.Outlined.Tv
                    }
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectRole(role) },
                        modifier = Modifier.testTag("role_chip_${role.name.lowercase()}"),
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = role.shortLabel,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoseGoldTerracotta,
                            selectedLabelColor = PurePorcelain,
                            selectedLeadingIconColor = PurePorcelain,
                            containerColor = WarmAlabaster,
                            labelColor = DeepCharcoal
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = SilkHairline
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun QueueStatusBadge(
    status: QueueStatus,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (status) {
        QueueStatus.MENUNGGU -> WarmAlabaster to DeepCharcoal
        QueueStatus.HAMPIR_DIPANGGIL -> AmberSoft to AmberWarm
        QueueStatus.DIPANGGIL -> BlushMist to RoseGoldTerracotta
        QueueStatus.SEDANG_DILAYANI -> EmeraldSoft to EmeraldSage
        QueueStatus.SELESAI -> EmeraldSoft to EmeraldSage
        QueueStatus.DILEWATI -> AmberSoft to AmberWarm
        QueueStatus.DIBATALKAN -> CrimsonSoft to CrimsonRose
    }
    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun QrVerificationBadge(
    status: QrVerificationStatus,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (status) {
        QrVerificationStatus.BELUM_DIPINDAI -> BlushMist to RoseGoldTerracotta
        QrVerificationStatus.SUDAH_DIVERIFIKASI -> EmeraldSoft to EmeraldSage
        QrVerificationStatus.SELESAI -> EmeraldSoft to EmeraldSage
        QrVerificationStatus.DIBATALKAN -> CrimsonSoft to CrimsonRose
        QrVerificationStatus.TIDAK_VALID -> CrimsonSoft to CrimsonRose
    }
    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(999.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "QR: ${status.label}",
                style = MaterialTheme.typography.labelMedium,
                color = fg,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun RealTimeNotificationBanner(
    notification: InAppNotification?,
    onDismiss: () -> Unit,
    onOpenInbox: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = notification != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        notification?.let { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onOpenInbox() },
                colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (item.channelType == NotificationChannelType.WHATSAPP_SIM) {
                                    WhatsAppGreen
                                } else {
                                    RoseGoldTerracotta
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.channelType == NotificationChannelType.WHATSAPP_SIM) {
                                Icons.Outlined.Chat
                            } else {
                                Icons.Filled.Notifications
                            },
                            contentDescription = null,
                            tint = PurePorcelain,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = PurePorcelain,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PurePorcelain.copy(alpha = 0.85f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Tutup notifikasi",
                            tint = PurePorcelain
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterSheet(
    notifications: List<InAppNotification>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Notifikasi Real-Time & WhatsApp",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Riwayat pemberitahuan ambil antrean, hampir dipanggil, dan panggilan kursi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada notifikasi saat ini.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PurePorcelain),
                            border = BorderStroke(1.dp, SilkHairline),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (notif.channelType == NotificationChannelType.WHATSAPP_SIM) {
                                            EmeraldSoft
                                        } else {
                                            BlushMist
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = notif.channelType.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (notif.channelType == NotificationChannelType.WHATSAPP_SIM) {
                                                WhatsAppGreen
                                            } else {
                                                RoseGoldTerracotta
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text(
                                        text = QrAndPrinterHelper.formatJam(notif.timestampMillis),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepCharcoal
                                )
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!notif.whatsappPreviewText.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = EmeraldSoft.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "Pesan WhatsApp Terkirim (Simulasi):",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = WhatsAppGreen
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = notif.whatsappPreviewText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = DeepCharcoal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketPrintAndQrSheet(
    ticket: QueueTicket,
    onDismiss: () -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedWidth by remember { mutableStateOf(PrinterWidth.THERMAL_58MM) }
    var showRawEscPos by remember { mutableStateOf(false) }

    val ticketBitmap = remember(ticket.id, ticket.qrToken, selectedWidth) {
        QrAndPrinterHelper.renderTicketBitmap(ticket, selectedWidth, context)
    }
    val escPosBytes = remember(ticket.id, ticket.qrToken, selectedWidth) {
        QrAndPrinterHelper.buildEscPosReceiptBytes(ticket, selectedWidth)
    }
    val textPreview = remember(ticket.id, ticket.qrToken, selectedWidth) {
        QrAndPrinterHelper.formatThermalTextPreview(ticket, selectedWidth)
    }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                val msg = QrAndPrinterHelper.sendToPairedBluetoothPrinter(context, ticket, selectedWidth)
                    .getOrElse { it.localizedMessage ?: "Gagal terhubung ke printer Bluetooth." }
                onMessage(msg)
            }
        } else {
            onMessage("Izin Bluetooth ditolak. Gunakan Cetak PDF atau Bagikan Tiket.")
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cetak & Bagikan Tiket",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DeepCharcoal
                    )
                    Text(
                        text = "Format Printer Thermal 58 mm / 80 mm & Dokumen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Filled.QrCode2,
                    contentDescription = null,
                    tint = RoseGoldTerracotta,
                    modifier = Modifier.size(32.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PrinterWidth.entries.forEach { width ->
                    val selected = selectedWidth == width
                    FilterChip(
                        selected = selected,
                        onClick = { selectedWidth = width },
                        modifier = Modifier.testTag("printer_width_${width.paperMm}"),
                        label = { Text(width.label) },
                        leadingIcon = if (selected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoseGoldTerracotta,
                            selectedLabelColor = PurePorcelain,
                            selectedLeadingIconColor = PurePorcelain
                        )
                    )
                }
            }

            // Pratinjau Gambar Struk Resmi (Logo Antri Yuk + QR + Info Lengkap)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(WarmAlabaster)
                    .border(1.dp, SilkHairline, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = ticketBitmap.asImageBitmap(),
                    contentDescription = "Pratinjau tiket cetak ${ticket.queueNumber}",
                    modifier = Modifier
                        .width(if (selectedWidth == PrinterWidth.THERMAL_58MM) 250.dp else 320.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            TextButton(
                onClick = { showRawEscPos = !showRawEscPos },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    if (showRawEscPos) {
                        "Sembunyikan Teks ESC/POS (${escPosBytes.size} byte)"
                    } else {
                        "Lihat Format Teks & Byte ESC/POS (${escPosBytes.size} byte)"
                    }
                )
            }

            if (showRawEscPos) {
                Surface(
                    color = DeepCharcoal,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = textPreview,
                        color = PurePorcelain,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            HorizontalDivider(color = SilkHairline)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        QrAndPrinterHelper.printTicketDocument(context, ticket, selectedWidth)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("print_pdf_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseGoldTerracotta)
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cetak / PDF")
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            QrAndPrinterHelper.shareTicketImage(context, ticket, selectedWidth)
                                .onFailure {
                                    onMessage("Gagal membagikan tiket: ${it.localizedMessage}")
                                }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("share_ticket_button"),
                    border = BorderStroke(1.dp, RoseGoldTerracotta)
                ) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = null,
                        tint = RoseGoldTerracotta,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan", color = RoseGoldTerracotta)
                }
            }

            OutlinedButton(
                onClick = {
                    val needsRuntimeBt = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    val granted = !needsRuntimeBt || ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED

                    if (granted) {
                        scope.launch {
                            val msg = QrAndPrinterHelper.sendToPairedBluetoothPrinter(
                                context,
                                ticket,
                                selectedWidth
                            ).getOrElse { it.localizedMessage ?: "Gagal terhubung ke printer Bluetooth." }
                            onMessage(msg)
                        }
                    } else {
                        bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("bluetooth_print_button")
            ) {
                Icon(Icons.Filled.Bluetooth, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kirim ESC/POS ke Printer Bluetooth (${selectedWidth.paperMm} mm)")
            }
        }
    }
}

package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.model.Hairstylist
import com.example.model.QueueStatus
import com.example.model.QueueTicket
import com.example.model.Salon
import com.example.model.SalonService
import com.example.ui.components.AntriYukBrandLogo
import com.example.ui.components.QrVerificationBadge
import com.example.ui.components.QueueStatusBadge
import com.example.ui.components.TicketPrintAndQrSheet
import com.example.ui.theme.BlushMist
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ChampagneSoft
import com.example.ui.theme.CrimsonRose
import com.example.ui.theme.CrimsonSoft
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.DeepTerracotta
import com.example.ui.theme.EmeraldSage
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.PurePorcelain
import com.example.ui.theme.QueueNumberCardStyle
import com.example.ui.theme.QueueNumberDisplayStyle
import com.example.ui.theme.RoseGoldTerracotta
import com.example.ui.theme.SilkHairline
import com.example.ui.theme.WarmAlabaster
import com.example.ui.theme.WarmIvory
import com.example.ui.theme.WhatsAppGreen
import com.example.util.QrAndPrinterHelper
import com.example.viewmodel.AntriYukUiState
import com.example.viewmodel.QueueViewModel

@Composable
fun CustomerScreen(
    uiState: AntriYukUiState,
    viewModel: QueueViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var ticketToPrint by remember { mutableStateOf<QueueTicket?>(null) }
    var zoomedQrTicket by remember { mutableStateOf<QueueTicket?>(null) }
    var ticketToCancel by remember { mutableStateOf<QueueTicket?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Ditangani secara gracefully oleh NotificationHelper */ }

    BackHandler(enabled = selectedTab == 1) {
        selectedTab = 0
    }

    val selectedSalon = uiState.selectedSalon
    val activeTicket = uiState.activeCustomerTicket
    val activeServices = uiState.salonServices.filter { it.isActive }
    val categories = remember(activeServices) {
        listOf("Semua") + activeServices.map { it.category }.distinct()
    }
    val filteredServices = remember(activeServices, selectedCategory) {
        if (selectedCategory == "Semua") activeServices
        else activeServices.filter { it.category == selectedCategory }
    }
    val chosenServices = activeServices.filter { it.id in uiState.selectedServiceIds }
    val totalPrice = chosenServices.sumOf { it.priceRupiah }
    val totalDuration = chosenServices.sumOf { it.durationMinutes }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = WarmIvory,
            contentColor = RoseGoldTerracotta,
            modifier = Modifier.widthIn(max = 720.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.testTag("customer_tab_booking"),
                icon = { Icon(Icons.Filled.Spa, contentDescription = null) },
                text = { Text("Ambil Antrean") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.testTag("customer_tab_ticket"),
                icon = { Icon(Icons.Filled.ConfirmationNumber, contentDescription = null) },
                text = {
                    val label = if (activeTicket != null) {
                        "Tiket Saya (${activeTicket.queueNumber})"
                    } else {
                        "Tiket & Pantau"
                    }
                    Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            )
        }

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Hero Banner Salon Pilihan
                if (selectedSalon != null) {
                    item {
                        SalonHeroBanner(
                            salon = selectedSalon,
                            currentlyCalled = uiState.currentlyCalledTicket?.queueNumber ?: "-",
                            waitingCount = uiState.waitingTicketsForSelectedSalon.size
                        )
                    }
                }

                // 2. Pilih Salon (Status Buka / Tutup)
                item {
                    SectionTitle(
                        title = "1. Pilih Salon Kecantikan",
                        subtitle = "Status operasional & kebijakan antrean real-time"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.salons, key = { it.id }) { salon ->
                            SalonPickerCard(
                                salon = salon,
                                isSelected = salon.id == selectedSalon?.id,
                                onClick = {
                                    selectedCategory = "Semua"
                                    viewModel.selectSalon(salon.id)
                                }
                            )
                        }
                    }
                }

                // 3. Pilih Layanan (Multi-select)
                item {
                    SectionTitle(
                        title = "2. Pilih Layanan (Bisa Lebih dari Satu)",
                        subtitle = "Harga transparan & estimasi durasi per perawatan"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            val selected = cat == selectedCategory
                            FilterChip(
                                selected = selected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeepCharcoal,
                                    selectedLabelColor = PurePorcelain,
                                    containerColor = WarmAlabaster
                                )
                            )
                        }
                    }
                }

                items(filteredServices, key = { it.id }) { service ->
                    val isChecked = service.id in uiState.selectedServiceIds
                    ServiceSelectionCard(
                        service = service,
                        isSelected = isChecked,
                        onToggle = { viewModel.toggleServiceSelection(service.id) }
                    )
                }

                // 4. Pilih Hairstylist
                item {
                    SectionTitle(
                        title = "3. Pilih Hairstylist",
                        subtitle = "Pilih kapster favorit atau layanan tercepat"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FastestStylistCard(
                        isSelected = uiState.selectedStylistId == null,
                        onClick = { viewModel.selectStylist(null) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.salonStylists.forEach { stylist ->
                            StylistSelectionCard(
                                stylist = stylist,
                                isSelected = uiState.selectedStylistId == stylist.id,
                                onClick = {
                                    if (stylist.isAvailable) {
                                        viewModel.selectStylist(stylist.id)
                                    } else {
                                        viewModel.postSnackbar("${stylist.name} sedang tidak bertugas.")
                                    }
                                }
                            )
                        }
                    }
                }

                // 5. Data Pelanggan & Konfirmasi Ambil Nomor
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PurePorcelain),
                        border = BorderStroke(1.dp, SilkHairline),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "4. Data Pelanggan & Ambil Nomor",
                                style = MaterialTheme.typography.titleLarge,
                                color = DeepCharcoal
                            )
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("Nama Anda") },
                                placeholder = { Text("Contoh: Anisa Rahmawati") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("customer_name_input")
                            )
                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                label = { Text("Nomor WhatsApp (untuk notifikasi antrean)") },
                                placeholder = { Text("0812-xxxx-xxxx") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("customer_phone_input")
                            )

                            Surface(
                                color = BlushMist.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${chosenServices.size} Layanan • $totalDuration Menit",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = DeepTerracotta
                                        )
                                        Text(
                                            text = selectedSalon?.cancellationPolicy ?: "",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }
                                    Text(
                                        text = QrAndPrinterHelper.formatRupiah(totalPrice),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepCharcoal
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    val finalName = customerName.trim().ifBlank { "Tamu Salon" }
                                    viewModel.takeQueueTicket(
                                        customerName = finalName,
                                        customerPhone = customerPhone.trim()
                                    )
                                    customerName = ""
                                    customerPhone = ""
                                    selectedTab = 1
                                },
                                enabled = selectedSalon?.isOpen == true && chosenServices.isNotEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("take_queue_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = RoseGoldTerracotta)
                            ) {
                                Icon(Icons.Filled.ConfirmationNumber, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedSalon?.isOpen == true) {
                                        "Ambil Nomor Antrean Sekarang"
                                    } else {
                                        "Salon Sedang Tutup"
                                    },
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 2: TIKET & PANTAU ANTREAN REAL-TIME
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    LiveSalonMonitorStrip(
                        salonName = selectedSalon?.name ?: "-",
                        currentlyCalled = uiState.currentlyCalledTicket,
                        waitingCount = uiState.waitingTicketsForSelectedSalon.size
                    )
                }

                if (activeTicket != null) {
                    item {
                        ActiveTicketBoardingCard(
                            ticket = activeTicket,
                            positionAhead = uiState.getPositionAhead(activeTicket),
                            cancellationPolicy = selectedSalon?.cancellationPolicy
                                ?: "Pembatalan diperbolehkan sebelum nomor dipanggil.",
                            onZoomQr = { zoomedQrTicket = activeTicket },
                            onPrintTicket = { ticketToPrint = activeTicket },
                            onCancelTicket = { ticketToCancel = activeTicket }
                        )
                    }
                } else {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PurePorcelain),
                            border = BorderStroke(1.dp, SilkHairline),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = RoseGoldTerracotta,
                                    modifier = Modifier.size(44.dp)
                                )
                                Text(
                                    text = "Belum Ada Tiket Aktif",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = DeepCharcoal
                                )
                                Text(
                                    text = "Ambil nomor antrean terlebih dahulu untuk mendapatkan QR Code dan memantau estimasi giliran Anda.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { selectedTab = 0 },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoseGoldTerracotta)
                                ) {
                                    Text("Ambil Antrean")
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Antrean Salon Saat Ini (${selectedSalon?.name ?: ""})",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepCharcoal
                    )
                }

                items(uiState.salonTickets.reversed(), key = { it.id }) { ticket ->
                    val isCurrent = ticket.id == activeTicket?.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectCustomerTicket(ticket.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) BlushMist.copy(alpha = 0.45f) else PurePorcelain
                        ),
                        border = BorderStroke(
                            width = if (isCurrent) 1.5.dp else 1.dp,
                            color = if (isCurrent) RoseGoldTerracotta else SilkHairline
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = ticket.queueNumber,
                                        style = QueueNumberCardStyle.copy(fontSize = 18.sp),
                                        color = DeepCharcoal
                                    )
                                    Text(
                                        text = ticket.customerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeepCharcoal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "${ticket.serviceNames.joinToString(", ")} • ${ticket.stylistName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            QueueStatusBadge(status = ticket.status)
                        }
                    }
                }
            }
        }
    }

    ticketToPrint?.let { ticket ->
        TicketPrintAndQrSheet(
            ticket = ticket,
            onDismiss = { ticketToPrint = null },
            onMessage = { msg -> viewModel.postSnackbar(msg) }
        )
    }

    zoomedQrTicket?.let { ticket ->
        val qrBitmap = remember(ticket.qrToken) {
            QrAndPrinterHelper.generateQrBitmap(ticket.qrToken, sizePx = 640)
        }
        Dialog(onDismissRequest = { zoomedQrTicket = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PurePorcelain),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tunjukkan ke Admin Salon",
                        style = MaterialTheme.typography.titleLarge,
                        color = DeepCharcoal
                    )
                    Text(
                        text = ticket.queueNumber,
                        style = QueueNumberDisplayStyle,
                        color = RoseGoldTerracotta
                    )
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code Tiket ${ticket.queueNumber}",
                        modifier = Modifier
                            .size(230.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, SilkHairline, RoundedCornerShape(16.dp))
                    )
                    QrVerificationBadge(status = ticket.qrVerificationStatus)
                    Text(
                        text = "Kode Aman: ${ticket.qrNonce}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { zoomedQrTicket = null },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseGoldTerracotta),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tutup")
                    }
                }
            }
        }
    }

    ticketToCancel?.let { ticket ->
        val policy = uiState.salons.find { it.id == ticket.salonId }?.cancellationPolicy
            ?: "Pembatalan hanya berlaku sebelum nomor dipanggil."
        AlertDialog(
            onDismissRequest = { ticketToCancel = null },
            title = { Text("Batalkan Antrean ${ticket.queueNumber}?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ketentuan Salon: $policy")
                    Text("QR Code tiket akan langsung dinonaktifkan dan tidak dapat digunakan kembali.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelCustomerTicket(ticket.id)
                        ticketToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRose)
                ) {
                    Text("Ya, Batalkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { ticketToCancel = null }) {
                    Text("Kembali")
                }
            }
        )
    }
}

@Composable
private fun SalonHeroBanner(
    salon: Salon,
    currentlyCalled: String,
    waitingCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Image(
            painter = painterResource(id = salon.imageRes),
            contentDescription = salon.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            DeepCharcoal.copy(alpha = 0.86f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (salon.isOpen) EmeraldSage else CrimsonRose,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = if (salon.isOpen) "BUKA SEKARANG" else "SEDANG TUTUP",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PurePorcelain,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Surface(
                    color = PurePorcelain.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = ChampagneGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${salon.rating}",
                            style = MaterialTheme.typography.labelLarge,
                            color = DeepCharcoal
                        )
                    }
                }
            }

            Column {
                Text(
                    text = salon.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = PurePorcelain
                )
                Text(
                    text = salon.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PurePorcelain.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Dipanggil: $currentlyCalled",
                        style = MaterialTheme.typography.labelLarge,
                        color = BlushMist
                    )
                    Text(
                        text = "•  Menunggu: $waitingCount orang",
                        style = MaterialTheme.typography.labelLarge,
                        color = PurePorcelain
                    )
                }
            }
        }
    }
}

@Composable
private fun SalonPickerCard(
    salon: Salon,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(248.dp)
            .clickable(onClick = onClick)
            .testTag("salon_card_${salon.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BlushMist.copy(alpha = 0.55f) else PurePorcelain
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) RoseGoldTerracotta else SilkHairline
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (salon.isOpen) EmeraldSoft else CrimsonSoft,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (salon.isOpen) "Buka" else "Tutup",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (salon.isOpen) EmeraldSage else CrimsonRose,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Terpilih",
                        tint = RoseGoldTerracotta,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = salon.name,
                style = MaterialTheme.typography.titleMedium,
                color = DeepCharcoal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = RoseGoldTerracotta,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = salon.address,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = salon.openHoursLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ServiceSelectionCard(
    service: SalonService,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .testTag("service_card_${service.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BlushMist.copy(alpha = 0.42f) else PurePorcelain
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) RoseGoldTerracotta else SilkHairline
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) RoseGoldTerracotta else WarmAlabaster),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCut,
                    contentDescription = null,
                    tint = if (isSelected) PurePorcelain else RoseGoldTerracotta,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = DeepCharcoal
                )
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = QrAndPrinterHelper.formatRupiah(service.priceRupiah),
                        style = MaterialTheme.typography.labelLarge,
                        color = RoseGoldTerracotta,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• ${service.durationMinutes} menit",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = if (isSelected) "Dipilih" else "Belum dipilih",
                tint = if (isSelected) RoseGoldTerracotta else SilkHairline,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun FastestStylistCard(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("stylist_fastest_card"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ChampagneSoft.copy(alpha = 0.7f) else PurePorcelain
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) ChampagneGold else SilkHairline
        ),
        shape = RoundedCornerShape(16.dp)
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
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(ChampagneGold),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = PurePorcelain)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kapster Tersedia / Tercepat",
                    style = MaterialTheme.typography.titleMedium,
                    color = DeepCharcoal
                )
                Text(
                    text = "Otomatis dipilihkan hairstylist yang paling cepat siap melayani.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) ChampagneGold else SilkHairline
            )
        }
    }
}

@Composable
private fun StylistSelectionCard(
    stylist: Hairstylist,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BlushMist.copy(alpha = 0.45f) else PurePorcelain
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) RoseGoldTerracotta else SilkHairline
        ),
        shape = RoundedCornerShape(16.dp)
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
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (stylist.isAvailable) WarmAlabaster else CrimsonSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = if (stylist.isAvailable) RoseGoldTerracotta else CrimsonRose
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stylist.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepCharcoal
                    )
                    Surface(
                        color = if (stylist.isAvailable) EmeraldSoft else CrimsonSoft,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (stylist.isAvailable) stylist.chairNumber else "Libur",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (stylist.isAvailable) EmeraldSage else CrimsonRose,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "${stylist.specialty} • ${stylist.experienceYears} thn pengalaman",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) RoseGoldTerracotta else SilkHairline
            )
        }
    }
}

@Composable
private fun LiveSalonMonitorStrip(
    salonName: String,
    currentlyCalled: QueueTicket?,
    waitingCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PANTAUAN LIVE • $salonName",
                    style = MaterialTheme.typography.labelMedium,
                    color = BlushMist
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentlyCalled?.let { "Dipanggil: ${it.queueNumber}" }
                        ?: "Belum ada nomor dipanggil",
                    style = QueueNumberCardStyle,
                    color = PurePorcelain
                )
                Text(
                    text = currentlyCalled?.let { "${it.stylistName} • ${it.customerName}" }
                        ?: "Menunggu pembukaan panggilan berikutnya",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PurePorcelain.copy(alpha = 0.8f)
                )
            }
            Surface(
                color = RoseGoldTerracotta,
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$waitingCount",
                        style = QueueNumberCardStyle,
                        color = PurePorcelain
                    )
                    Text(
                        text = "Menunggu",
                        style = MaterialTheme.typography.labelMedium,
                        color = PurePorcelain
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveTicketBoardingCard(
    ticket: QueueTicket,
    positionAhead: Int,
    cancellationPolicy: String,
    onZoomQr: () -> Unit,
    onPrintTicket: () -> Unit,
    onCancelTicket: () -> Unit
) {
    val qrBitmap = remember(ticket.qrToken) {
        QrAndPrinterHelper.generateQrBitmap(ticket.qrToken, sizePx = 420)
    }
    val canCancel = ticket.status == QueueStatus.MENUNGGU ||
        ticket.status == QueueStatus.HAMPIR_DIPANGGIL

    Card(
        colors = CardDefaults.cardColors(containerColor = PurePorcelain),
        border = BorderStroke(1.5.dp, RoseGoldTerracotta.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_ticket_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AntriYukBrandLogo(size = 30.dp)
                    Column {
                        Text(
                            text = ticket.salonName,
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepCharcoal
                        )
                        Text(
                            text = "Diambil ${QrAndPrinterHelper.formatTanggalJam(ticket.createdAtMillis)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                QueueStatusBadge(status = ticket.status)
            }

            HorizontalDivider(color = SilkHairline)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NOMOR ANTREAN ANDA",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ticket.queueNumber,
                        style = QueueNumberDisplayStyle,
                        color = DeepCharcoal
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = ticket.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepCharcoal
                    )
                    Text(
                        text = "Hairstylist: ${ticket.stylistName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RoseGoldTerracotta,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onZoomQr)
                        .border(1.dp, SilkHairline, RoundedCornerShape(16.dp))
                        .background(WarmIvory)
                        .padding(8.dp)
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code Tiket ${ticket.queueNumber}",
                        modifier = Modifier.size(104.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCode2,
                            contentDescription = null,
                            tint = RoseGoldTerracotta,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Perbesar QR",
                            style = MaterialTheme.typography.labelMedium,
                            color = RoseGoldTerracotta
                        )
                    }
                }
            }

            QrVerificationBadge(status = ticket.qrVerificationStatus)

            // Estimasi & Posisi Antrean Real-time
            Surface(
                color = WarmAlabaster,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Posisi Antrean",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = when (ticket.status) {
                                QueueStatus.DIPANGGIL -> "Giliran Anda Sekarang!"
                                QueueStatus.SEDANG_DILAYANI -> "Sedang Dilayani"
                                QueueStatus.SELESAI -> "Pelayanan Selesai"
                                QueueStatus.DIBATALKAN -> "Dibatalkan"
                                QueueStatus.DILEWATI -> "Dilewati"
                                else -> "$positionAhead antrean di depan Anda"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepCharcoal
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Estimasi Pelayanan",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = QrAndPrinterHelper.formatJam(ticket.estimatedServiceMillis),
                            style = MaterialTheme.typography.titleMedium,
                            color = RoseGoldTerracotta,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Detail Layanan & Biaya
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Layanan Dipilih (${ticket.totalDurationMinutes} menit):",
                    style = MaterialTheme.typography.labelLarge,
                    color = DeepCharcoal
                )
                Text(
                    text = ticket.serviceNames.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total Estimasi Biaya: ${QrAndPrinterHelper.formatRupiah(ticket.totalPriceRupiah)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = DeepCharcoal
                )
            }

            // Status Notifikasi WhatsApp & In-App
            Surface(
                color = EmeraldSoft.copy(alpha = 0.65f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Chat,
                        contentDescription = null,
                        tint = WhatsAppGreen
                    )
                    Text(
                        text = "Notifikasi WhatsApp & Aplikasi aktif untuk nomor ${ticket.customerPhone}. Mohon menunggu hingga nomor antrean Anda dipanggil.",
                        style = MaterialTheme.typography.labelMedium,
                        color = DeepCharcoal
                    )
                }
            }

            AnimatedVisibility(visible = canCancel) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = cancellationPolicy,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPrintTicket,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_print_sheet_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseGoldTerracotta)
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cetak / Unduh")
                }

                if (canCancel) {
                    OutlinedButton(
                        onClick = onCancelTicket,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("cancel_ticket_button"),
                        border = BorderStroke(1.dp, CrimsonRose)
                    ) {
                        Icon(
                            Icons.Filled.Cancel,
                            contentDescription = null,
                            tint = CrimsonRose,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Batalkan", color = CrimsonRose)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = DeepCharcoal
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

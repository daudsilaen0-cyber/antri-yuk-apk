package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Hairstylist
import com.example.model.MonthlyReportEntry
import com.example.model.QueueStatus
import com.example.model.QueueTicket
import com.example.model.SalonService
import com.example.model.UserRole
import com.example.ui.components.QrScannerSheet
import com.example.ui.components.QrVerificationBadge
import com.example.ui.components.QueueStatusBadge
import com.example.ui.components.TicketPrintAndQrSheet
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AmberWarm
import com.example.ui.theme.BlushMist
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonRose
import com.example.ui.theme.CrimsonSoft
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.EmeraldSage
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.PurePorcelain
import com.example.ui.theme.QueueNumberCardStyle
import com.example.ui.theme.RoseGoldTerracotta
import com.example.ui.theme.SilkHairline
import com.example.ui.theme.WarmAlabaster
import com.example.ui.theme.WarmIvory
import com.example.util.QrAndPrinterHelper
import com.example.viewmodel.AntriYukUiState
import com.example.viewmodel.QueueViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminScreen(
    uiState: AntriYukUiState,
    viewModel: QueueViewModel,
    isOwnerMode: Boolean,
    modifier: Modifier = Modifier
) {
    if (!uiState.isAdminAuthenticated) {
        StaffLoginGate(
            isOwnerMode = isOwnerMode,
            authError = uiState.authErrorMessage,
            onLogin = { email, pin ->
                viewModel.loginStaff(
                    email = email,
                    pin = pin,
                    targetRole = if (isOwnerMode) UserRole.PEMILIK_SALON else UserRole.ADMIN_SALON
                )
            },
            onQuickDemoLogin = { asOwner -> viewModel.quickDemoLogin(asOwner) },
            modifier = modifier
        )
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var queueFilter by remember { mutableStateOf("Aktif") }
    var showQrScanner by remember { mutableStateOf(false) }
    var showWalkInDialog by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<SalonService?>(null) }
    var isCreatingService by remember { mutableStateOf(false) }
    var editingStylist by remember { mutableStateOf<Hairstylist?>(null) }
    var isCreatingStylist by remember { mutableStateOf(false) }
    var ticketForStatusChange by remember { mutableStateOf<QueueTicket?>(null) }
    var ticketToPrint by remember { mutableStateOf<QueueTicket?>(null) }

    val selectedSalon = uiState.selectedSalon
    val dailyReport = uiState.buildDailyReport()
    val hasOwnerAccess = isOwnerMode || uiState.loggedInStaffRole == UserRole.PEMILIK_SALON

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Staff & Salon Bar
        Surface(
            color = PurePorcelain,
            border = BorderStroke(1.dp, SilkHairline),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasOwnerAccess) "Mode Pemilik Salon (Akses Penuh)" else "Mode Admin Salon",
                            style = MaterialTheme.typography.labelMedium,
                            color = RoseGoldTerracotta,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.loggedInStaffName.ifBlank { "Staf Salon" },
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepCharcoal
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (selectedSalon != null) {
                            FilterChip(
                                selected = selectedSalon.isOpen,
                                onClick = { viewModel.toggleSalonOpen(selectedSalon.id) },
                                label = {
                                    Text(if (selectedSalon.isOpen) "Salon Buka" else "Salon Tutup")
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldSoft,
                                    selectedLabelColor = EmeraldSage,
                                    containerColor = CrimsonSoft,
                                    labelColor = CrimsonRose
                                )
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.logoutStaff() },
                            modifier = Modifier.testTag("staff_logout_button"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.Logout, contentDescription = "Keluar", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Keluar")
                        }
                    }
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.salons.forEach { salon ->
                        val active = salon.id == selectedSalon?.id
                        FilterChip(
                            selected = active,
                            onClick = { viewModel.selectSalon(salon.id) },
                            label = { Text(salon.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepCharcoal,
                                selectedLabelColor = PurePorcelain
                            )
                        )
                    }
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = WarmIvory,
            contentColor = RoseGoldTerracotta,
            modifier = Modifier.widthIn(max = 840.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Outlined.ListAlt, contentDescription = null) },
                text = { Text("Antrean") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Outlined.ContentCut, contentDescription = null) },
                text = { Text("Layanan") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
                text = { Text("Operasional") }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                icon = { Icon(Icons.Outlined.Assessment, contentDescription = null) },
                text = { Text("Laporan") }
            )
        }

        when (selectedTab) {
            0 -> {
                val filteredTickets = remember(uiState.salonTickets, queueFilter) {
                    when (queueFilter) {
                        "Aktif" -> uiState.salonTickets.filter {
                            it.status != QueueStatus.SELESAI && it.status != QueueStatus.DIBATALKAN
                        }
                        "Selesai" -> uiState.salonTickets.filter { it.status == QueueStatus.SELESAI }
                        else -> uiState.salonTickets
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 840.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Dashboard KPI Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KpiMiniCard(
                                label = "Menunggu",
                                value = "${dailyReport.sedangMenunggu}",
                                container = BlushMist,
                                accent = RoseGoldTerracotta,
                                modifier = Modifier.weight(1f)
                            )
                            KpiMiniCard(
                                label = "Selesai",
                                value = "${dailyReport.selesaiDilayani}",
                                container = EmeraldSoft,
                                accent = EmeraldSage,
                                modifier = Modifier.weight(1f)
                            )
                            KpiMiniCard(
                                label = "Omzet Hari Ini",
                                value = QrAndPrinterHelper.formatRupiah(dailyReport.estimasiPendapatanHariIni),
                                container = PurePorcelain,
                                accent = DeepCharcoal,
                                modifier = Modifier.weight(1.4f)
                            )
                        }
                    }

                    // Action Buttons: Call Next, Scan QR, Add Walk-in
                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.callNextQueue() },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("admin_call_next_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = RoseGoldTerracotta)
                            ) {
                                Icon(Icons.Filled.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Panggil Berikutnya")
                            }

                            OutlinedButton(
                                onClick = { showQrScanner = true },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("admin_scan_qr_button"),
                                border = BorderStroke(1.dp, DeepCharcoal)
                            ) {
                                Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = DeepCharcoal)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan QR Tiket", color = DeepCharcoal)
                            }

                            OutlinedButton(
                                onClick = { showWalkInDialog = true },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("admin_walkin_button")
                            ) {
                                Icon(Icons.Filled.PersonAdd, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tambah Walk-In")
                            }
                        }
                    }

                    // Filter Chips
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Aktif", "Selesai", "Semua").forEach { tab ->
                                FilterChip(
                                    selected = queueFilter == tab,
                                    onClick = { queueFilter = tab },
                                    label = { Text(tab) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DeepCharcoal,
                                        selectedLabelColor = PurePorcelain
                                    )
                                )
                            }
                        }
                    }

                    if (filteredTickets.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = PurePorcelain),
                                border = BorderStroke(1.dp, SilkHairline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Belum ada antrean pada filter '$queueFilter'.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(24.dp)
                                )
                            }
                        }
                    }

                    items(filteredTickets, key = { it.id }) { ticket ->
                        AdminQueueTicketCard(
                            ticket = ticket,
                            onRecall = { viewModel.recallTicket(ticket.id) },
                            onStart = { viewModel.startService(ticket.id) },
                            onComplete = { viewModel.completeService(ticket.id) },
                            onSkip = { viewModel.skipTicket(ticket.id) },
                            onChangeStatus = { ticketForStatusChange = ticket },
                            onPrint = { ticketToPrint = ticket }
                        )
                    }
                }
            }

            1 -> {
                // TAB 2: KELOLA LAYANAN & HAIRSTYLIST
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 840.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Master Layanan (${uiState.salonServices.size})",
                                style = MaterialTheme.typography.titleLarge,
                                color = DeepCharcoal
                            )
                            Button(
                                onClick = { isCreatingService = true },
                                colors = ButtonDefaults.buttonColors(containerColor = RoseGoldTerracotta)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Layanan")
                            }
                        }
                    }

                    items(uiState.salonServices, key = { it.id }) { service ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PurePorcelain),
                            border = BorderStroke(1.dp, SilkHairline),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = service.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeepCharcoal
                                    )
                                    Text(
                                        text = "${service.category} • ${QrAndPrinterHelper.formatRupiah(service.priceRupiah)} • ${service.durationMinutes} mnt",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = RoseGoldTerracotta,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (service.isActive) "Status: Aktif" else "Status: Nonaktif",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row {
                                    IconButton(onClick = { editingService = service }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Ubah layanan")
                                    }
                                    IconButton(onClick = { viewModel.deleteService(service.id) }) {
                                        Icon(
                                            Icons.Filled.DeleteOutline,
                                            contentDescription = "Hapus layanan",
                                            tint = CrimsonRose
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daftar Hairstylist (${uiState.salonStylists.size})",
                                style = MaterialTheme.typography.titleLarge,
                                color = DeepCharcoal
                            )
                            Button(
                                onClick = { isCreatingStylist = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Kapster")
                            }
                        }
                    }

                    items(uiState.salonStylists, key = { it.id }) { stylist ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PurePorcelain),
                            border = BorderStroke(1.dp, SilkHairline),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${stylist.name} (${stylist.chairNumber})",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeepCharcoal
                                    )
                                    Text(
                                        text = "${stylist.specialty} • ${stylist.experienceYears} thn",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = stylist.isAvailable,
                                    onCheckedChange = {
                                        viewModel.saveStylist(stylist.copy(isAvailable = it))
                                    }
                                )
                                IconButton(onClick = { editingStylist = stylist }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Ubah kapster")
                                }
                                IconButton(onClick = { viewModel.deleteStylist(stylist.id) }) {
                                    Icon(
                                        Icons.Filled.DeleteOutline,
                                        contentDescription = "Hapus kapster",
                                        tint = CrimsonRose
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // TAB 3: PENGATURAN SALON & JAM OPERASIONAL
                selectedSalon?.let { salon ->
                    var salonNameEdit by remember(salon.id) { mutableStateOf(salon.name) }
                    var salonAddressEdit by remember(salon.id) { mutableStateOf(salon.address) }
                    var salonHoursEdit by remember(salon.id) { mutableStateOf(salon.openHoursLabel) }
                    var salonPolicyEdit by remember(salon.id) { mutableStateOf(salon.cancellationPolicy) }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 840.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = PurePorcelain),
                                border = BorderStroke(1.dp, SilkHairline),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = if (hasOwnerAccess) {
                                            "Pengaturan Profil Salon (Owner)"
                                        } else {
                                            "Informasi & Kebijakan Salon"
                                        },
                                        style = MaterialTheme.typography.titleLarge,
                                        color = DeepCharcoal
                                    )
                                    OutlinedTextField(
                                        value = salonNameEdit,
                                        onValueChange = { salonNameEdit = it },
                                        label = { Text("Nama Salon") },
                                        enabled = hasOwnerAccess,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = salonAddressEdit,
                                        onValueChange = { salonAddressEdit = it },
                                        label = { Text("Alamat Lengkap") },
                                        enabled = hasOwnerAccess,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = salonHoursEdit,
                                        onValueChange = { salonHoursEdit = it },
                                        label = { Text("Ringkasan Jam Buka") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = salonPolicyEdit,
                                        onValueChange = { salonPolicyEdit = it },
                                        label = { Text("Ketentuan Pembatalan") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            viewModel.updateSalonDetails(
                                                salonId = salon.id,
                                                name = salonNameEdit,
                                                address = salonAddressEdit,
                                                openHoursLabel = salonHoursEdit,
                                                cancellationPolicy = salonPolicyEdit
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoseGoldTerracotta),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Simpan Pengaturan Salon")
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Jadwal Jam Operasional Mingguan",
                                style = MaterialTheme.typography.titleLarge,
                                color = DeepCharcoal
                            )
                        }

                        items(salon.operatingSchedule.size) { index ->
                            val day = salon.operatingSchedule[index]
                            Card(
                                colors = CardDefaults.cardColors(containerColor = PurePorcelain),
                                border = BorderStroke(1.dp, SilkHairline),
                                shape = RoundedCornerShape(16.dp)
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
                                            text = day.dayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = DeepCharcoal
                                        )
                                        Text(
                                            text = if (day.isOpen) "${day.openTime} - ${day.closeTime} WIB" else "Libur / Tutup",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (day.isOpen) RoseGoldTerracotta else CrimsonRose
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (day.isOpen) {
                                            TextButton(
                                                onClick = {
                                                    val nextClose = if (day.closeTime == "20:00") "21:00" else "20:00"
                                                    viewModel.updateOperatingDay(
                                                        salon.id,
                                                        index,
                                                        day.copy(closeTime = nextClose)
                                                    )
                                                }
                                            ) {
                                                Text("Ubah Jam")
                                            }
                                        }
                                        Switch(
                                            checked = day.isOpen,
                                            onCheckedChange = { open ->
                                                viewModel.updateOperatingDay(
                                                    salon.id,
                                                    index,
                                                    day.copy(isOpen = open)
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // TAB 4: LAPORAN HARIAN & BULANAN
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 840.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PurePorcelain),
                            border = BorderStroke(1.dp, SilkHairline),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Ringkasan Laporan Harian",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = DeepCharcoal
                                )
                                Text(
                                    text = selectedSalon?.name ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = RoseGoldTerracotta
                                )
                                HorizontalDivider(color = SilkHairline)
                                ReportStatRow("Total Antrean Masuk", "${dailyReport.totalAntreanHariIni} pelanggan")
                                ReportStatRow("Selesai Dilayani", "${dailyReport.selesaiDilayani} pelanggan")
                                ReportStatRow("Sedang Menunggu / Dipanggil", "${dailyReport.sedangMenunggu} pelanggan")
                                ReportStatRow("Batal / Dilewati", "${dailyReport.dibatalkanAtauLewat} pelanggan")
                                ReportStatRow("Rata-rata Durasi", "${dailyReport.rataRataDurasiMenit} menit")
                                ReportStatRow("Layanan Terpopuler", dailyReport.layananTerpopuler)
                                ReportStatRow(
                                    "Estimasi Pendapatan Hari Ini",
                                    QrAndPrinterHelper.formatRupiah(dailyReport.estimasiPendapatanHariIni),
                                    highlight = true
                                )
                            }
                        }
                    }

                    item {
                        MonthlyRevenueChartCard(entries = uiState.monthlyReports)
                    }
                }
            }
        }
    }

    if (showQrScanner) {
        QrScannerSheet(
            tickets = uiState.tickets,
            lastResult = uiState.lastQrScanResult,
            onScanPayload = { raw -> viewModel.verifyScannedQrCode(raw) },
            onClearResult = { viewModel.clearQrScanResult() },
            onDismiss = {
                viewModel.clearQrScanResult()
                showQrScanner = false
            }
        )
    }

    if (showWalkInDialog) {
        WalkInDialog(
            services = uiState.salonServices,
            stylists = uiState.salonStylists,
            onDismiss = { showWalkInDialog = false },
            onSubmit = { name, phone, srvIds, stylistId ->
                viewModel.takeQueueTicket(
                    customerName = name,
                    customerPhone = phone,
                    isWalkIn = true,
                    customServiceIds = srvIds,
                    customStylistId = stylistId
                )
                showWalkInDialog = false
            }
        )
    }

    if (isCreatingService || editingService != null) {
        ServiceEditorDialog(
            salonId = uiState.selectedSalonId,
            existing = editingService,
            onDismiss = {
                isCreatingService = false
                editingService = null
            },
            onSave = { service ->
                viewModel.saveService(service)
                isCreatingService = false
                editingService = null
            }
        )
    }

    if (isCreatingStylist || editingStylist != null) {
        StylistEditorDialog(
            salonId = uiState.selectedSalonId,
            existing = editingStylist,
            onDismiss = {
                isCreatingStylist = false
                editingStylist = null
            },
            onSave = { stylist ->
                viewModel.saveStylist(stylist)
                isCreatingStylist = false
                editingStylist = null
            }
        )
    }

    ticketForStatusChange?.let { ticket ->
        ChangeStatusDialog(
            ticket = ticket,
            onDismiss = { ticketForStatusChange = null },
            onSelectStatus = { status ->
                viewModel.updateTicketStatus(ticket.id, status)
                ticketForStatusChange = null
            }
        )
    }

    ticketToPrint?.let { ticket ->
        TicketPrintAndQrSheet(
            ticket = ticket,
            onDismiss = { ticketToPrint = null },
            onMessage = { msg -> viewModel.postSnackbar(msg) }
        )
    }
}

@Composable
private fun StaffLoginGate(
    isOwnerMode: Boolean,
    authError: String?,
    onLogin: (String, String) -> Unit,
    onQuickDemoLogin: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember(isOwnerMode) {
        mutableStateOf(if (isOwnerMode) "owner@aurarose.id" else "admin@aurarose.id")
    }
    var pin by remember(isOwnerMode) {
        mutableStateOf(if (isOwnerMode) "owner123" else "123456")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PurePorcelain),
            border = BorderStroke(1.dp, SilkHairline),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = BlushMist,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = if (isOwnerMode) Icons.Filled.Storefront else Icons.Filled.Lock,
                            contentDescription = null,
                            tint = RoseGoldTerracotta,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isOwnerMode) "Login Pemilik Salon" else "Login Admin Salon",
                            style = MaterialTheme.typography.headlineMedium,
                            color = DeepCharcoal
                        )
                        Text(
                            text = "Masuk untuk mengelola antrean, QR, dan laporan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Staf / Owner") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("staff_email_input")
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("PIN / Kata Sandi") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("staff_pin_input")
                )

                if (!authError.isNullOrBlank()) {
                    Text(
                        text = authError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CrimsonRose
                    )
                }

                Button(
                    onClick = { onLogin(email, pin) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("staff_login_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseGoldTerracotta)
                ) {
                    Text(if (isOwnerMode) "Masuk sebagai Pemilik" else "Masuk sebagai Admin")
                }

                OutlinedButton(
                    onClick = { onQuickDemoLogin(isOwnerMode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("staff_quick_demo_login_button")
                ) {
                    Text(
                        if (isOwnerMode) "Masuk Cepat Demo (Pemilik Salon)"
                        else "Masuk Cepat Demo (Admin Salon)"
                    )
                }

                Text(
                    text = "Kredensial Demo: admin@aurarose.id / 123456 • owner@aurarose.id / owner123",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminQueueTicketCard(
    ticket: QueueTicket,
    onRecall: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onChangeStatus: () -> Unit,
    onPrint: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PurePorcelain),
        border = BorderStroke(1.dp, SilkHairline),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = BlushMist,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = ticket.queueNumber,
                            style = QueueNumberCardStyle,
                            color = DeepCharcoal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = ticket.customerName,
                                style = MaterialTheme.typography.titleMedium,
                                color = DeepCharcoal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (ticket.isWalkIn) {
                                Surface(color = AmberSoft, shape = RoundedCornerShape(6.dp)) {
                                    Text(
                                        text = "Walk-In",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AmberWarm,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = ticket.serviceNames.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${ticket.stylistName} • ${QrAndPrinterHelper.formatRupiah(ticket.totalPriceRupiah)} (${ticket.totalDurationMinutes} mnt)",
                            style = MaterialTheme.typography.labelMedium,
                            color = RoseGoldTerracotta
                        )
                    }
                }
                QueueStatusBadge(status = ticket.status)
            }

            QrVerificationBadge(status = ticket.qrVerificationStatus)

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (ticket.status == QueueStatus.DIPANGGIL ||
                    ticket.status == QueueStatus.MENUNGGU ||
                    ticket.status == QueueStatus.HAMPIR_DIPANGGIL
                ) {
                    OutlinedButton(onClick = onRecall) {
                        Icon(Icons.Filled.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Panggil Ulang")
                    }
                }
                if (ticket.status == QueueStatus.MENUNGGU ||
                    ticket.status == QueueStatus.HAMPIR_DIPANGGIL ||
                    ticket.status == QueueStatus.DIPANGGIL
                ) {
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSage)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mulai")
                    }
                }
                if (ticket.status == QueueStatus.SEDANG_DILAYANI || ticket.status == QueueStatus.DIPANGGIL) {
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = RoseGoldTerracotta)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Selesai")
                    }
                }
                if (ticket.status == QueueStatus.MENUNGGU ||
                    ticket.status == QueueStatus.HAMPIR_DIPANGGIL ||
                    ticket.status == QueueStatus.DIPANGGIL
                ) {
                    OutlinedButton(onClick = onSkip) {
                        Icon(Icons.Filled.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lewati")
                    }
                }
                OutlinedButton(onClick = onPrint) {
                    Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Struk 58/80")
                }
                TextButton(onClick = onChangeStatus) {
                    Text("Ubah Status")
                }
            }
        }
    }
}

@Composable
private fun KpiMiniCard(
    label: String,
    value: String,
    container: androidx.compose.ui.graphics.Color,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = container,
        border = BorderStroke(1.dp, SilkHairline),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReportStatRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (highlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (highlight) RoseGoldTerracotta else DeepCharcoal
        )
    }
}

@Composable
private fun MonthlyRevenueChartCard(entries: List<MonthlyReportEntry>) {
    val maxRevenue = remember(entries) { entries.maxOfOrNull { it.pendapatanRupiah }?.coerceAtLeast(1L) ?: 1L }

    Card(
        colors = CardDefaults.cardColors(containerColor = PurePorcelain),
        border = BorderStroke(1.dp, SilkHairline),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Laporan Pendapatan & Kunjungan Bulanan",
                style = MaterialTheme.typography.titleLarge,
                color = DeepCharcoal
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .padding(top = 8.dp)
            ) {
                val count = entries.size.coerceAtLeast(1)
                val slotWidth = size.width / count
                val barWidth = (slotWidth * 0.48f).coerceAtMost(44.dp.toPx())
                entries.forEachIndexed { idx, entry ->
                    val ratio = entry.pendapatanRupiah.toFloat() / maxRevenue.toFloat()
                    val barHeight = size.height * 0.85f * ratio
                    val left = idx * slotWidth + (slotWidth - barWidth) / 2f
                    val top = size.height - barHeight
                    drawRoundRect(
                        color = if (idx == entries.lastIndex) RoseGoldTerracotta else ChampagneGold.copy(alpha = 0.72f),
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                entries.forEach { entry ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = entry.monthLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = DeepCharcoal
                        )
                        Text(
                            text = "${entry.totalPelanggan} org",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = SilkHairline)

            entries.forEach { entry ->
                ReportStatRow(
                    label = "${entry.monthLabel} (${entry.totalPelanggan} pelanggan)",
                    value = QrAndPrinterHelper.formatRupiah(entry.pendapatanRupiah)
                )
            }
        }
    }
}

package com.example.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AntriYukDatabase
import com.example.data.SalonQueueRepository
import com.example.model.DailyReportSummary
import com.example.model.DummySalonData
import com.example.model.Hairstylist
import com.example.model.InAppNotification
import com.example.model.MonthlyReportEntry
import com.example.model.NotificationChannelType
import com.example.model.OperatingDayHours
import com.example.model.QrScanValidationResult
import com.example.model.QrVerificationStatus
import com.example.model.QueueStatus
import com.example.model.QueueTicket
import com.example.model.Salon
import com.example.model.SalonService
import com.example.model.UserRole
import com.example.util.NotificationHelper
import com.example.util.QrAndPrinterHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

data class AntriYukUiState(
    val currentRole: UserRole = UserRole.PELANGGAN,
    val salons: List<Salon> = emptyList(),
    val selectedSalonId: String = "salon_aura",
    val services: List<SalonService> = emptyList(),
    val stylists: List<Hairstylist> = emptyList(),
    val tickets: List<QueueTicket> = emptyList(),
    val selectedServiceIds: Set<String> = setOf("srv_1"),
    val selectedStylistId: String? = null, // null = Kapster Tersedia / Tercepat
    val activeCustomerTicketId: String? = "tkt_005",
    val isAdminAuthenticated: Boolean = false,
    val loggedInStaffRole: UserRole? = null,
    val loggedInStaffName: String = "",
    val authErrorMessage: String? = null,
    val notifications: List<InAppNotification> = emptyList(),
    val activeBannerNotification: InAppNotification? = null,
    val lastQrScanResult: QrScanValidationResult? = null,
    val isTtsReady: Boolean = false,
    val isTtsMuted: Boolean = false,
    val lastSpokenAnnouncement: String? = null,
    val monthlyReports: List<MonthlyReportEntry> = DummySalonData.monthlyReports,
    val statusSnackbarMessage: String? = null
) {
    val selectedSalon: Salon?
        get() = salons.find { it.id == selectedSalonId } ?: salons.firstOrNull()

    val salonServices: List<SalonService>
        get() = services.filter { it.salonId == selectedSalonId }

    val salonStylists: List<Hairstylist>
        get() = stylists.filter { it.salonId == selectedSalonId }

    val salonTickets: List<QueueTicket>
        get() = tickets.filter { it.salonId == selectedSalonId }

    val activeCustomerTicket: QueueTicket?
        get() = tickets.find { it.id == activeCustomerTicketId }
            ?: tickets.lastOrNull {
                it.salonId == selectedSalonId &&
                    it.status in listOf(
                        QueueStatus.MENUNGGU,
                        QueueStatus.HAMPIR_DIPANGGIL,
                        QueueStatus.DIPANGGIL,
                        QueueStatus.SEDANG_DILAYANI
                    )
            }

    val currentlyCalledTicket: QueueTicket?
        get() = salonTickets.lastOrNull { it.status == QueueStatus.DIPANGGIL }
            ?: salonTickets.lastOrNull { it.status == QueueStatus.SEDANG_DILAYANI }

    val nextWaitingTicket: QueueTicket?
        get() = salonTickets.firstOrNull { it.status == QueueStatus.HAMPIR_DIPANGGIL }
            ?: salonTickets.firstOrNull { it.status == QueueStatus.MENUNGGU }

    val waitingTicketsForSelectedSalon: List<QueueTicket>
        get() = salonTickets.filter {
            it.status == QueueStatus.MENUNGGU || it.status == QueueStatus.HAMPIR_DIPANGGIL
        }

    fun getPositionAhead(ticket: QueueTicket): Int {
        if (ticket.status != QueueStatus.MENUNGGU && ticket.status != QueueStatus.HAMPIR_DIPANGGIL) {
            return 0
        }
        return tickets.count {
            it.salonId == ticket.salonId &&
                (it.status == QueueStatus.MENUNGGU || it.status == QueueStatus.HAMPIR_DIPANGGIL) &&
                it.createdAtMillis < ticket.createdAtMillis
        }
    }

    fun buildDailyReport(): DailyReportSummary {
        val list = salonTickets
        val done = list.filter { it.status == QueueStatus.SELESAI }
        val waiting = list.count {
            it.status == QueueStatus.MENUNGGU ||
                it.status == QueueStatus.HAMPIR_DIPANGGIL ||
                it.status == QueueStatus.DIPANGGIL
        }
        val cancelledOrSkipped = list.count {
            it.status == QueueStatus.DIBATALKAN || it.status == QueueStatus.DILEWATI
        }
        val revenue = list
            .filter { it.status == QueueStatus.SELESAI || it.status == QueueStatus.SEDANG_DILAYANI }
            .sumOf { it.totalPriceRupiah }
        val avgDuration = if (list.isNotEmpty()) {
            list.map { it.totalDurationMinutes }.average().toInt()
        } else 0
        val topService = list
            .flatMap { it.serviceNames }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: "-"

        return DailyReportSummary(
            totalAntreanHariIni = list.size,
            selesaiDilayani = done.size,
            sedangMenunggu = waiting,
            dibatalkanAtauLewat = cancelledOrSkipped,
            estimasiPendapatanHariIni = revenue,
            rataRataDurasiMenit = avgDuration,
            layananTerpopuler = topService
        )
    }
}

private data class LocalSessionState(
    val currentRole: UserRole = UserRole.PELANGGAN,
    val selectedSalonId: String = "salon_aura",
    val selectedServiceIds: Set<String> = setOf("srv_1"),
    val selectedStylistId: String? = null,
    val activeCustomerTicketId: String? = "tkt_005",
    val isAdminAuthenticated: Boolean = false,
    val loggedInStaffRole: UserRole? = null,
    val loggedInStaffName: String = "",
    val authErrorMessage: String? = null,
    val notifications: List<InAppNotification> = listOf(
        InAppNotification(
            id = "notif_init",
            title = "Antrean Hampir Dipanggil",
            message = "Nomor antrean A-005 (Sekar Kinanti) tinggal 1 antrean lagi menuju kursi layanan.",
            channelType = NotificationChannelType.WHATSAPP_SIM,
            whatsappPreviewText = "Halo Sekar Kinanti, nomor antrean *A-005* di *Aura Rose Beauty & Hair* sebentar lagi dipanggil. Mohon bersiap di area tunggu salon."
        )
    ),
    val activeBannerNotification: InAppNotification? = null,
    val lastQrScanResult: QrScanValidationResult? = null,
    val isTtsReady: Boolean = false,
    val isTtsMuted: Boolean = false,
    val lastSpokenAnnouncement: String? = null,
    val statusSnackbarMessage: String? = null
)

class QueueViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val repository = SalonQueueRepository(
        AntriYukDatabase.getInstance(application).antriYukDao()
    )
    private val localState = MutableStateFlow(LocalSessionState())
    private var textToSpeech: TextToSpeech? = null

    val uiState: StateFlow<AntriYukUiState> = combine(
        repository.salonsFlow,
        repository.servicesFlow,
        repository.stylistsFlow,
        repository.ticketsFlow,
        localState
    ) { salons, services, stylists, tickets, session ->
        AntriYukUiState(
            currentRole = session.currentRole,
            salons = salons,
            selectedSalonId = session.selectedSalonId,
            services = services,
            stylists = stylists,
            tickets = tickets,
            selectedServiceIds = session.selectedServiceIds,
            selectedStylistId = session.selectedStylistId,
            activeCustomerTicketId = session.activeCustomerTicketId,
            isAdminAuthenticated = session.isAdminAuthenticated,
            loggedInStaffRole = session.loggedInStaffRole,
            loggedInStaffName = session.loggedInStaffName,
            authErrorMessage = session.authErrorMessage,
            notifications = session.notifications,
            activeBannerNotification = session.activeBannerNotification,
            lastQrScanResult = session.lastQrScanResult,
            isTtsReady = session.isTtsReady,
            isTtsMuted = session.isTtsMuted,
            lastSpokenAnnouncement = session.lastSpokenAnnouncement,
            statusSnackbarMessage = session.statusSnackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AntriYukUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
        NotificationHelper.ensureChannel(application)
        textToSpeech = TextToSpeech(application, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale("id", "ID"))
            val ready = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED
            textToSpeech?.setSpeechRate(0.95f)
            localState.update { it.copy(isTtsReady = ready) }
        }
    }

    override fun onCleared() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onCleared()
    }

    fun switchRole(role: UserRole) {
        localState.update { state ->
            // Jika beralih langsung ke mode Owner saat belum login, biarkan tetap lewat gerbang login yang bisa 1-tap demo
            state.copy(
                currentRole = role,
                authErrorMessage = null
            )
        }
    }

    fun selectSalon(salonId: String) {
        val currentServices = uiState.value.services.filter { it.salonId == salonId && it.isActive }
        val firstServiceId = currentServices.firstOrNull()?.id
        localState.update {
            it.copy(
                selectedSalonId = salonId,
                selectedServiceIds = if (firstServiceId != null) setOf(firstServiceId) else emptySet(),
                selectedStylistId = null
            )
        }
    }

    fun toggleServiceSelection(serviceId: String) {
        localState.update { state ->
            val updated = state.selectedServiceIds.toMutableSet()
            if (updated.contains(serviceId)) {
                if (updated.size > 1) updated.remove(serviceId)
            } else {
                updated.add(serviceId)
            }
            state.copy(selectedServiceIds = updated)
        }
    }

    fun selectStylist(stylistId: String?) {
        localState.update { it.copy(selectedStylistId = stylistId) }
    }

    fun selectCustomerTicket(ticketId: String) {
        localState.update { it.copy(activeCustomerTicketId = ticketId) }
    }

    fun loginStaff(email: String, pin: String, targetRole: UserRole) {
        val trimmedEmail = email.trim().lowercase()
        val trimmedPin = pin.trim()
        val valid = when {
            trimmedEmail == "owner@aurarose.id" && trimmedPin == "owner123" -> {
                Triple(true, UserRole.PEMILIK_SALON, "Ibu Ratna Kusuma (Owner)")
            }
            trimmedEmail == "admin@aurarose.id" && trimmedPin == "123456" -> {
                Triple(true, targetRole.takeIf { it == UserRole.PEMILIK_SALON } ?: UserRole.ADMIN_SALON, "Dina Resepsionis (Admin)")
            }
            trimmedPin == "123456" && trimmedEmail.isNotBlank() -> {
                Triple(true, targetRole, trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() })
            }
            else -> Triple(false, targetRole, "")
        }

        if (valid.first) {
            localState.update {
                it.copy(
                    isAdminAuthenticated = true,
                    loggedInStaffRole = valid.second,
                    loggedInStaffName = valid.third,
                    currentRole = if (targetRole == UserRole.PEMILIK_SALON || valid.second == UserRole.PEMILIK_SALON) {
                        UserRole.PEMILIK_SALON
                    } else {
                        UserRole.ADMIN_SALON
                    },
                    authErrorMessage = null
                )
            }
        } else {
            localState.update {
                it.copy(authErrorMessage = "Email atau PIN tidak sesuai. Gunakan akun demo yang tersedia.")
            }
        }
    }

    fun quickDemoLogin(asOwner: Boolean) {
        localState.update {
            it.copy(
                isAdminAuthenticated = true,
                loggedInStaffRole = if (asOwner) UserRole.PEMILIK_SALON else UserRole.ADMIN_SALON,
                loggedInStaffName = if (asOwner) "Ratna Kusuma (Pemilik)" else "Dina Putri (Admin Salon)",
                currentRole = if (asOwner) UserRole.PEMILIK_SALON else UserRole.ADMIN_SALON,
                authErrorMessage = null
            )
        }
    }

    fun logoutStaff() {
        localState.update {
            it.copy(
                isAdminAuthenticated = false,
                loggedInStaffRole = null,
                loggedInStaffName = "",
                authErrorMessage = null
            )
        }
    }

    fun takeQueueTicket(
        customerName: String,
        customerPhone: String,
        isWalkIn: Boolean = false,
        customServiceIds: Set<String>? = null,
        customStylistId: String? = null
    ) {
        val state = uiState.value
        val salon = state.selectedSalon ?: return
        if (!salon.isOpen) {
            postSnackbar("Maaf, ${salon.name} sedang tutup. Silakan pilih salon lain yang buka.")
            return
        }
        val cleanName = customerName.trim()
        if (cleanName.isBlank()) {
            postSnackbar("Masukkan nama pelanggan terlebih dahulu.")
            return
        }
        val serviceIdsToUse = (customServiceIds ?: state.selectedServiceIds).toList()
        val chosenServices = state.services.filter { it.id in serviceIdsToUse }
        if (chosenServices.isEmpty()) {
            postSnackbar("Pilih minimal satu layanan salon.")
            return
        }

        val stylistIdToUse = if (isWalkIn) customStylistId else state.selectedStylistId
        val chosenStylist = state.stylists.find { it.id == stylistIdToUse }
        val stylistName = chosenStylist?.name ?: "Kapster Tersedia / Tercepat"

        val queueNumber = generateNextQueueNumber(salon, state.tickets)
        val waitingAhead = state.tickets.filter {
            it.salonId == salon.id &&
                (it.status == QueueStatus.MENUNGGU ||
                    it.status == QueueStatus.HAMPIR_DIPANGGIL ||
                    it.status == QueueStatus.DIPANGGIL)
        }
        val activeStylistCount = state.salonStylists.count { it.isAvailable }.coerceAtLeast(1)
        val totalMinutesAhead = waitingAhead.sumOf { it.totalDurationMinutes } / activeStylistCount
        val now = System.currentTimeMillis()
        val estimatedWaitMinutes = (totalMinutesAhead + 10).coerceAtLeast(5)
        val estimatedServiceMillis = now + estimatedWaitMinutes * 60_000L

        val ticketId = "tkt_${UUID.randomUUID().toString().take(8)}"
        val nonce = QrAndPrinterHelper.generateSecureNonce()
        val qrToken = QrAndPrinterHelper.buildSignedQrToken(ticketId, queueNumber, nonce)

        val ticket = QueueTicket(
            id = ticketId,
            salonId = salon.id,
            salonName = salon.name,
            queueNumber = queueNumber,
            customerName = cleanName,
            customerPhone = customerPhone.ifBlank { "-" },
            serviceIds = chosenServices.map { it.id },
            serviceNames = chosenServices.map { it.name },
            totalPriceRupiah = chosenServices.sumOf { it.priceRupiah },
            totalDurationMinutes = chosenServices.sumOf { it.durationMinutes },
            stylistId = stylistIdToUse,
            stylistName = stylistName,
            status = QueueStatus.MENUNGGU,
            qrToken = qrToken,
            qrNonce = nonce,
            qrVerificationStatus = QrVerificationStatus.BELUM_DIPINDAI,
            createdAtMillis = now,
            estimatedServiceMillis = estimatedServiceMillis,
            isWalkIn = isWalkIn
        )

        viewModelScope.launch {
            repository.saveTicket(ticket)
            if (!isWalkIn) {
                localState.update { it.copy(activeCustomerTicketId = ticket.id) }
            }
            val estStr = QrAndPrinterHelper.formatJam(estimatedServiceMillis)
            val waMsg = "Halo $cleanName, nomor antrean *$queueNumber* di *${salon.name}* berhasil diambil. " +
                "Layanan: ${ticket.serviceNames.joinToString(", ")}. Estimasi giliran pukul $estStr. " +
                "Tunjukkan QR Code pada tiket saat dipanggil."
            pushNotification(
                title = "Antrean $queueNumber Berhasil Diambil",
                message = "Tiket untuk $cleanName di ${salon.name}. Estimasi pelayanan $estStr.",
                channelType = NotificationChannelType.WHATSAPP_SIM,
                whatsappPreview = waMsg,
                alsoSystemNotify = true
            )
            postSnackbar(
                if (isWalkIn) "Walk-in $queueNumber ($cleanName) ditambahkan."
                else "Tiket $queueNumber siap! QR Code sudah diterbitkan."
            )
        }
    }

    fun cancelCustomerTicket(ticketId: String) {
        val ticket = uiState.value.tickets.find { it.id == ticketId } ?: return
        if (ticket.status == QueueStatus.SEDANG_DILAYANI || ticket.status == QueueStatus.SELESAI) {
            postSnackbar("Antrean yang sedang dilayani atau selesai tidak dapat dibatalkan.")
            return
        }
        val updated = ticket.copy(
            status = QueueStatus.DIBATALKAN,
            qrVerificationStatus = QrVerificationStatus.DIBATALKAN
        )
        viewModelScope.launch {
            repository.saveTicket(updated)
            pushNotification(
                title = "Antrean ${ticket.queueNumber} Dibatalkan",
                message = "Tiket atas nama ${ticket.customerName} telah dibatalkan sesuai ketentuan salon.",
                channelType = NotificationChannelType.IN_APP,
                alsoSystemNotify = false
            )
            postSnackbar("Antrean ${ticket.queueNumber} berhasil dibatalkan.")
        }
    }

    fun callNextQueue(salonId: String = uiState.value.selectedSalonId) {
        val state = uiState.value
        val salonTickets = state.tickets.filter { it.salonId == salonId }
        val nextTicket = salonTickets.firstOrNull { it.status == QueueStatus.HAMPIR_DIPANGGIL }
            ?: salonTickets.firstOrNull { it.status == QueueStatus.MENUNGGU }

        if (nextTicket == null) {
            postSnackbar("Tidak ada antrean menunggu di salon ini.")
            return
        }

        val availableStylists = state.stylists.filter { it.salonId == salonId && it.isAvailable }
        val assignedStylist = if (nextTicket.stylistId == null) {
            availableStylists.firstOrNull()
        } else {
            state.stylists.find { it.id == nextTicket.stylistId }
        }

        val calledTicket = nextTicket.copy(
            status = QueueStatus.DIPANGGIL,
            calledAtMillis = System.currentTimeMillis(),
            stylistId = assignedStylist?.id ?: nextTicket.stylistId,
            stylistName = assignedStylist?.name ?: nextTicket.stylistName
        )

        // Promosikan antrean menunggu berikutnya menjadi HAMPIR_DIPANGGIL
        val followingTicket = salonTickets.firstOrNull {
            it.id != nextTicket.id && it.status == QueueStatus.MENUNGGU
        }?.copy(status = QueueStatus.HAMPIR_DIPANGGIL)

        viewModelScope.launch {
            repository.saveTicket(calledTicket)
            if (followingTicket != null) {
                repository.saveTicket(followingTicket)
                pushNotification(
                    title = "Hampir Dipanggil: ${followingTicket.queueNumber}",
                    message = "Halo ${followingTicket.customerName}, bersiaplah! Anda adalah antrean berikutnya.",
                    channelType = NotificationChannelType.WHATSAPP_SIM,
                    whatsappPreview = "Halo ${followingTicket.customerName}, antrean *${followingTicket.queueNumber}* di *${followingTicket.salonName}* HAMPIR DIPANGGIL. Silakan bersiap.",
                    alsoSystemNotify = true
                )
            }

            val chairText = assignedStylist?.chairNumber ?: "meja layanan"
            pushNotification(
                title = "Dipanggil Sekarang: ${calledTicket.queueNumber}",
                message = "${calledTicket.customerName} silakan menuju ${calledTicket.stylistName} ($chairText).",
                channelType = NotificationChannelType.WHATSAPP_SIM,
                whatsappPreview = "Halo ${calledTicket.customerName}, nomor antrean *${calledTicket.queueNumber}* SEDANG DIPANGGIL di *${calledTicket.salonName}*. Silakan menuju ${calledTicket.stylistName} ($chairText).",
                alsoSystemNotify = true
            )

            announceTicketVoice(calledTicket, isRecall = false)
            postSnackbar("Memanggil nomor antrean ${calledTicket.queueNumber}.")
        }
    }

    fun recallTicket(ticketId: String) {
        val ticket = uiState.value.tickets.find { it.id == ticketId } ?: return
        val updated = ticket.copy(
            status = QueueStatus.DIPANGGIL,
            calledAtMillis = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.saveTicket(updated)
            announceTicketVoice(updated, isRecall = true)
            pushNotification(
                title = "Panggilan Ulang: ${ticket.queueNumber}",
                message = "${ticket.customerName} dimohon segera menuju ${ticket.stylistName}.",
                channelType = NotificationChannelType.IN_APP,
                alsoSystemNotify = true
            )
            postSnackbar("Panggilan ulang suara untuk ${ticket.queueNumber}.")
        }
    }

    fun startService(ticketId: String) {
        val ticket = uiState.value.tickets.find { it.id == ticketId } ?: return
        val updated = ticket.copy(
            status = QueueStatus.SEDANG_DILAYANI,
            startedAtMillis = System.currentTimeMillis(),
            qrVerificationStatus = if (ticket.qrVerificationStatus == QrVerificationStatus.BELUM_DIPINDAI) {
                QrVerificationStatus.SUDAH_DIVERIFIKASI
            } else {
                ticket.qrVerificationStatus
            }
        )
        viewModelScope.launch {
            repository.saveTicket(updated)
            postSnackbar("Pelayanan ${ticket.queueNumber} (${ticket.customerName}) dimulai.")
        }
    }

    fun completeService(ticketId: String) {
        val ticket = uiState.value.tickets.find { it.id == ticketId } ?: return
        val updated = ticket.copy(
            status = QueueStatus.SELESAI,
            completedAtMillis = System.currentTimeMillis(),
            qrVerificationStatus = QrVerificationStatus.SELESAI
        )
        viewModelScope.launch {
            repository.saveTicket(updated)
            postSnackbar("Pelayanan ${ticket.queueNumber} selesai.")
        }
    }

    fun skipTicket(ticketId: String) {
        val ticket = uiState.value.tickets.find { it.id == ticketId } ?: return
        val updated = ticket.copy(status = QueueStatus.DILEWATI)
        viewModelScope.launch {
            repository.saveTicket(updated)
            postSnackbar("Antrean ${ticket.queueNumber} dilewati.")
        }
    }

    fun updateTicketStatus(ticketId: String, newStatus: QueueStatus) {
        val ticket = uiState.value.tickets.find { it.id == ticketId } ?: return
        val newQrStatus = when (newStatus) {
            QueueStatus.DIBATALKAN -> QrVerificationStatus.DIBATALKAN
            QueueStatus.SELESAI -> QrVerificationStatus.SELESAI
            QueueStatus.SEDANG_DILAYANI -> QrVerificationStatus.SUDAH_DIVERIFIKASI
            else -> ticket.qrVerificationStatus
        }
        val updated = ticket.copy(
            status = newStatus,
            qrVerificationStatus = newQrStatus,
            calledAtMillis = if (newStatus == QueueStatus.DIPANGGIL) System.currentTimeMillis() else ticket.calledAtMillis,
            startedAtMillis = if (newStatus == QueueStatus.SEDANG_DILAYANI) System.currentTimeMillis() else ticket.startedAtMillis,
            completedAtMillis = if (newStatus == QueueStatus.SELESAI) System.currentTimeMillis() else ticket.completedAtMillis
        )
        viewModelScope.launch {
            repository.saveTicket(updated)
            if (newStatus == QueueStatus.DIPANGGIL) {
                announceTicketVoice(updated, isRecall = false)
            }
            postSnackbar("Status ${ticket.queueNumber} diubah menjadi ${newStatus.label}.")
        }
    }

    fun verifyScannedQrCode(rawPayload: String) {
        val result = QrAndPrinterHelper.validateScannedQr(rawPayload, uiState.value.tickets)
        if (result.isSuccess && result.ticket != null) {
            val verifiedTicket = result.ticket.copy(
                qrVerificationStatus = QrVerificationStatus.SUDAH_DIVERIFIKASI,
                status = if (result.ticket.status == QueueStatus.DIPANGGIL) {
                    QueueStatus.SEDANG_DILAYANI
                } else {
                    result.ticket.status
                },
                startedAtMillis = if (result.ticket.status == QueueStatus.DIPANGGIL) {
                    System.currentTimeMillis()
                } else {
                    result.ticket.startedAtMillis
                }
            )
            viewModelScope.launch {
                repository.saveTicket(verifiedTicket)
            }
            localState.update {
                it.copy(
                    lastQrScanResult = result.copy(ticket = verifiedTicket)
                )
            }
        } else {
            localState.update { it.copy(lastQrScanResult = result) }
        }
    }

    fun clearQrScanResult() {
        localState.update { it.copy(lastQrScanResult = null) }
    }

    // --- Pengaturan & Master Data (Admin & Pemilik Salon) ---

    fun saveService(service: SalonService) {
        viewModelScope.launch {
            repository.saveService(service)
            postSnackbar("Layanan '${service.name}' berhasil disimpan.")
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            repository.deleteService(serviceId)
            postSnackbar("Layanan berhasil dihapus.")
        }
    }

    fun saveStylist(stylist: Hairstylist) {
        viewModelScope.launch {
            repository.saveStylist(stylist)
            postSnackbar("Data hairstylist '${stylist.name}' diperbarui.")
        }
    }

    fun deleteStylist(stylistId: String) {
        viewModelScope.launch {
            repository.deleteStylist(stylistId)
            postSnackbar("Hairstylist dihapus.")
        }
    }

    fun toggleSalonOpen(salonId: String) {
        val salon = uiState.value.salons.find { it.id == salonId } ?: return
        val nextOpen = !salon.isOpen
        val updatedLabel = if (nextOpen) {
            salon.openHoursLabel.replace(" (Sedang Tutup)", "")
        } else {
            "${salon.openHoursLabel.replace(" (Sedang Tutup)", "")} (Sedang Tutup)"
        }
        viewModelScope.launch {
            repository.saveSalon(salon.copy(isOpen = nextOpen, openHoursLabel = updatedLabel))
            postSnackbar(
                if (nextOpen) "${salon.name} sekarang BUKA." else "${salon.name} sekarang TUTUP."
            )
        }
    }

    fun updateSalonDetails(
        salonId: String,
        name: String,
        address: String,
        openHoursLabel: String,
        cancellationPolicy: String
    ) {
        val salon = uiState.value.salons.find { it.id == salonId } ?: return
        viewModelScope.launch {
            repository.saveSalon(
                salon.copy(
                    name = name.trim().ifBlank { salon.name },
                    address = address.trim().ifBlank { salon.address },
                    openHoursLabel = openHoursLabel.trim().ifBlank { salon.openHoursLabel },
                    cancellationPolicy = cancellationPolicy.trim().ifBlank { salon.cancellationPolicy }
                )
            )
            postSnackbar("Informasi salon berhasil diperbarui.")
        }
    }

    fun updateOperatingDay(salonId: String, dayIndex: Int, updatedDay: OperatingDayHours) {
        val salon = uiState.value.salons.find { it.id == salonId } ?: return
        val schedule = salon.operatingSchedule.toMutableList()
        if (dayIndex in schedule.indices) {
            schedule[dayIndex] = updatedDay
            viewModelScope.launch {
                repository.saveSalon(salon.copy(operatingSchedule = schedule))
                postSnackbar("Jam operasional hari ${updatedDay.dayName} disimpan.")
            }
        }
    }

    // --- Pengumuman Suara TTS Bahasa Indonesia (Layar TV & Panggilan) ---

    fun toggleTtsMute() {
        val nextMuted = !localState.value.isTtsMuted
        if (nextMuted) textToSpeech?.stop()
        localState.update { it.copy(isTtsMuted = nextMuted) }
    }

    fun announceTicketVoice(ticket: QueueTicket, isRecall: Boolean = false) {
        val spokenNumber = formatQueueNumberForIndonesianSpeech(ticket.queueNumber)
        val stylistPhrase = if (ticket.stylistName.contains("Kapster Tersedia")) {
            "meja layanan yang tersedia"
        } else {
            "kursi kapster ${ticket.stylistName}"
        }
        val prefix = if (isRecall) "Panggilan ulang. " else ""
        val sentence = "${prefix}Nomor antrean $spokenNumber, atas nama ${ticket.customerName}, " +
            "silakan menuju ke $stylistPhrase di ${ticket.salonName}."

        localState.update { it.copy(lastSpokenAnnouncement = sentence) }

        if (!localState.value.isTtsMuted && localState.value.isTtsReady) {
            textToSpeech?.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, "antriyuk_${ticket.id}")
        }
    }

    private fun formatQueueNumberForIndonesianSpeech(queueNumber: String): String {
        val parts = queueNumber.split("-")
        if (parts.size != 2) return queueNumber
        val digits = parts[1].map { ch ->
            when (ch) {
                '0' -> "kosong"
                '1' -> "satu"
                '2' -> "dua"
                '3' -> "tiga"
                '4' -> "empat"
                '5' -> "lima"
                '6' -> "enam"
                '7' -> "tujuh"
                '8' -> "delapan"
                '9' -> "sembilan"
                else -> ch.toString()
            }
        }.joinToString(" ")
        return "${parts[0]}, $digits"
    }

    // --- Notifikasi ---

    fun dismissBannerNotification() {
        localState.update { it.copy(activeBannerNotification = null) }
    }

    fun markAllNotificationsRead() {
        localState.update { state ->
            state.copy(notifications = state.notifications.map { it.copy(isRead = true) })
        }
    }

    fun clearSnackbar() {
        localState.update { it.copy(statusSnackbarMessage = null) }
    }

    fun postSnackbar(message: String) {
        localState.update { it.copy(statusSnackbarMessage = message) }
    }

    private fun pushNotification(
        title: String,
        message: String,
        channelType: NotificationChannelType,
        whatsappPreview: String? = null,
        alsoSystemNotify: Boolean
    ) {
        val notif = InAppNotification(
            id = "ntf_${UUID.randomUUID().toString().take(8)}",
            title = title,
            message = message,
            channelType = channelType,
            whatsappPreviewText = whatsappPreview
        )
        localState.update { state ->
            state.copy(
                notifications = listOf(notif) + state.notifications,
                activeBannerNotification = notif
            )
        }
        if (alsoSystemNotify) {
            NotificationHelper.showQueueNotification(
                context = getApplication(),
                notificationId = notif.id.hashCode(),
                title = title,
                body = message
            )
        }
    }

    private fun generateNextQueueNumber(salon: Salon, tickets: List<QueueTicket>): String {
        val maxNum = tickets
            .filter { it.salonId == salon.id }
            .mapNotNull { it.queueNumber.substringAfter("-", "").toIntOrNull() }
            .maxOrNull() ?: 0
        val next = maxNum + 1
        return "${salon.queuePrefix}-${next.toString().padStart(3, '0')}"
    }
}

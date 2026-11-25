package com.example.model

import androidx.annotation.DrawableRes
import com.example.R

enum class UserRole(val label: String, val shortLabel: String) {
    PELANGGAN("Pelanggan", "Pelanggan"),
    ADMIN_SALON("Admin Salon", "Admin"),
    PEMILIK_SALON("Pemilik Salon", "Owner"),
    LAYAR_TV("Layar TV Antrean", "Layar TV")
}

enum class QueueStatus(val label: String) {
    MENUNGGU("Menunggu"),
    HAMPIR_DIPANGGIL("Hampir Dipanggil"),
    DIPANGGIL("Dipanggil"),
    SEDANG_DILAYANI("Sedang Dilayani"),
    SELESAI("Selesai"),
    DILEWATI("Dilewati"),
    DIBATALKAN("Dibatalkan")
}

enum class QrVerificationStatus(val label: String, val description: String) {
    BELUM_DIPINDAI("Belum Dipindai", "Tiket sah dan siap diverifikasi di meja resepsionis."),
    SUDAH_DIVERIFIKASI("Sudah Diverifikasi", "QR Code sudah pernah dipindai & terverifikasi."),
    DIBATALKAN("Dibatalkan", "Tiket ini telah dibatalkan sehingga tidak berlaku."),
    SELESAI("Selesai", "Pelayanan untuk tiket ini sudah selesai."),
    TIDAK_VALID("Tidak Valid / Palsu", "Tanda tangan keamanan QR tidak cocok atau rusak.")
}

enum class PrinterWidth(val label: String, val paperMm: Int, val charsPerLine: Int) {
    THERMAL_58MM("Printer Thermal 58 mm", 58, 32),
    THERMAL_80MM("Printer Thermal 80 mm", 80, 48)
}

enum class NotificationChannelType(val label: String) {
    IN_APP("Notifikasi Aplikasi"),
    WHATSAPP_SIM("Simulasi WhatsApp"),
    SISTEM("Notifikasi Perangkat")
}

data class OperatingDayHours(
    val dayName: String,
    val isOpen: Boolean,
    val openTime: String,
    val closeTime: String
)

data class Salon(
    val id: String,
    val name: String,
    val tagline: String,
    val address: String,
    val phoneWhatsApp: String,
    val isOpen: Boolean,
    val openHoursLabel: String,
    val cancellationPolicy: String,
    val queuePrefix: String,
    val rating: Double,
    @DrawableRes val imageRes: Int,
    val operatingSchedule: List<OperatingDayHours> = defaultOperatingSchedule()
)

data class SalonService(
    val id: String,
    val salonId: String,
    val name: String,
    val category: String,
    val description: String,
    val priceRupiah: Long,
    val durationMinutes: Int,
    val isActive: Boolean = true
)

data class Hairstylist(
    val id: String,
    val salonId: String,
    val name: String,
    val specialty: String,
    val experienceYears: Int,
    val isAvailable: Boolean = true,
    val chairNumber: String = "Kursi 1"
)

data class QueueTicket(
    val id: String,
    val salonId: String,
    val salonName: String,
    val queueNumber: String,
    val customerName: String,
    val customerPhone: String,
    val serviceIds: List<String>,
    val serviceNames: List<String>,
    val totalPriceRupiah: Long,
    val totalDurationMinutes: Int,
    val stylistId: String?, // null berarti Kapster Tersedia / Tercepat
    val stylistName: String,
    val status: QueueStatus,
    val qrToken: String,
    val qrNonce: String,
    val qrVerificationStatus: QrVerificationStatus,
    val createdAtMillis: Long,
    val estimatedServiceMillis: Long,
    val calledAtMillis: Long? = null,
    val startedAtMillis: Long? = null,
    val completedAtMillis: Long? = null,
    val isWalkIn: Boolean = false
)

data class InAppNotification(
    val id: String,
    val title: String,
    val message: String,
    val channelType: NotificationChannelType,
    val whatsappPreviewText: String? = null,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class QrScanValidationResult(
    val isSuccess: Boolean,
    val verificationStatus: QrVerificationStatus,
    val message: String,
    val ticket: QueueTicket? = null
)

data class DailyReportSummary(
    val totalAntreanHariIni: Int,
    val selesaiDilayani: Int,
    val sedangMenunggu: Int,
    val dibatalkanAtauLewat: Int,
    val estimasiPendapatanHariIni: Long,
    val rataRataDurasiMenit: Int,
    val layananTerpopuler: String
)

data class MonthlyReportEntry(
    val monthLabel: String,
    val totalPelanggan: Int,
    val pendapatanRupiah: Long
)

fun defaultOperatingSchedule(): List<OperatingDayHours> = listOf(
    OperatingDayHours("Senin", true, "09:00", "20:00"),
    OperatingDayHours("Selasa", true, "09:00", "20:00"),
    OperatingDayHours("Rabu", true, "09:00", "20:00"),
    OperatingDayHours("Kamis", true, "09:00", "20:00"),
    OperatingDayHours("Jumat", true, "09:00", "21:00"),
    OperatingDayHours("Sabtu", true, "08:30", "21:00"),
    OperatingDayHours("Minggu", true, "10:00", "19:00")
)

object DummySalonData {
    val salons = listOf(
        Salon(
            id = "salon_aura",
            name = "Aura Rose Beauty & Hair",
            tagline = "Perawatan Rambut & Kulit Kepala Premium",
            address = "Jl. Senopati No. 48, Kebayoran Baru, Jakarta Selatan",
            phoneWhatsApp = "0812-8890-4411",
            isOpen = true,
            openHoursLabel = "09:00 - 20:00 WIB",
            cancellationPolicy = "Pembatalan gratis sebelum nomor dipanggil ke kursi layanan.",
            queuePrefix = "A",
            rating = 4.9,
            imageRes = R.drawable.img_salon_hero
        ),
        Salon(
            id = "salon_velvet",
            name = "Velvet Atelier Hair Studio",
            tagline = "Coloring, Balayage & Hair Styling Minimalis",
            address = "Jl. Kemang Raya No. 19, Jakarta Selatan",
            phoneWhatsApp = "0813-7721-9030",
            isOpen = true,
            openHoursLabel = "10:00 - 21:00 WIB",
            cancellationPolicy = "Maksimal pembatalan 10 menit sebelum estimasi panggilan.",
            queuePrefix = "V",
            rating = 4.8,
            imageRes = R.drawable.img_salon_studio
        ),
        Salon(
            id = "salon_serene",
            name = "Serene Botanica Hair Spa",
            tagline = "Organic Creambath, Facial & Blowout",
            address = "Jl. Dharmawangsa III No. 12, Jakarta Selatan",
            phoneWhatsApp = "0811-9450-2208",
            isOpen = false,
            openHoursLabel = "10:00 - 19:00 WIB (Sedang Tutup)",
            cancellationPolicy = "Pembatalan diperbolehkan selama status masih Menunggu.",
            queuePrefix = "S",
            rating = 4.7,
            imageRes = R.drawable.img_salon_glow
        )
    )

    val services = listOf(
        SalonService("srv_1", "salon_aura", "Signature Haircut & Blow", "Gunting & Styling", "Potong rambut presisi sesuai bentuk wajah plus cuci & blow natural.", 165000, 45),
        SalonService("srv_2", "salon_aura", "Rose Gold Hair Spa", "Perawatan Rambut", "Nutrisi mendalam dengan ekstrak mawar & pijat relaksasi bahu.", 220000, 60),
        SalonService("srv_3", "salon_aura", "Keratin Silk Smoothing", "Tekstur & Lurus", "Melembutkan rambut mengembang hingga 4 bulan tanpa kaku.", 680000, 120),
        SalonService("srv_4", "salon_aura", "Organic Scalp Detox", "Kulit Kepala", "Detoksifikasi folikel rambut untuk mengurangi rontok & ketombe.", 195000, 40),
        SalonService("srv_5", "salon_aura", "Glam Styling & Curling", "Gunting & Styling", "Penataan rambut acara spesial agar tahan lama & bervolume.", 150000, 35),
        SalonService("srv_6", "salon_velvet", "Precision Trim & Wash", "Gunting & Styling", "Rapikan ujung rambut plus vitamin pelindung panas.", 140000, 40),
        SalonService("srv_7", "salon_velvet", "Soft Balayage Coloring", "Pewarnaan", "Gradasi warna lembut dengan cat rambut bebas amonia.", 750000, 150),
        SalonService("srv_8", "salon_velvet", "Creambath Alpukat Organik", "Perawatan Rambut", "Masker alpukat segar & uap hangat untuk kilau alami.", 135000, 50),
        SalonService("srv_9", "salon_serene", "Botanical Aromatherapy Spa", "Perawatan Rambut", "Terapi rambut minyak esensial lavender & chamomile.", 210000, 55),
        SalonService("srv_10", "salon_serene", "Express Blow & Styling", "Gunting & Styling", "Cuci bersih dan blow dry cepat untuk aktivitas harian.", 95000, 30)
    )

    val stylists = listOf(
        Hairstylist("sty_1", "salon_aura", "Nadia Putri", "Master Stylist & Layering", 7, true, "Kursi 1"),
        Hairstylist("sty_2", "salon_aura", "Raka Pratama", "Color & Keratin Specialist", 5, true, "Kursi 2"),
        Hairstylist("sty_3", "salon_aura", "Clarissa Tan", "Hair Spa & Blowout", 4, true, "Kursi 3"),
        Hairstylist("sty_4", "salon_velvet", "Maya Wijaya", "Balayage & Fashion Color", 6, true, "Kursi 1"),
        Hairstylist("sty_5", "salon_velvet", "Dimas Kusuma", "Signature Cut & Styling", 5, true, "Kursi 2"),
        Hairstylist("sty_6", "salon_serene", "Sinta Dewi", "Holistic Scalp Therapist", 8, false, "Ruang Spa 1")
    )

    val monthlyReports = listOf(
        MonthlyReportEntry("Jul", 312, 64200000),
        MonthlyReportEntry("Agu", 348, 71800000),
        MonthlyReportEntry("Sep", 330, 68900000),
        MonthlyReportEntry("Okt", 376, 79400000),
        MonthlyReportEntry("Nov", 405, 86250000)
    )
}

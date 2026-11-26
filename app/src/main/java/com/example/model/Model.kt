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
            id = "salon_casy",
            name = "Casy Salon HM Yamin",
            tagline = "Hair Styling, Coloring & Perawatan Rambut Profesional",
            address = "Jl. Prof. H. M. Yamin No.225 E, Sei Kera Hilir I, Kec. Medan Perjuangan, Kota Medan, Sumatera Utara 20222",
            phoneWhatsApp = "0812-6500-2022",
            isOpen = true,
            openHoursLabel = "09:00 - 20:30 WIB",
            cancellationPolicy = "Pembatalan gratis sebelum nomor dipanggil ke kursi layanan.",
            queuePrefix = "C",
            rating = 4.9,
            imageRes = R.drawable.img_salon_hero
        ),
        Salon(
            id = "salon_ln",
            name = "LN Beauty Salon Halat",
            tagline = "LN beauty • Perawatan Rambut, Facial & Kecantikan",
            address = "Jl. Halat, Kota Matsum IV, Kec. Medan Area, Kota Medan, Sumatera Utara 20216",
            phoneWhatsApp = "0813-7721-2021",
            isOpen = true,
            openHoursLabel = "09:30 - 20:30 WIB",
            cancellationPolicy = "Maksimal pembatalan 10 menit sebelum estimasi panggilan.",
            queuePrefix = "L",
            rating = 4.9,
            imageRes = R.drawable.img_salon_studio
        ),
        Salon(
            id = "salon_apin",
            name = "Apin Salon",
            tagline = "Potong Rambut Presisi, Creambath & Smoothing",
            address = "Jl. Sena No.50, Perintis, Kec. Medan Tim., Kota Medan, Sumatera Utara 20239",
            phoneWhatsApp = "0812-6044-2023",
            isOpen = true,
            openHoursLabel = "09:00 - 20:00 WIB",
            cancellationPolicy = "Pembatalan gratis sebelum nomor dipanggil ke kursi layanan.",
            queuePrefix = "A",
            rating = 4.8,
            imageRes = R.drawable.img_salon_glow
        ),
        Salon(
            id = "salon_farida",
            name = "Farida Beauty Salon & Spa",
            tagline = "Beauty Salon, Hair Spa & Body Treatment",
            address = "Jl. Tempuling No.163, Sidorejo Hilir, Kec. Medan Tembung, Kota Medan, Sumatera Utara 20222",
            phoneWhatsApp = "0813-9655-2022",
            isOpen = true,
            openHoursLabel = "09:00 - 21:00 WIB",
            cancellationPolicy = "Pembatalan diperbolehkan selama status masih Menunggu.",
            queuePrefix = "F",
            rating = 4.9,
            imageRes = R.drawable.img_salon_hero
        ),
        Salon(
            id = "salon_kendy",
            name = "KENDY SALON",
            tagline = "Potong Rambut, Coloring & Perawatan Rambut Profesional",
            address = "Jl. Platina Raya No.88c, Rengas Pulau, Kec. Medan Marelan, Kota Medan, Sumatera Utara 20255",
            phoneWhatsApp = "0812-6500-8888",
            isOpen = true,
            openHoursLabel = "09:00 - 21:00 WIB",
            cancellationPolicy = "Pembatalan gratis sebelum nomor dipanggil ke kursi layanan.",
            queuePrefix = "K",
            rating = 4.9,
            imageRes = R.drawable.img_salon_studio
        )
    )

    val services = listOf(
        // Casy Salon HM Yamin
        SalonService("srv_c1", "salon_casy", "Signature Haircut & Blow", "Gunting & Styling", "Potong rambut presisi sesuai bentuk wajah plus cuci & blow natural.", 95000, 40),
        SalonService("srv_c2", "salon_casy", "Keratin Silk Smoothing", "Tekstur & Lurus", "Pelurusan lembut berkilau tahan lama tanpa membuat rambut kaku.", 480000, 110),
        SalonService("srv_c3", "salon_casy", "Hair Coloring & Balayage", "Pewarnaan", "Pewarnaan gradasi modern dengan serum pelindung kutikula rambut.", 420000, 120),
        SalonService("srv_c4", "salon_casy", "Aromatic Creambath Spa", "Perawatan Rambut", "Creambath nutrisi mendalam lengkap dengan pijat relaksasi bahu.", 125000, 50),

        // LN Beauty Salon Halat
        SalonService("srv_l1", "salon_ln", "LN Glow Facial & Detox", "Perawatan Wajah", "Pembersihan komedo lembut, masker pencerah, dan pijat wajah relaksasi.", 185000, 60),
        SalonService("srv_l2", "salon_ln", "Korean Layer Cut & Styling", "Gunting & Styling", "Potongan layer ringan modern lengkap dengan cuci dan blow bervolume.", 110000, 45),
        SalonService("srv_l3", "salon_ln", "Silk Protein Hair Mask", "Perawatan Rambut", "Perawatan batang rambut kering agar kembali lembut dan mudah diatur.", 140000, 45),
        SalonService("srv_l4", "salon_ln", "Glossy Fashion Coloring", "Pewarnaan", "Pewarnaan rambut bebas amonia dengan kilau tahan lama.", 350000, 90),

        // Apin Salon
        SalonService("srv_a1", "salon_apin", "Potong Rambut & Cuci Blow", "Gunting & Styling", "Potong rambut rapi untuk tampilan harian plus cuci bersih dan tonic.", 75000, 35),
        SalonService("srv_a2", "salon_apin", "Creambath Ginseng & Kemiri", "Perawatan Rambut", "Menguatkan akar rambut dan mengurangi rontok dengan pijat kepala.", 105000, 45),
        SalonService("srv_a3", "salon_apin", "Smoothing Natural Shine", "Tekstur & Lurus", "Membuat rambut halus lurus alami dan bebas mengembang.", 390000, 100),
        SalonService("srv_a4", "salon_apin", "Styling & Catok Pesta", "Gunting & Styling", "Penataan rambut acara spesial agar bervolume dan tahan lama.", 120000, 40),

        // Farida Beauty Salon & Spa
        SalonService("srv_f1", "salon_farida", "Royal Hair Spa & Lulur", "Perawatan Rambut", "Paket relaksasi hair spa aromaterapi plus lulur badan menyegarkan.", 245000, 75),
        SalonService("srv_f2", "salon_farida", "Facial Brightening Spa", "Perawatan Wajah", "Perawatan wajah menyeluruh untuk kulit cerah, lembap, dan segar.", 175000, 55),
        SalonService("srv_f3", "salon_farida", "Potong Rambut & Hair Mask", "Gunting & Styling", "Potong rambut rapi dilanjutkan masker vitamin untuk rambut sehat.", 130000, 50),
        SalonService("srv_f4", "salon_farida", "Totok Wajah & Relaxing Massage", "Perawatan Wajah", "Relaksasi otot wajah dan bahu untuk melancarkan sirkulasi.", 150000, 45),

        // KENDY SALON
        SalonService("srv_k1", "salon_kendy", "Potong Rambut & Styling", "Gunting & Styling", "Potong rambut rapi modern plus cuci bersih dan hair tonic.", 85000, 35),
        SalonService("srv_k2", "salon_kendy", "Hair Coloring & Highlight", "Pewarnaan", "Pewarnaan rambut tahan lama dengan serum pelindung warna.", 320000, 90),
        SalonService("srv_k3", "salon_kendy", "Creambath & Hair Mask", "Perawatan Rambut", "Perawatan intensif rambut kering dengan pijat relaksasi.", 115000, 45),
        SalonService("srv_k4", "salon_kendy", "Smoothing Keratin Premium", "Tekstur & Lurus", "Pelurusan alami lembut berkilau dan bebas kusut.", 450000, 110)
    )

    val stylists = listOf(
        // Casy Salon HM Yamin
        Hairstylist("sty_c1", "salon_casy", "Casy Putri", "Master Stylist & Layering", 8, true, "Kursi 1"),
        Hairstylist("sty_c2", "salon_casy", "Raka Pratama", "Color & Keratin Specialist", 5, true, "Kursi 2"),
        Hairstylist("sty_c3", "salon_casy", "Nadia Siregar", "Hair Spa & Blowout", 4, true, "Kursi 3"),

        // LN Beauty Salon Halat
        Hairstylist("sty_l1", "salon_ln", "Lina Tanjung", "Facial & Beauty Therapist", 7, true, "Ruang Beauty 1"),
        Hairstylist("sty_l2", "salon_ln", "Novi Harahap", "Korean Cut & Coloring", 5, true, "Kursi 1"),

        // Apin Salon
        Hairstylist("sty_a1", "salon_apin", "Apin Wijaya", "Precision Cut & Styling", 9, true, "Kursi 1"),
        Hairstylist("sty_a2", "salon_apin", "Dinda Lubis", "Creambath & Smoothing", 5, true, "Kursi 2"),

        // Farida Beauty Salon & Spa
        Hairstylist("sty_f1", "salon_farida", "Farida Nasution", "Holistic Spa & Facial", 10, true, "Ruang Spa 1"),
        Hairstylist("sty_f2", "salon_farida", "Sari Batubara", "Hair Care & Styling", 6, true, "Kursi 1"),

        // KENDY SALON
        Hairstylist("sty_k1", "salon_kendy", "Kendy Wijaya", "Master Cut & Coloring", 8, true, "Kursi 1"),
        Hairstylist("sty_k2", "salon_kendy", "Bella Tarigan", "Smoothing & Hair Care", 5, true, "Kursi 2")
    )

    val monthlyReports = listOf(
        MonthlyReportEntry("Jul", 312, 64200000),
        MonthlyReportEntry("Agu", 348, 71800000),
        MonthlyReportEntry("Sep", 330, 68900000),
        MonthlyReportEntry("Okt", 376, 79400000),
        MonthlyReportEntry("Nov", 405, 86250000)
    )
}

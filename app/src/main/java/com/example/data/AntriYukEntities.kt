package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.R
import com.example.model.Hairstylist
import com.example.model.OperatingDayHours
import com.example.model.QrVerificationStatus
import com.example.model.QueueStatus
import com.example.model.QueueTicket
import com.example.model.Salon
import com.example.model.SalonService

@Entity(tableName = "salons")
data class SalonEntity(
    @PrimaryKey val id: String,
    val name: String,
    val tagline: String,
    val address: String,
    val phoneWhatsApp: String,
    val isOpen: Boolean,
    val openHoursLabel: String,
    val cancellationPolicy: String,
    val queuePrefix: String,
    val rating: Double,
    val imageKey: String,
    val scheduleSerialized: String
)

@Entity(tableName = "salon_services")
data class SalonServiceEntity(
    @PrimaryKey val id: String,
    val salonId: String,
    val name: String,
    val category: String,
    val description: String,
    val priceRupiah: Long,
    val durationMinutes: Int,
    val isActive: Boolean
)

@Entity(tableName = "hairstylists")
data class HairstylistEntity(
    @PrimaryKey val id: String,
    val salonId: String,
    val name: String,
    val specialty: String,
    val experienceYears: Int,
    val isAvailable: Boolean,
    val chairNumber: String
)

@Entity(tableName = "queue_tickets")
data class QueueTicketEntity(
    @PrimaryKey val id: String,
    val salonId: String,
    val salonName: String,
    val queueNumber: String,
    val customerName: String,
    val customerPhone: String,
    val serviceIdsCsv: String,
    val serviceNamesJoined: String,
    val totalPriceRupiah: Long,
    val totalDurationMinutes: Int,
    val stylistId: String?,
    val stylistName: String,
    val statusName: String,
    val qrToken: String,
    val qrNonce: String,
    val qrVerificationStatusName: String,
    val createdAtMillis: Long,
    val estimatedServiceMillis: Long,
    val calledAtMillis: Long?,
    val startedAtMillis: Long?,
    val completedAtMillis: Long?,
    val isWalkIn: Boolean
)

fun serializeSchedule(list: List<OperatingDayHours>): String =
    list.joinToString(";;") { "${it.dayName}|${it.isOpen}|${it.openTime}|${it.closeTime}" }

fun deserializeSchedule(raw: String): List<OperatingDayHours> {
    if (raw.isBlank()) return com.example.model.defaultOperatingSchedule()
    return raw.split(";;").mapNotNull { entry ->
        val p = entry.split("|")
        if (p.size == 4) {
            OperatingDayHours(p[0], p[1].toBooleanStrictOrNull() ?: true, p[2], p[3])
        } else null
    }
}

fun imageKeyFromRes(res: Int): String = when (res) {
    R.drawable.img_salon_studio -> "studio"
    R.drawable.img_salon_glow -> "glow"
    else -> "hero"
}

fun resFromImageKey(key: String): Int = when (key) {
    "studio" -> R.drawable.img_salon_studio
    "glow" -> R.drawable.img_salon_glow
    else -> R.drawable.img_salon_hero
}

fun SalonEntity.toDomain(): Salon = Salon(
    id = id,
    name = name,
    tagline = tagline,
    address = address,
    phoneWhatsApp = phoneWhatsApp,
    isOpen = isOpen,
    openHoursLabel = openHoursLabel,
    cancellationPolicy = cancellationPolicy,
    queuePrefix = queuePrefix,
    rating = rating,
    imageRes = resFromImageKey(imageKey),
    operatingSchedule = deserializeSchedule(scheduleSerialized)
)

fun Salon.toEntity(): SalonEntity = SalonEntity(
    id = id,
    name = name,
    tagline = tagline,
    address = address,
    phoneWhatsApp = phoneWhatsApp,
    isOpen = isOpen,
    openHoursLabel = openHoursLabel,
    cancellationPolicy = cancellationPolicy,
    queuePrefix = queuePrefix,
    rating = rating,
    imageKey = imageKeyFromRes(imageRes),
    scheduleSerialized = serializeSchedule(operatingSchedule)
)

fun SalonServiceEntity.toDomain(): SalonService = SalonService(
    id = id,
    salonId = salonId,
    name = name,
    category = category,
    description = description,
    priceRupiah = priceRupiah,
    durationMinutes = durationMinutes,
    isActive = isActive
)

fun SalonService.toEntity(): SalonServiceEntity = SalonServiceEntity(
    id = id,
    salonId = salonId,
    name = name,
    category = category,
    description = description,
    priceRupiah = priceRupiah,
    durationMinutes = durationMinutes,
    isActive = isActive
)

fun HairstylistEntity.toDomain(): Hairstylist = Hairstylist(
    id = id,
    salonId = salonId,
    name = name,
    specialty = specialty,
    experienceYears = experienceYears,
    isAvailable = isAvailable,
    chairNumber = chairNumber
)

fun Hairstylist.toEntity(): HairstylistEntity = HairstylistEntity(
    id = id,
    salonId = salonId,
    name = name,
    specialty = specialty,
    experienceYears = experienceYears,
    isAvailable = isAvailable,
    chairNumber = chairNumber
)

fun QueueTicketEntity.toDomain(): QueueTicket = QueueTicket(
    id = id,
    salonId = salonId,
    salonName = salonName,
    queueNumber = queueNumber,
    customerName = customerName,
    customerPhone = customerPhone,
    serviceIds = if (serviceIdsCsv.isBlank()) emptyList() else serviceIdsCsv.split("||"),
    serviceNames = if (serviceNamesJoined.isBlank()) emptyList() else serviceNamesJoined.split("||"),
    totalPriceRupiah = totalPriceRupiah,
    totalDurationMinutes = totalDurationMinutes,
    stylistId = stylistId,
    stylistName = stylistName,
    status = QueueStatus.entries.find { it.name == statusName } ?: QueueStatus.MENUNGGU,
    qrToken = qrToken,
    qrNonce = qrNonce,
    qrVerificationStatus = QrVerificationStatus.entries.find { it.name == qrVerificationStatusName }
        ?: QrVerificationStatus.BELUM_DIPINDAI,
    createdAtMillis = createdAtMillis,
    estimatedServiceMillis = estimatedServiceMillis,
    calledAtMillis = calledAtMillis,
    startedAtMillis = startedAtMillis,
    completedAtMillis = completedAtMillis,
    isWalkIn = isWalkIn
)

fun QueueTicket.toEntity(): QueueTicketEntity = QueueTicketEntity(
    id = id,
    salonId = salonId,
    salonName = salonName,
    queueNumber = queueNumber,
    customerName = customerName,
    customerPhone = customerPhone,
    serviceIdsCsv = serviceIds.joinToString("||"),
    serviceNamesJoined = serviceNames.joinToString("||"),
    totalPriceRupiah = totalPriceRupiah,
    totalDurationMinutes = totalDurationMinutes,
    stylistId = stylistId,
    stylistName = stylistName,
    statusName = status.name,
    qrToken = qrToken,
    qrNonce = qrNonce,
    qrVerificationStatusName = qrVerificationStatus.name,
    createdAtMillis = createdAtMillis,
    estimatedServiceMillis = estimatedServiceMillis,
    calledAtMillis = calledAtMillis,
    startedAtMillis = startedAtMillis,
    completedAtMillis = completedAtMillis,
    isWalkIn = isWalkIn
)

package com.example.data

import com.example.model.DummySalonData
import com.example.model.Hairstylist
import com.example.model.QrVerificationStatus
import com.example.model.QueueStatus
import com.example.model.QueueTicket
import com.example.model.Salon
import com.example.model.SalonService
import com.example.util.QrAndPrinterHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SalonQueueRepository(private val dao: AntriYukDao) {

    val salonsFlow: Flow<List<Salon>> = dao.observeSalons().map { list -> list.map { it.toDomain() } }
    val servicesFlow: Flow<List<SalonService>> = dao.observeServices().map { list -> list.map { it.toDomain() } }
    val stylistsFlow: Flow<List<Hairstylist>> = dao.observeStylists().map { list -> list.map { it.toDomain() } }
    val ticketsFlow: Flow<List<QueueTicket>> = dao.observeTickets().map { list -> list.map { it.toDomain() } }

    suspend fun ensureSeeded() {
        if (dao.getSalonCount() > 0) return

        dao.upsertSalons(DummySalonData.salons.map { it.toEntity() })
        dao.upsertServices(DummySalonData.services.map { it.toEntity() })
        dao.upsertStylists(DummySalonData.stylists.map { it.toEntity() })

        val now = System.currentTimeMillis()
        val minuteMs = 60_000L

        fun seedTicket(
            id: String,
            salon: Salon,
            queueNum: String,
            customer: String,
            phone: String,
            services: List<SalonService>,
            stylistId: String?,
            stylistName: String,
            status: QueueStatus,
            qrStatus: QrVerificationStatus,
            createdOffsetMin: Long,
            estOffsetMin: Long,
            calledOffsetMin: Long? = null,
            startedOffsetMin: Long? = null,
            completedOffsetMin: Long? = null,
            isWalkIn: Boolean = false
        ): QueueTicket {
            val nonce = QrAndPrinterHelper.generateSecureNonce()
            val token = QrAndPrinterHelper.buildSignedQrToken(id, queueNum, nonce)
            return QueueTicket(
                id = id,
                salonId = salon.id,
                salonName = salon.name,
                queueNumber = queueNum,
                customerName = customer,
                customerPhone = phone,
                serviceIds = services.map { it.id },
                serviceNames = services.map { it.name },
                totalPriceRupiah = services.sumOf { it.priceRupiah },
                totalDurationMinutes = services.sumOf { it.durationMinutes },
                stylistId = stylistId,
                stylistName = stylistName,
                status = status,
                qrToken = token,
                qrNonce = nonce,
                qrVerificationStatus = qrStatus,
                createdAtMillis = now + createdOffsetMin * minuteMs,
                estimatedServiceMillis = now + estOffsetMin * minuteMs,
                calledAtMillis = calledOffsetMin?.let { now + it * minuteMs },
                startedAtMillis = startedOffsetMin?.let { now + it * minuteMs },
                completedAtMillis = completedOffsetMin?.let { now + it * minuteMs },
                isWalkIn = isWalkIn
            )
        }

        val aura = DummySalonData.salons[0]
        val velvet = DummySalonData.salons[1]
        val srv1 = DummySalonData.services[0]
        val srv2 = DummySalonData.services[1]
        val srv3 = DummySalonData.services[2]
        val srv4 = DummySalonData.services[3]
        val srv5 = DummySalonData.services[4]
        val srv6 = DummySalonData.services[5]
        val srv7 = DummySalonData.services[6]

        val initialTickets = listOf(
            seedTicket(
                id = "tkt_001",
                salon = aura,
                queueNum = "A-001",
                customer = "Citra Maharani",
                phone = "0812-3344-1101",
                services = listOf(srv1),
                stylistId = "sty_1",
                stylistName = "Nadia Putri",
                status = QueueStatus.SELESAI,
                qrStatus = QrVerificationStatus.SELESAI,
                createdOffsetMin = -110,
                estOffsetMin = -100,
                calledOffsetMin = -100,
                startedOffsetMin = -98,
                completedOffsetMin = -55
            ),
            seedTicket(
                id = "tkt_002",
                salon = aura,
                queueNum = "A-002",
                customer = "Putri Anggraini",
                phone = "0813-5566-2202",
                services = listOf(srv2, srv5),
                stylistId = "sty_3",
                stylistName = "Clarissa Tan",
                status = QueueStatus.SELESAI,
                qrStatus = QrVerificationStatus.SELESAI,
                createdOffsetMin = -90,
                estOffsetMin = -75,
                calledOffsetMin = -75,
                startedOffsetMin = -72,
                completedOffsetMin = -18,
                isWalkIn = true
            ),
            seedTicket(
                id = "tkt_003",
                salon = aura,
                queueNum = "A-003",
                customer = "Nabila Azzahra",
                phone = "0811-7788-3303",
                services = listOf(srv3),
                stylistId = "sty_2",
                stylistName = "Raka Pratama",
                status = QueueStatus.SEDANG_DILAYANI,
                qrStatus = QrVerificationStatus.SUDAH_DIVERIFIKASI,
                createdOffsetMin = -45,
                estOffsetMin = -20,
                calledOffsetMin = -20,
                startedOffsetMin = -17
            ),
            seedTicket(
                id = "tkt_004",
                salon = aura,
                queueNum = "A-004",
                customer = "Larasati Wijaya",
                phone = "0819-2211-4404",
                services = listOf(srv1, srv4),
                stylistId = "sty_1",
                stylistName = "Nadia Putri",
                status = QueueStatus.DIPANGGIL,
                qrStatus = QrVerificationStatus.BELUM_DIPINDAI,
                createdOffsetMin = -25,
                estOffsetMin = 0,
                calledOffsetMin = -1
            ),
            seedTicket(
                id = "tkt_005",
                salon = aura,
                queueNum = "A-005",
                customer = "Sekar Kinanti",
                phone = "0817-9900-5505",
                services = listOf(srv2),
                stylistId = null,
                stylistName = "Kapster Tersedia / Tercepat",
                status = QueueStatus.HAMPIR_DIPANGGIL,
                qrStatus = QrVerificationStatus.BELUM_DIPINDAI,
                createdOffsetMin = -15,
                estOffsetMin = 15
            ),
            seedTicket(
                id = "tkt_006",
                salon = aura,
                queueNum = "A-006",
                customer = "Kezia Permata",
                phone = "0815-4433-6606",
                services = listOf(srv5),
                stylistId = "sty_3",
                stylistName = "Clarissa Tan",
                status = QueueStatus.MENUNGGU,
                qrStatus = QrVerificationStatus.BELUM_DIPINDAI,
                createdOffsetMin = -6,
                estOffsetMin = 35
            ),
            seedTicket(
                id = "tkt_v01",
                salon = velvet,
                queueNum = "V-001",
                customer = "Sasha Aurelia",
                phone = "0812-1020-3040",
                services = listOf(srv6, srv7),
                stylistId = "sty_4",
                stylistName = "Maya Wijaya",
                status = QueueStatus.SEDANG_DILAYANI,
                qrStatus = QrVerificationStatus.SUDAH_DIVERIFIKASI,
                createdOffsetMin = -40,
                estOffsetMin = -25,
                calledOffsetMin = -25,
                startedOffsetMin = -22
            ),
            seedTicket(
                id = "tkt_v02",
                salon = velvet,
                queueNum = "V-002",
                customer = "Tania Kusuma",
                phone = "0813-4050-6070",
                services = listOf(srv6),
                stylistId = "sty_5",
                stylistName = "Dimas Kusuma",
                status = QueueStatus.MENUNGGU,
                qrStatus = QrVerificationStatus.BELUM_DIPINDAI,
                createdOffsetMin = -10,
                estOffsetMin = 20
            )
        )

        dao.upsertTickets(initialTickets.map { it.toEntity() })
    }

    suspend fun saveSalon(salon: Salon) = dao.upsertSalon(salon.toEntity())
    suspend fun saveService(service: SalonService) = dao.upsertService(service.toEntity())
    suspend fun deleteService(serviceId: String) = dao.deleteServiceById(serviceId)
    suspend fun saveStylist(stylist: Hairstylist) = dao.upsertStylist(stylist.toEntity())
    suspend fun deleteStylist(stylistId: String) = dao.deleteStylistById(stylistId)
    suspend fun saveTicket(ticket: QueueTicket) = dao.upsertTicket(ticket.toEntity())
}

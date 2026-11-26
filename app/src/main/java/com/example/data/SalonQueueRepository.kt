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
        val existingSalons = dao.getSalonsSnapshot()
        val expectedIds = DummySalonData.salons.map { it.id }.toSet()
        val existingIds = existingSalons.map { it.id }.toSet()

        if (existingSalons.isNotEmpty() && existingIds == expectedIds) {
            return
        }

        dao.clearAllTickets()
        dao.clearAllStylists()
        dao.clearAllServices()
        dao.clearAllSalons()

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

        val casy = DummySalonData.salons.first { it.id == "salon_casy" }
        val lnBeauty = DummySalonData.salons.first { it.id == "salon_ln" }
        val apin = DummySalonData.salons.first { it.id == "salon_apin" }
        val farida = DummySalonData.salons.first { it.id == "salon_farida" }
        val kendy = DummySalonData.salons.first { it.id == "salon_kendy" }

        val srvC1 = DummySalonData.services.first { it.id == "srv_c1" }
        val srvC2 = DummySalonData.services.first { it.id == "srv_c2" }
        val srvC4 = DummySalonData.services.first { it.id == "srv_c4" }
        val srvL1 = DummySalonData.services.first { it.id == "srv_l1" }
        val srvL2 = DummySalonData.services.first { it.id == "srv_l2" }
        val srvA1 = DummySalonData.services.first { it.id == "srv_a1" }
        val srvA2 = DummySalonData.services.first { it.id == "srv_a2" }
        val srvF1 = DummySalonData.services.first { it.id == "srv_f1" }
        val srvF2 = DummySalonData.services.first { it.id == "srv_f2" }
        val srvK1 = DummySalonData.services.first { it.id == "srv_k1" }
        val srvK2 = DummySalonData.services.first { it.id == "srv_k2" }

        val initialTickets = listOf(
            // Casy Salon HM Yamin
            seedTicket(
                id = "tkt_c01",
                salon = casy,
                queueNum = "C-001",
                customer = "Citra Maharani",
                phone = "0812-3344-1101",
                services = listOf(srvC1),
                stylistId = "sty_c1",
                stylistName = "Casy Putri",
                status = QueueStatus.SELESAI,
                qrStatus = QrVerificationStatus.SELESAI,
                createdOffsetMin = -95,
                estOffsetMin = -85,
                calledOffsetMin = -85,
                startedOffsetMin = -82,
                completedOffsetMin = -42
            ),
            seedTicket(
                id = "tkt_c02",
                salon = casy,
                queueNum = "C-002",
                customer = "Nabila Azzahra",
                phone = "0811-7788-3303",
                services = listOf(srvC2),
                stylistId = "sty_c2",
                stylistName = "Raka Pratama",
                status = QueueStatus.SEDANG_DILAYANI,
                qrStatus = QrVerificationStatus.SUDAH_DIVERIFIKASI,
                createdOffsetMin = -40,
                estOffsetMin = -18,
                calledOffsetMin = -18,
                startedOffsetMin = -15
            ),
            seedTicket(
                id = "tkt_c03",
                salon = casy,
                queueNum = "C-003",
                customer = "Sekar Kinanti",
                phone = "0817-9900-5505",
                services = listOf(srvC1, srvC4),
                stylistId = null,
                stylistName = "Kapster Tersedia / Tercepat",
                status = QueueStatus.HAMPIR_DIPANGGIL,
                qrStatus = QrVerificationStatus.BELUM_DIPINDAI,
                createdOffsetMin = -16,
                estOffsetMin = 12
            ),
            seedTicket(
                id = "tkt_c04",
                salon = casy,
                queueNum = "C-004",
                customer = "Kezia Permata",
                phone = "0815-4433-6606",
                services = listOf(srvC4),
                stylistId = "sty_c3",
                stylistName = "Nadia Siregar",
                status = QueueStatus.MENUNGGU,
                qrStatus = QrVerificationStatus.BELUM_DIPINDAI,
                createdOffsetMin = -7,
                estOffsetMin = 30
            ),
            // LN Beauty Salon Halat
            seedTicket(
                id = "tkt_l01",
                salon = lnBeauty,
                queueNum = "L-001",
                customer = "Grace Simanjuntak",
                phone = "0812-6410-1122",
                services = listOf(srvL1),
                stylistId = "sty_l1",
                stylistName = "Lina Tanjung",
                status = QueueStatus.DIPANGGIL,
                qrStatus = QrVerificationStatus.BELUM_DIPINDAI,
                createdOffsetMin = -18,
                estOffsetMin = 0,
                calledOffsetMin = -1
            ),
            seedTicket(
                id = "tkt_l02",
                salon = lnBeauty,
                queueNum = "L-002",
                customer = "Natasha Ginting",
                phone = "0813-7210-3344",
                services = listOf(srvL2),
                stylistId = "sty_l2",
                stylistName = "Novi Harahap",
                status = QueueStatus.MENUNGGU,
                qrStatus = QrVerificationStatus.BELUM_DIPINDAI,
                createdOffsetMin = -8,
                estOffsetMin = 18
            ),
            // Apin Salon
            seedTicket(
                id = "tkt_a01",
                salon = apin,
                queueNum = "A-001",
                customer = "Larasati Wijaya",
                phone = "0819-2211-4404",
                services = listOf(srvA1),
                stylistId = "sty_a1",
                stylistName = "Apin Wijaya",
                status = QueueStatus.SEDANG_DILAYANI,
                qrStatus = QrVerificationStatus.SUDAH_DIVERIFIKASI,
                createdOffsetMin = -30,
                estOffsetMin = -15,
                calledOffsetMin = -15,
                startedOffsetMin = -12
            ),
            seedTicket(
                id = "tkt_a02",
                salon = apin,
                queueNum = "A-002",
                customer = "Mawar Sembiring",
                phone = "0811-6300-5566",
                services = listOf(srvA2),
                stylistId = "sty_a2",
                stylistName = "Dinda Lubis",
                status = QueueStatus.MENUNGGU,
                qrStatus = QrVerificationStatus.BELUM_DIPINDAI,
                createdOffsetMin = -6,
                estOffsetMin = 20
            ),
            // Farida Beauty Salon & Spa
            seedTicket(
                id = "tkt_f01",
                salon = farida,
                queueNum = "F-001",
                customer = "Sasha Aurelia",
                phone = "0812-1020-3040",
                services = listOf(srvF1),
                stylistId = "sty_f1",
                stylistName = "Farida Nasution",
                status = QueueStatus.DIPANGGIL,
                qrStatus = QrVerificationStatus.BELUM_DIPINDAI,
                createdOffsetMin = -20,
                estOffsetMin = 0,
                calledOffsetMin = -1
            ),
            seedTicket(
                id = "tkt_f02",
                salon = farida,
                queueNum = "F-002",
                customer = "Tania Kusuma",
                phone = "0813-4050-6070",
                services = listOf(srvF2),
                stylistId = "sty_f2",
                stylistName = "Sari Batubara",
                status = QueueStatus.MENUNGGU,
                qrStatus = QrVerificationStatus.BELUM_DIPINDAI,
                createdOffsetMin = -5,
                estOffsetMin = 22
            ),
            // KENDY SALON
            seedTicket(
                id = "tkt_k01",
                salon = kendy,
                queueNum = "K-001",
                customer = "Rina Siregar",
                phone = "0812-6211-9001",
                services = listOf(srvK1, srvK2),
                stylistId = "sty_k1",
                stylistName = "Kendy Wijaya",
                status = QueueStatus.DIPANGGIL,
                qrStatus = QrVerificationStatus.BELUM_DIPINDAI,
                createdOffsetMin = -20,
                estOffsetMin = 0,
                calledOffsetMin = -1
            ),
            seedTicket(
                id = "tkt_k02",
                salon = kendy,
                queueNum = "K-002",
                customer = "Dina Nasution",
                phone = "0813-7088-4512",
                services = listOf(srvK1),
                stylistId = "sty_k2",
                stylistName = "Bella Tarigan",
                status = QueueStatus.MENUNGGU,
                qrStatus = QrVerificationStatus.BELUM_DIPINDAI,
                createdOffsetMin = -8,
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

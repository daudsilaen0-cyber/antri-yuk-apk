package com.example

import com.example.model.PrinterWidth
import com.example.model.QrVerificationStatus
import com.example.model.QueueStatus
import com.example.model.QueueTicket
import com.example.util.QrAndPrinterHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets

class ExampleUnitTest {

    private fun sampleTicket(
        qrStatus: QrVerificationStatus = QrVerificationStatus.BELUM_DIPINDAI,
        status: QueueStatus = QueueStatus.MENUNGGU
    ): QueueTicket {
        val nonce = QrAndPrinterHelper.generateSecureNonce()
        val token = QrAndPrinterHelper.buildSignedQrToken("tkt_test", "C-007", nonce)
        return QueueTicket(
            id = "tkt_test",
            salonId = "salon_casy",
            salonName = "Casy Salon HM Yamin",
            queueNumber = "C-007",
            customerName = "Anisa Rahmawati",
            customerPhone = "0812-8899-0011",
            serviceIds = listOf("srv_c1", "srv_c4"),
            serviceNames = listOf("Signature Haircut & Blow", "Aromatic Creambath Spa"),
            totalPriceRupiah = 220000,
            totalDurationMinutes = 90,
            stylistId = "sty_c1",
            stylistName = "Casy Putri",
            status = status,
            qrToken = token,
            qrNonce = nonce,
            qrVerificationStatus = qrStatus,
            createdAtMillis = 1_764_000_000_000L,
            estimatedServiceMillis = 1_764_001_800_000L
        )
    }

    @Test
    fun qrToken_validSignaturePassesAndTamperedFails() {
        val ticket = sampleTicket()
        val parsed = QrAndPrinterHelper.verifyTokenSignature(ticket.qrToken)
        assertNotNull(parsed)
        assertEquals("tkt_test", parsed?.ticketId)
        assertEquals("C-007", parsed?.queueNumber)

        val tampered = ticket.qrToken.replace("C-007", "C-001")
        assertNull(QrAndPrinterHelper.verifyTokenSignature(tampered))
    }

    @Test
    fun qrValidation_preventsDuplicateAndCancelledTickets() {
        val validTicket = sampleTicket(QrVerificationStatus.BELUM_DIPINDAI, QueueStatus.DIPANGGIL)
        val validResult = QrAndPrinterHelper.validateScannedQr(validTicket.qrToken, listOf(validTicket))
        assertTrue(validResult.isSuccess)
        assertEquals(QrVerificationStatus.BELUM_DIPINDAI, validResult.verificationStatus)

        val alreadyScanned = validTicket.copy(qrVerificationStatus = QrVerificationStatus.SUDAH_DIVERIFIKASI)
        val duplicateResult = QrAndPrinterHelper.validateScannedQr(alreadyScanned.qrToken, listOf(alreadyScanned))
        assertFalse(duplicateResult.isSuccess)
        assertEquals(QrVerificationStatus.SUDAH_DIVERIFIKASI, duplicateResult.verificationStatus)

        val cancelled = validTicket.copy(
            status = QueueStatus.DIBATALKAN,
            qrVerificationStatus = QrVerificationStatus.DIBATALKAN
        )
        val cancelledResult = QrAndPrinterHelper.validateScannedQr(cancelled.qrToken, listOf(cancelled))
        assertFalse(cancelledResult.isSuccess)
        assertEquals(QrVerificationStatus.DIBATALKAN, cancelledResult.verificationStatus)
    }

    @Test
    fun escPosBuilder_containsRequiredFieldsFor58And80mm() {
        val ticket = sampleTicket()
        val bytes58 = QrAndPrinterHelper.buildEscPosReceiptBytes(ticket, PrinterWidth.THERMAL_58MM)
        val bytes80 = QrAndPrinterHelper.buildEscPosReceiptBytes(ticket, PrinterWidth.THERMAL_80MM)

        val text58 = String(bytes58, StandardCharsets.ISO_8859_1)
        val text80 = String(bytes80, StandardCharsets.ISO_8859_1)

        val normalized58 = text58.replace("\n", " ")
        assertTrue(normalized58.contains("ANTRI YUK"))
        assertTrue(normalized58.contains("Casy Salon HM Yamin"))
        assertTrue(normalized58.contains("C-007"))
        assertTrue(normalized58.contains("Anisa Rahmawati"))
        assertTrue(normalized58.contains("Casy Putri"))
        assertTrue(normalized58.contains("Mohon menunggu hingga nomor antrean Anda dipanggil."))

        assertTrue(text80.contains("ANTRI YUK"))
        assertTrue(text80.contains("C-007"))
        assertTrue(bytes80.size >= bytes58.size)
    }
}

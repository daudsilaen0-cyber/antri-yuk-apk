package com.example.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.print.PrintHelper
import com.example.R
import com.example.model.PrinterWidth
import com.example.model.QrScanValidationResult
import com.example.model.QrVerificationStatus
import com.example.model.QueueStatus
import com.example.model.QueueTicket
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object QrAndPrinterHelper {

    private const val TOKEN_PREFIX = "AY1"
    private const val HMAC_SECRET = "AntriYukSalonSecretKey#2026!RoseGold"
    private val secureRandom = SecureRandom()
    private val indonesiaLocale = Locale("id", "ID")

    data class ParsedQrPayload(
        val ticketId: String,
        val queueNumber: String,
        val nonce: String,
        val signature: String
    )

    fun formatRupiah(amount: Long): String {
        val formatter = NumberFormat.getNumberInstance(indonesiaLocale)
        return "Rp ${formatter.format(amount)}"
    }

    fun formatJam(timeMillis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", indonesiaLocale)
        return "${sdf.format(Date(timeMillis))} WIB"
    }

    fun formatTanggalJam(timeMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", indonesiaLocale)
        return "${sdf.format(Date(timeMillis))} WIB"
    }

    fun generateSecureNonce(): String {
        val bytes = ByteArray(8)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02X".format(it) }
    }

    fun buildSignedQrToken(
        ticketId: String,
        queueNumber: String,
        nonce: String
    ): String {
        val core = "$TOKEN_PREFIX|$ticketId|$queueNumber|$nonce"
        val sig = computeShortHmac(core)
        return "$core|$sig"
    }

    private fun computeShortHmac(data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(HMAC_SECRET.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        mac.init(keySpec)
        val raw = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return raw.take(6).joinToString("") { "%02X".format(it) }
    }

    fun verifyTokenSignature(rawPayload: String): ParsedQrPayload? {
        val parts = rawPayload.trim().split("|")
        if (parts.size != 5 || parts[0] != TOKEN_PREFIX) return null
        val ticketId = parts[1]
        val queueNumber = parts[2]
        val nonce = parts[3]
        val providedSig = parts[4]
        val core = "$TOKEN_PREFIX|$ticketId|$queueNumber|$nonce"
        val expectedSig = computeShortHmac(core)
        val match = MessageDigest.isEqual(
            providedSig.toByteArray(StandardCharsets.UTF_8),
            expectedSig.toByteArray(StandardCharsets.UTF_8)
        )
        return if (match) ParsedQrPayload(ticketId, queueNumber, nonce, providedSig) else null
    }

    fun validateScannedQr(
        rawPayload: String,
        tickets: List<QueueTicket>
    ): QrScanValidationResult {
        val trimmed = rawPayload.trim()
        // Juga dukung pencarian cepat nomor antrean manual jika berformat seperti "A-001" hanya untuk uji simulasi,
        // tetapi tetap memprioritaskan token QR terenkripsi HMAC.
        val parsed = verifyTokenSignature(trimmed)
        if (parsed == null) {
            return QrScanValidationResult(
                isSuccess = false,
                verificationStatus = QrVerificationStatus.TIDAK_VALID,
                message = "QR Code tidak valid atau terdeteksi telah dimodifikasi. Pastikan memindai tiket resmi Antri Yuk."
            )
        }

        val ticket = tickets.find {
            it.id == parsed.ticketId &&
                it.qrNonce == parsed.nonce &&
                it.queueNumber == parsed.queueNumber
        } ?: return QrScanValidationResult(
            isSuccess = false,
            verificationStatus = QrVerificationStatus.TIDAK_VALID,
            message = "Token tiket tidak ditemukan di sistem salon atau bukan tiket aktif."
        )

        return when {
            ticket.status == QueueStatus.DIBATALKAN ||
                ticket.qrVerificationStatus == QrVerificationStatus.DIBATALKAN -> {
                QrScanValidationResult(
                    isSuccess = false,
                    verificationStatus = QrVerificationStatus.DIBATALKAN,
                    message = "Tiket ${ticket.queueNumber} (${ticket.customerName}) sudah dibatalkan.",
                    ticket = ticket
                )
            }

            ticket.status == QueueStatus.SELESAI ||
                ticket.qrVerificationStatus == QrVerificationStatus.SELESAI -> {
                QrScanValidationResult(
                    isSuccess = false,
                    verificationStatus = QrVerificationStatus.SELESAI,
                    message = "Pelayanan untuk tiket ${ticket.queueNumber} telah selesai.",
                    ticket = ticket
                )
            }

            ticket.qrVerificationStatus == QrVerificationStatus.SUDAH_DIVERIFIKASI -> {
                QrScanValidationResult(
                    isSuccess = false,
                    verificationStatus = QrVerificationStatus.SUDAH_DIVERIFIKASI,
                    message = "Tiket ${ticket.queueNumber} sudah pernah diverifikasi sebelumnya (anti-duplikasi aktif).",
                    ticket = ticket
                )
            }

            else -> {
                QrScanValidationResult(
                    isSuccess = true,
                    verificationStatus = QrVerificationStatus.BELUM_DIPINDAI,
                    message = "Tiket ${ticket.queueNumber} atas nama ${ticket.customerName} tervalidasi! Siap check-in.",
                    ticket = ticket
                )
            }
        }
    }

    fun generateQrBitmap(
        content: String,
        sizePx: Int = 512,
        foregroundColor: Int = 0xFF232528.toInt(),
        backgroundColor: Int = 0xFFFFFFFF.toInt()
    ): Bitmap {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val pixels = IntArray(sizePx * sizePx)
        for (y in 0 until sizePx) {
            val offset = y * sizePx
            for (x in 0 until sizePx) {
                pixels[offset + x] = if (matrix.get(x, y)) foregroundColor else backgroundColor
            }
        }
        return Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
        }
    }

    /**
     * Membangun byte array ESC/POS siap kirim ke printer thermal Bluetooth 58 mm atau 80 mm.
     */
    fun buildEscPosReceiptBytes(
        ticket: QueueTicket,
        width: PrinterWidth
    ): ByteArray {
        val out = ByteArrayOutputStream()
        val lineWidth = width.charsPerLine
        val divider = "-".repeat(lineWidth)
        val doubleDivider = "=".repeat(lineWidth)

        fun writeBytes(vararg bytes: Int) {
            bytes.forEach { out.write(it) }
        }

        fun writeText(text: String) {
            out.write(text.toByteArray(StandardCharsets.ISO_8859_1))
        }

        fun writeLine(text: String = "") {
            writeText(text)
            writeBytes(0x0A)
        }

        fun alignLeft() = writeBytes(0x1B, 0x61, 0x00)
        fun alignCenter() = writeBytes(0x1B, 0x61, 0x01)
        fun bold(on: Boolean) = writeBytes(0x1B, 0x45, if (on) 0x01 else 0x00)
        fun sizeNormal() = writeBytes(0x1D, 0x21, 0x00)
        fun sizeLarge() = writeBytes(0x1D, 0x21, 0x11)
        fun sizeQueueHuge() = writeBytes(0x1D, 0x21, 0x22)

        // Init printer
        writeBytes(0x1B, 0x40)

        // Logo & Brand Header
        alignCenter()
        bold(true)
        sizeLarge()
        writeLine("ANTRI YUK")
        sizeNormal()
        bold(false)
        writeLine("Salon Kecantikan & Perawatan")
        writeLine(doubleDivider)

        // Nama Salon
        bold(true)
        writeWrappedLines(ticket.salonName, lineWidth).forEach { writeLine(it) }
        bold(false)
        writeLine(divider)

        // Nomor Antrean
        writeLine("NOMOR ANTREAN")
        bold(true)
        sizeQueueHuge()
        writeLine(ticket.queueNumber)
        sizeNormal()
        bold(false)
        writeLine(divider)

        // Detail Tiket
        alignLeft()
        val labelWidth = if (width == PrinterWidth.THERMAL_58MM) 10 else 14
        fun row(label: String, value: String) {
            val prefix = label.padEnd(labelWidth) + ": "
            val available = (lineWidth - prefix.length).coerceAtLeast(10)
            val wrapped = writeWrappedLines(value, available)
            wrapped.forEachIndexed { idx, line ->
                if (idx == 0) {
                    writeLine(prefix + line)
                } else {
                    writeLine(" ".repeat(prefix.length) + line)
                }
            }
        }

        row("Pelanggan", ticket.customerName)
        row("Layanan", ticket.serviceNames.joinToString(", "))
        row("Hairstylist", ticket.stylistName)
        row("Diambil", formatTanggalJam(ticket.createdAtMillis))
        row("Estimasi", formatJam(ticket.estimatedServiceMillis))
        row("Total", "${formatRupiah(ticket.totalPriceRupiah)} (${ticket.totalDurationMinutes} mnt)")

        writeLine(divider)
        alignCenter()
        writeLine("PINDAI QR VERIFIKASI")

        // Native ESC/POS QR Code (GS ( k)
        val qrBytes = ticket.qrToken.toByteArray(StandardCharsets.UTF_8)
        val storeLen = qrBytes.size + 3
        val pL = storeLen and 0xFF
        val pH = (storeLen shr 8) and 0xFF
        val moduleSize = if (width == PrinterWidth.THERMAL_58MM) 5 else 6

        // Select model 2
        writeBytes(0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00)
        // Set size of module
        writeBytes(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, moduleSize)
        // Error correction level M (49)
        writeBytes(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x31)
        // Store data
        writeBytes(0x1D, 0x28, 0x6B, pL, pH, 0x31, 0x50, 0x30)
        out.write(qrBytes)
        // Print QR
        writeBytes(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30)

        writeLine()
        writeLine("Kode: ${ticket.qrNonce}")
        writeLine(divider)
        writeWrappedLines("Mohon menunggu hingga nomor antrean Anda dipanggil.", lineWidth)
            .forEach { writeLine(it) }
        writeLine(doubleDivider)
        writeBytes(0x0A, 0x0A, 0x0A)

        // Partial cut
        writeBytes(0x1D, 0x56, 0x42, 0x00)
        return out.toByteArray()
    }

    fun formatThermalTextPreview(ticket: QueueTicket, width: PrinterWidth): String {
        val w = width.charsPerLine
        val sep = "-".repeat(w)
        val dbl = "=".repeat(w)
        fun center(text: String): String {
            if (text.length >= w) return text.take(w)
            val pad = (w - text.length) / 2
            return " ".repeat(pad) + text
        }
        return buildString {
            appendLine(center("ANTRI YUK"))
            appendLine(center("Salon Kecantikan Modern"))
            appendLine(dbl)
            writeWrappedLines(ticket.salonName, w).forEach { appendLine(center(it)) }
            appendLine(sep)
            appendLine(center("NOMOR ANTREAN"))
            appendLine(center("[ ${ticket.queueNumber} ]"))
            appendLine(sep)
            appendLine("Pelanggan  : ${ticket.customerName}")
            appendLine("Layanan    : ${ticket.serviceNames.joinToString(", ")}")
            appendLine("Hairstylist: ${ticket.stylistName}")
            appendLine("Diambil    : ${formatTanggalJam(ticket.createdAtMillis)}")
            appendLine("Estimasi   : ${formatJam(ticket.estimatedServiceMillis)}")
            appendLine("Total      : ${formatRupiah(ticket.totalPriceRupiah)}")
            appendLine(sep)
            appendLine(center("[ QR CODE VERIFIKASI ]"))
            appendLine(center("Ref: ${ticket.qrNonce}"))
            appendLine(sep)
            writeWrappedLines("Mohon menunggu hingga nomor antrean Anda dipanggil.", w)
                .forEach { appendLine(center(it)) }
            appendLine(dbl)
        }
    }

    private fun writeWrappedLines(text: String, maxChars: Int): List<String> {
        if (text.isBlank()) return listOf("")
        val words = text.trim().split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            if (current.isEmpty()) {
                current.append(word.take(maxChars))
            } else if (current.length + 1 + word.length <= maxChars) {
                current.append(' ').append(word)
            } else {
                lines.add(current.toString())
                current = StringBuilder(word.take(maxChars))
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }

    /**
     * Merender gambar struk/tiket beresolusi tinggi (58mm atau 80mm) lengkap dengan logo Antri Yuk & QR Code
     * untuk pratinjau, cetak sistem Android/PDF, dan dibagikan sebagai gambar.
     */
    fun renderTicketBitmap(
        ticket: QueueTicket,
        width: PrinterWidth,
        context: Context? = null
    ): Bitmap {
        val canvasWidth = if (width == PrinterWidth.THERMAL_58MM) 580 else 800
        val padding = 36f
        val contentWidth = (canvasWidth - padding * 2).toInt()
        val poppinsBase = context?.let { runCatching { ResourcesCompat.getFont(it, R.font.poppins) }.getOrNull() }

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF232528.toInt()
            textSize = if (width == PrinterWidth.THERMAL_58MM) 30f else 36f
            typeface = if (poppinsBase != null) {
                Typeface.create(poppinsBase, Typeface.BOLD)
            } else {
                Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            textAlign = Paint.Align.CENTER
        }
        val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF6E6864.toInt()
            textSize = if (width == PrinterWidth.THERMAL_58MM) 18f else 21f
            typeface = poppinsBase ?: Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val queueNumberPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF232528.toInt()
            textSize = if (width == PrinterWidth.THERMAL_58MM) 74f else 92f
            typeface = if (poppinsBase != null) {
                Typeface.create(poppinsBase, Typeface.BOLD)
            } else {
                Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            textAlign = Paint.Align.CENTER
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF232528.toInt()
            textSize = if (width == PrinterWidth.THERMAL_58MM) 20f else 23f
            typeface = poppinsBase ?: Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val boldBodyPaint = TextPaint(bodyPaint).apply {
            typeface = if (poppinsBase != null) {
                Typeface.create(poppinsBase, Typeface.BOLD)
            } else {
                Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD9CBC4.toInt()
            strokeWidth = 2f
        }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFC87D72.toInt()
            style = Paint.Style.FILL
        }

        val infoText = buildString {
            appendLine("Pelanggan : ${ticket.customerName}")
            appendLine("Layanan   : ${ticket.serviceNames.joinToString(", ")}")
            appendLine("Hairstylist: ${ticket.stylistName}")
            appendLine("Waktu Ambil: ${formatTanggalJam(ticket.createdAtMillis)}")
            appendLine("Estimasi  : ${formatJam(ticket.estimatedServiceMillis)} (${ticket.totalDurationMinutes} mnt)")
            append("Total Biaya: ${formatRupiah(ticket.totalPriceRupiah)}")
        }

        val infoLayout = StaticLayout.Builder
            .obtain(infoText, 0, infoText.length, bodyPaint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(6f, 1f)
            .build()

        val footerMsg = "Mohon menunggu hingga nomor antrean Anda dipanggil."
        val footerLayout = StaticLayout.Builder
            .obtain(footerMsg, 0, footerMsg.length, subtitlePaint.apply { textAlign = Paint.Align.LEFT }, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(4f, 1f)
            .build()

        subtitlePaint.textAlign = Paint.Align.CENTER

        val qrSize = if (width == PrinterWidth.THERMAL_58MM) 250 else 300
        val totalHeight = (350 + infoLayout.height + qrSize + footerLayout.height + 160)

        val bitmap = Bitmap.createBitmap(canvasWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xFFFAF8F6.toInt())

        // Bingkai elegan Rose Gold tipis
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFC87D72.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRoundRect(RectF(12f, 12f, canvasWidth - 12f, totalHeight - 12f), 24f, 24f, borderPaint)

        var y = 42f
        val centerX = canvasWidth / 2f

        // Logo Antri Yuk dari resource aplikasi
        val logoBitmap = context?.let {
            runCatching {
                BitmapFactory.decodeResource(it.resources, R.drawable.img_app_logo)
            }.getOrNull()
        }
        if (logoBitmap != null) {
            val logoSize = if (width == PrinterWidth.THERMAL_58MM) 88f else 104f
            val dst = RectF(centerX - logoSize / 2f, y, centerX + logoSize / 2f, y + logoSize)
            canvas.drawBitmap(logoBitmap, null, dst, null)
            y += logoSize + 18f
        } else {
            canvas.drawCircle(centerX, y + 26f, 28f, accentPaint)
            val logoLetterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 24f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("AY", centerX, y + 35f, logoLetterPaint)
            y += 82f
        }

        canvas.drawText("ANTRI YUK", centerX, y, titlePaint)
        y += 30f
        canvas.drawText(ticket.salonName, centerX, y, boldBodyPaint.apply { textAlign = Paint.Align.CENTER })
        boldBodyPaint.textAlign = Paint.Align.LEFT
        y += 28f

        canvas.drawLine(padding, y, canvasWidth - padding, y, linePaint)
        y += 36f

        canvas.drawText("NOMOR ANTREAN", centerX, y, subtitlePaint)
        y += 84f
        canvas.drawText(ticket.queueNumber, centerX, y, queueNumberPaint)
        y += 32f

        canvas.drawLine(padding, y, canvasWidth - padding, y, linePaint)
        y += 24f

        canvas.save()
        canvas.translate(padding, y)
        infoLayout.draw(canvas)
        canvas.restore()
        y += infoLayout.height + 24f

        canvas.drawLine(padding, y, canvasWidth - padding, y, linePaint)
        y += 26f

        val qrBitmap = generateQrBitmap(
            content = ticket.qrToken,
            sizePx = qrSize,
            foregroundColor = 0xFF232528.toInt(),
            backgroundColor = 0xFFFFFFFF.toInt()
        )
        val qrLeft = centerX - qrSize / 2f
        canvas.drawBitmap(qrBitmap, qrLeft, y, null)
        y += qrSize + 24f

        canvas.drawText("Kode Tiket: ${ticket.qrNonce}", centerX, y, subtitlePaint)
        y += 26f

        canvas.save()
        canvas.translate(padding, y)
        footerLayout.draw(canvas)
        canvas.restore()

        return bitmap
    }

    fun printTicketDocument(context: Context, ticket: QueueTicket, width: PrinterWidth) {
        val bitmap = renderTicketBitmap(ticket, width, context)
        val printHelper = PrintHelper(context).apply {
            scaleMode = PrintHelper.SCALE_MODE_FIT
        }
        printHelper.printBitmap("Tiket_AntriYuk_${ticket.queueNumber}", bitmap)
    }

    suspend fun shareTicketImage(
        context: Context,
        ticket: QueueTicket,
        width: PrinterWidth
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = renderTicketBitmap(ticket, width, context)
            val dir = File(context.cacheDir, "tickets").apply { mkdirs() }
            val file = File(dir, "Tiket_${ticket.queueNumber}_${width.paperMm}mm.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Tiket Antrean ${ticket.queueNumber} di ${ticket.salonName}. " +
                        "Estimasi dilayani pukul ${formatJam(ticket.estimatedServiceMillis)}."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            withContext(Dispatchers.Main) {
                context.startActivity(
                    Intent.createChooser(intent, "Bagikan / Simpan Tiket Antri Yuk")
                )
            }
        }
    }

    /**
     * Mengirim byte ESC/POS langsung ke printer thermal Bluetooth terpasang (SPP UUID 00001101...).
     * Jika tidak ada printer Bluetooth terhubung, mengembalikan pesan instruksi yang jelas.
     */
    @SuppressLint("MissingPermission")
    suspend fun sendToPairedBluetoothPrinter(
        context: Context,
        ticket: QueueTicket,
        width: PrinterWidth
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter: BluetoothAdapter? = btManager?.adapter
            if (adapter == null) {
                return@runCatching "Perangkat tidak memiliki modul Bluetooth aktif."
            }
            if (!adapter.isEnabled) {
                return@runCatching "Bluetooth belum aktif. Nyalakan Bluetooth untuk mencetak ke printer thermal ${width.paperMm} mm."
            }
            val bonded = adapter.bondedDevices?.firstOrNull()
                ?: return@runCatching "Belum ada printer thermal ${width.paperMm} mm yang dipasangkan (paired). Gunakan fitur Cetak PDF atau Bagikan Tiket."

            val sppUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            val payload = buildEscPosReceiptBytes(ticket, width)
            bonded.createRfcommSocketToServiceRecord(sppUuid).use { socket ->
                socket.connect()
                socket.outputStream.use { os ->
                    os.write(payload)
                    os.flush()
                }
            }
            "Tiket ${ticket.queueNumber} berhasil dicetak ke ${bonded.name} (${width.paperMm} mm)."
        }
    }
}

package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.QrScanValidationResult
import com.example.model.QrVerificationStatus
import com.example.model.QueueTicket
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.AmberWarm
import com.example.ui.theme.CrimsonRose
import com.example.ui.theme.CrimsonSoft
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.EmeraldSage
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.PurePorcelain
import com.example.ui.theme.RoseGoldTerracotta
import com.example.ui.theme.SilkHairline
import com.example.ui.theme.WarmAlabaster
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QrScannerSheet(
    tickets: List<QueueTicket>,
    lastResult: QrScanValidationResult?,
    onScanPayload: (String) -> Unit,
    onClearResult: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var manualCode by remember { mutableStateOf("") }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = null,
                    tint = RoseGoldTerracotta,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "Pemindai QR Code Tiket",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DeepCharcoal
                    )
                    Text(
                        text = "CameraX + ML Kit Barcode Scanner & Uji Simulasi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = WarmAlabaster,
                contentColor = RoseGoldTerracotta
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Kamera Langsung") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Input / Simulasi QR") }
                )
            }

            if (selectedTab == 0) {
                if (hasCameraPermission) {
                    CameraQrPreviewBox(
                        onQrDetected = { raw ->
                            onScanPayload(raw)
                        }
                    )
                    Text(
                        text = "Arahkan kamera ke QR Code tiket pelanggan. Sistem otomatis memvalidasi keaslian token & mencegah scan ganda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarmAlabaster),
                        border = BorderStroke(1.dp, SilkHairline),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = RoseGoldTerracotta,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "Izin Kamera Diperlukan",
                                style = MaterialTheme.typography.titleMedium,
                                color = DeepCharcoal
                            )
                            Text(
                                text = "Izinkan akses kamera untuk memindai QR Code tiket secara langsung, atau gunakan tab Input / Simulasi QR untuk pengujian di emulator.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoseGoldTerracotta)
                                ) {
                                    Text("Izinkan Kamera")
                                }
                                OutlinedButton(onClick = { selectedTab = 1 }) {
                                    Text("Gunakan Simulasi")
                                }
                            }
                        }
                    }
                }
            } else {
                // Mode Input Manual / Simulasi Cepat di Emulator
                OutlinedTextField(
                    value = manualCode,
                    onValueChange = { manualCode = it },
                    label = { Text("Tempel Token QR Tiket") },
                    placeholder = { Text("AY1|tkt_...|A-004|...|SIG") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_qr_input")
                )

                Button(
                    onClick = {
                        if (manualCode.isNotBlank()) onScanPayload(manualCode)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("verify_manual_qr_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseGoldTerracotta)
                ) {
                    Text("Validasi Token QR")
                }

                Text(
                    text = "Pilih Tiket Cepat untuk Uji Validasi & Anti-Duplikasi:",
                    style = MaterialTheme.typography.labelLarge,
                    color = DeepCharcoal
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tickets.take(6).forEach { ticket ->
                        AssistChip(
                            onClick = {
                                manualCode = ticket.qrToken
                                onScanPayload(ticket.qrToken)
                            },
                            label = {
                                Text("${ticket.queueNumber} (${ticket.qrVerificationStatus.label})")
                            }
                        )
                    }
                    AssistChip(
                        onClick = {
                            val fake = "AY1|tkt_fake|A-999|DEADBEEF|INVALID"
                            manualCode = fake
                            onScanPayload(fake)
                        },
                        label = { Text("Uji QR Palsu / Dimodifikasi") }
                    )
                }
            }

            // Hasil Validasi QR Code
            lastResult?.let { result ->
                val (bg, fg, icon) = when {
                    result.isSuccess -> Triple(EmeraldSoft, EmeraldSage, Icons.Filled.CheckCircle)
                    result.verificationStatus == QrVerificationStatus.SUDAH_DIVERIFIKASI ->
                        Triple(AmberSoft, AmberWarm, Icons.Filled.WarningAmber)
                    else -> Triple(CrimsonSoft, CrimsonRose, Icons.Filled.ErrorOutline)
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = bg),
                    border = BorderStroke(1.dp, fg.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qr_validation_result_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(icon, contentDescription = null, tint = fg)
                            Text(
                                text = if (result.isSuccess) {
                                    "Verifikasi Berhasil"
                                } else {
                                    "Ditolak: ${result.verificationStatus.label}"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = fg
                            )
                        }
                        Text(
                            text = result.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeepCharcoal
                        )
                        result.ticket?.let { t ->
                            Text(
                                text = "Pelanggan: ${t.customerName} • ${t.serviceNames.joinToString(", ")} • ${t.stylistName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = DeepCharcoal
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(onClick = onClearResult) {
                                Text("Scan Lagi")
                            }
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalGetImage
@Composable
private fun CameraQrPreviewBox(
    onQrDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    var lastDetectedCode by remember { mutableStateOf<String?>(null) }

    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            runCatching {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                analysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                val raw = barcodes.firstOrNull()?.rawValue
                                if (!raw.isNullOrBlank() && raw != lastDetectedCode) {
                                    lastDetectedCode = raw
                                    onQrDetected(raw)
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            }.onFailure { err ->
                Log.w("QrScannerSheet", "CameraX preview unavailable: ${err.message}")
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            runCatching {
                cameraProviderFuture.get().unbindAll()
            }
            scanner.close()
            executor.shutdown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(DeepCharcoal)
            .border(2.dp, RoseGoldTerracotta, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .border(2.dp, PurePorcelain.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
        )
    }
}

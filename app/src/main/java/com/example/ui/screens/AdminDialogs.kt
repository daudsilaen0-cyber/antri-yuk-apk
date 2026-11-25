package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.Hairstylist
import com.example.model.QueueStatus
import com.example.model.QueueTicket
import com.example.model.SalonService
import com.example.ui.theme.BlushMist
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.PurePorcelain
import com.example.ui.theme.RoseGoldTerracotta
import com.example.ui.theme.SilkHairline
import com.example.util.QrAndPrinterHelper
import java.util.UUID

@Composable
fun WalkInDialog(
    services: List<SalonService>,
    stylists: List<Hairstylist>,
    onDismiss: () -> Unit,
    onSubmit: (name: String, phone: String, serviceIds: Set<String>, stylistId: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedServices by remember {
        mutableStateOf(services.firstOrNull()?.let { setOf(it.id) } ?: emptySet())
    }
    var selectedStylistId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Pelanggan Walk-In") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Pelanggan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. WhatsApp (Opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Pilih Layanan:",
                    style = MaterialTheme.typography.labelLarge,
                    color = DeepCharcoal
                )
                services.filter { it.isActive }.forEach { srv ->
                    val checked = srv.id in selectedServices
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val next = selectedServices.toMutableSet()
                                if (checked && next.size > 1) next.remove(srv.id) else next.add(srv.id)
                                selectedServices = next
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = RoseGoldTerracotta)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(srv.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${QrAndPrinterHelper.formatRupiah(srv.priceRupiah)} • ${srv.durationMinutes} mnt",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                Text(
                    text = "Pilih Hairstylist:",
                    style = MaterialTheme.typography.labelLarge,
                    color = DeepCharcoal
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedStylistId = null },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedStylistId == null, onClick = { selectedStylistId = null })
                    Text("Kapster Tersedia / Tercepat", style = MaterialTheme.typography.bodyMedium)
                }
                stylists.filter { it.isAvailable }.forEach { sty ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStylistId = sty.id },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedStylistId == sty.id, onClick = { selectedStylistId = sty.id })
                        Text("${sty.name} (${sty.chairNumber})", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = name.trim().ifBlank { "Tamu Walk-In" }
                    onSubmit(clean, phone.trim(), selectedServices, selectedStylistId)
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoseGoldTerracotta)
            ) {
                Text("Terbitkan Nomor")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun ServiceEditorDialog(
    salonId: String,
    existing: SalonService?,
    onDismiss: () -> Unit,
    onSave: (SalonService) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: "Gunting & Styling") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var priceText by remember { mutableStateOf(existing?.priceRupiah?.toString() ?: "150000") }
    var durationText by remember { mutableStateOf(existing?.durationMinutes?.toString() ?: "45") }
    var isActive by remember { mutableStateOf(existing?.isActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Tambah Layanan Baru" else "Ubah Layanan") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Layanan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategori") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi Singkat") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter(Char::isDigit) },
                        label = { Text("Harga (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it.filter(Char::isDigit) },
                        label = { Text("Durasi (Mnt)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tampilkan di Menu Pelanggan")
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val item = SalonService(
                        id = existing?.id ?: "srv_${UUID.randomUUID().toString().take(6)}",
                        salonId = existing?.salonId ?: salonId,
                        name = name.trim().ifBlank { "Layanan Salon" },
                        category = category.trim().ifBlank { "Perawatan Rambut" },
                        description = description.trim().ifBlank { "Perawatan salon profesional." },
                        priceRupiah = priceText.toLongOrNull() ?: 100000L,
                        durationMinutes = durationText.toIntOrNull()?.coerceAtLeast(10) ?: 45,
                        isActive = isActive
                    )
                    onSave(item)
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoseGoldTerracotta)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun StylistEditorDialog(
    salonId: String,
    existing: Hairstylist?,
    onDismiss: () -> Unit,
    onSave: (Hairstylist) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var specialty by remember { mutableStateOf(existing?.specialty ?: "Stylist & Coloring") }
    var experienceText by remember { mutableStateOf(existing?.experienceYears?.toString() ?: "3") }
    var chair by remember { mutableStateOf(existing?.chairNumber ?: "Kursi 1") }
    var isAvailable by remember { mutableStateOf(existing?.isAvailable ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Tambah Hairstylist" else "Ubah Data Hairstylist") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Keahlian") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = chair,
                        onValueChange = { chair = it },
                        label = { Text("Nomor Kursi") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = experienceText,
                        onValueChange = { experienceText = it.filter(Char::isDigit) },
                        label = { Text("Pengalaman (Thn)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sedang Bertugas / Tersedia")
                    Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val stylist = Hairstylist(
                        id = existing?.id ?: "sty_${UUID.randomUUID().toString().take(6)}",
                        salonId = existing?.salonId ?: salonId,
                        name = name.trim().ifBlank { "Kapster Salon" },
                        specialty = specialty.trim().ifBlank { "Hair Styling" },
                        experienceYears = experienceText.toIntOrNull() ?: 2,
                        isAvailable = isAvailable,
                        chairNumber = chair.trim().ifBlank { "Kursi 1" }
                    )
                    onSave(stylist)
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoseGoldTerracotta)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun ChangeStatusDialog(
    ticket: QueueTicket,
    onDismiss: () -> Unit,
    onSelectStatus: (QueueStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Status ${ticket.queueNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Pelanggan: ${ticket.customerName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                QueueStatus.entries.forEach { status ->
                    val selected = status == ticket.status
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectStatus(status) },
                        color = if (selected) BlushMist else PurePorcelain,
                        border = BorderStroke(1.dp, if (selected) RoseGoldTerracotta else SilkHairline),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = status.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = DeepCharcoal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

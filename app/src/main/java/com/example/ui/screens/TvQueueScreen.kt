package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QueueTicket
import com.example.ui.components.AntriYukBrandLogo
import com.example.ui.components.QueueStatusBadge
import com.example.ui.theme.BlushMist
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.DarkSalonBg
import com.example.ui.theme.DarkSalonOutline
import com.example.ui.theme.DarkSalonSurface
import com.example.ui.theme.DarkSalonSurfaceVariant
import com.example.ui.theme.IvoryText
import com.example.ui.theme.PurePorcelain
import com.example.ui.theme.QueueNumberCardStyle
import com.example.ui.theme.QueueNumberDisplayStyle
import com.example.ui.theme.RoseGoldLight
import com.example.ui.theme.RoseGoldTerracotta
import com.example.viewmodel.AntriYukUiState
import com.example.viewmodel.QueueViewModel

@Composable
fun TvQueueScreen(
    uiState: AntriYukUiState,
    viewModel: QueueViewModel,
    onExitTvMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onExitTvMode() }

    val salon = uiState.selectedSalon
    val calledTicket = uiState.currentlyCalledTicket
    val nextTicket = uiState.nextWaitingTicket
    val waitingList = uiState.waitingTicketsForSelectedSalon

    // Putar suara otomatis saat masuk layar TV jika ada nomor yang sedang dipanggil
    LaunchedEffect(calledTicket?.id, calledTicket?.calledAtMillis) {
        if (calledTicket != null && uiState.lastSpokenAnnouncement == null) {
            viewModel.announceTicketVoice(calledTicket, isRecall = false)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkSalonBg, DarkSalonSurface)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header TV Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AntriYukBrandLogo(size = 42.dp)
                    Column {
                        Text(
                            text = salon?.name ?: "Antri Yuk Salon",
                            style = MaterialTheme.typography.headlineLarge,
                            color = IvoryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "LAYAR ANTREAN REAL-TIME • SUARA BAHASA INDONESIA",
                            style = MaterialTheme.typography.labelMedium,
                            color = RoseGoldLight
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.toggleTtsMute() },
                        modifier = Modifier.testTag("tv_mute_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isTtsMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                            contentDescription = if (uiState.isTtsMuted) "Aktifkan Suara" else "Bisukan Suara",
                            tint = if (uiState.isTtsMuted) RoseGoldLight else ChampagneGold
                        )
                    }

                    OutlinedButton(
                        onClick = onExitTvMode,
                        border = BorderStroke(1.dp, DarkSalonOutline),
                        modifier = Modifier.testTag("tv_exit_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = IvoryText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Keluar TV", color = IvoryText)
                    }
                }
            }

            // Responsive Body: Portrait vs Landscape/Tablet
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val isWide = maxWidth >= 680.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1.25f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            TvCalledHeroCard(
                                calledTicket = calledTicket,
                                modifier = Modifier.weight(1f)
                            )
                            TvNextTicketCard(nextTicket = nextTicket)
                        }

                        TvWaitingQueuePanel(
                            waitingList = waitingList,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TvCalledHeroCard(calledTicket = calledTicket)
                        TvNextTicketCard(nextTicket = nextTicket)
                        TvWaitingQueuePanel(
                            waitingList = waitingList,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Bar Pengumuman Suara Otomatis & Kontrol Demo TV
            Surface(
                color = DarkSalonSurfaceVariant,
                border = BorderStroke(1.dp, DarkSalonOutline),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.RecordVoiceOver,
                            contentDescription = null,
                            tint = RoseGoldLight
                        )
                        Text(
                            text = uiState.lastSpokenAnnouncement
                                ?: if (uiState.isTtsReady) {
                                    "Suara otomatis Bahasa Indonesia (id-ID) siap memanggil antrean."
                                } else {
                                    "Mesin suara sedang bersiap atau menggunakan teks pengumuman visual."
                                },
                            style = MaterialTheme.typography.bodyMedium,
                            color = IvoryText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.callNextQueue() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("tv_call_next_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = RoseGoldTerracotta)
                        ) {
                            Icon(Icons.Filled.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Panggil Antrean Berikutnya")
                        }

                        if (calledTicket != null) {
                            OutlinedButton(
                                onClick = { viewModel.announceTicketVoice(calledTicket, isRecall = true) },
                                border = BorderStroke(1.dp, ChampagneGold),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("tv_repeat_voice_button")
                            ) {
                                Text("Ulangi Suara", color = ChampagneGold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvCalledHeroCard(
    calledTicket: QueueTicket?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RoseGoldTerracotta),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "NOMOR SEDANG DIPANGGIL",
                style = MaterialTheme.typography.labelLarge,
                color = BlushMist,
                letterSpacing = 1.6.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = calledTicket?.queueNumber ?: "---",
                style = QueueNumberDisplayStyle.copy(fontSize = 68.sp, lineHeight = 72.sp),
                color = PurePorcelain
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = calledTicket?.customerName ?: "Menunggu Antrean",
                style = MaterialTheme.typography.headlineMedium,
                color = PurePorcelain
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = DarkSalonBg.copy(alpha = 0.28f),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = calledTicket?.let { "Hairstylist: ${it.stylistName}" }
                        ?: "Belum ada pelanggan di kursi",
                    style = MaterialTheme.typography.titleMedium,
                    color = PurePorcelain,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun TvNextTicketCard(nextTicket: QueueTicket?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSalonSurfaceVariant),
        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.55f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NOMOR BERIKUTNYA",
                    style = MaterialTheme.typography.labelMedium,
                    color = ChampagneGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = nextTicket?.let { "${it.customerName} • ${it.stylistName}" }
                        ?: "Antrean berikutnya masih kosong",
                    style = MaterialTheme.typography.titleMedium,
                    color = IvoryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = nextTicket?.queueNumber ?: "-",
                style = QueueNumberCardStyle.copy(fontSize = 32.sp),
                color = ChampagneGold
            )
        }
    }
}

@Composable
private fun TvWaitingQueuePanel(
    waitingList: List<QueueTicket>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSalonSurfaceVariant),
        border = BorderStroke(1.dp, DarkSalonOutline),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DAFTAR ANTREAN MENUNGGU",
                    style = MaterialTheme.typography.titleMedium,
                    color = IvoryText
                )
                Text(
                    text = "${waitingList.size} antrean",
                    style = MaterialTheme.typography.labelLarge,
                    color = RoseGoldLight
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DarkSalonOutline)
            Spacer(modifier = Modifier.height(10.dp))

            if (waitingList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada antrean menunggu saat ini.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = IvoryText.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(waitingList, key = { it.id }) { ticket ->
                        Surface(
                            color = DarkSalonSurface,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, DarkSalonOutline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = ticket.queueNumber,
                                        style = QueueNumberCardStyle.copy(fontSize = 22.sp),
                                        color = RoseGoldLight
                                    )
                                    Column {
                                        Text(
                                            text = ticket.customerName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = IvoryText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = ticket.stylistName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = IvoryText.copy(alpha = 0.75f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                QueueStatusBadge(status = ticket.status)
                            }
                        }
                    }
                }
            }
        }
    }
}

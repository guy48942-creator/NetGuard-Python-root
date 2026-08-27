package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DeviceDetailModal
import com.example.ui.components.DeviceItemCard
import com.example.ui.components.RootStatusHeader
import com.example.ui.theme.*
import com.example.viewmodel.NetworkViewModel

@Composable
fun ScannerScreen(
    viewModel: NetworkViewModel
) {
    val rootStatus by viewModel.rootStatus.collectAsState()
    val networkInfo by viewModel.networkInfo.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val scanStatusMessage by viewModel.scanStatusMessage.collectAsState()
    val isMonitoring by viewModel.isMonitoring.collectAsState()
    val selectedDevice by viewModel.selectedDevice.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("الكل") } // الكل, راوتر, هاتف, حاسوب, وهمي

    val filteredDevices = remember(devices, searchQuery, selectedFilter) {
        devices.filter { dev ->
            val matchesQuery = dev.ip.contains(searchQuery, ignoreCase = true) ||
                    dev.mac.contains(searchQuery, ignoreCase = true) ||
                    dev.vendor.contains(searchQuery, ignoreCase = true) ||
                    dev.hostname.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "راوتر" -> dev.deviceType.contains("راوتر")
                "هاتف" -> dev.deviceType.contains("هاتف")
                "حاسوب" -> dev.deviceType.contains("حاسوب")
                "وهمي" -> dev.deviceType.contains("وهمي")
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBgDark)
            .padding(horizontal = 14.dp)
            .testTag("scanner_screen_container")
    ) {
        // Header Status
        RootStatusHeader(
            rootStatus = rootStatus,
            networkInfo = networkInfo,
            onRequestRoot = { viewModel.requestRootPermission() },
            onRefresh = { viewModel.refreshRootAndNetworkInfo() }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Scan Controls Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            shape = RoundedCornerShape(28.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder.copy(alpha = 0.3f))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Main ARP Scan Button
                    Button(
                        onClick = { viewModel.startArpNetworkScan() },
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = CyberPurple
                        ),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(52.dp)
                            .testTag("start_arp_scan_button")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = CyberPurple,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "جارٍ الفحص...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Icon(imageVector = Icons.Default.Radar, contentDescription = null, tint = CyberPurple)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "فحص ARP سريع", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Continuous Monitoring Toggle
                    OutlinedButton(
                        onClick = { viewModel.toggleContinuousMonitoring(intervalSeconds = 10) },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isMonitoring) CyberGreen else CyberCardBorder.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isMonitoring) CyberGreen.copy(alpha = 0.15f) else CyberCardDark
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("toggle_monitoring_button")
                    ) {
                        Icon(
                            imageVector = if (isMonitoring) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isMonitoring) CyberGreenGlow else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMonitoring) "مراقبة نشطة" else "مراقبة مستمرة",
                            color = if (isMonitoring) CyberGreenGlow else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Progress Bar & Status Text
                AnimatedVisibility(visible = isScanning || scanStatusMessage.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = scanStatusMessage,
                                fontSize = 12.sp,
                                color = CyberCyanGlow
                            )
                            if (isScanning) {
                                Text(
                                    text = "${(scanProgress * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan
                                )
                            }
                        }
                        if (isScanning) {
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { scanProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp),
                                color = CyberCyan,
                                trackColor = CyberCardBorder,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search & Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث عن IP, MAC, شركة...", fontSize = 13.sp, color = TextMuted) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CyberSurfaceDark,
                    unfocusedContainerColor = CyberSurfaceDark,
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("device_search_input")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Device Count Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎯 الأجهزة المتصلة (${filteredDevices.size}/${devices.size}):",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Device List or Empty State
        if (filteredDevices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiTetheringOff,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (devices.isEmpty()) "لم يتم اكتشاف أجهزة بعد." else "لا توجد نتائج مطابقة للبحث.",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اضغط على 'فحص ARP سريع' لبدء اكتشاف أجهزة الشبكة.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("discovered_devices_list"),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredDevices, key = { it.ip }) { device ->
                    DeviceItemCard(
                        device = device,
                        onClick = { viewModel.selectDevice(device) },
                        onPortScan = { viewModel.scanDevicePorts(device.ip) }
                    )
                }
            }
        }
    }

    // Modal Details Sheet
    DeviceDetailModal(
        device = selectedDevice,
        onDismiss = { viewModel.selectDevice(null) },
        onPortScan = { viewModel.scanDevicePorts(it) },
        onResolveHost = { viewModel.resolveDeviceHostname(it) }
    )
}

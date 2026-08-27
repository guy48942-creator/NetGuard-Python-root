package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiscoveredDevice
import com.example.ui.theme.*

@Composable
fun DeviceItemCard(
    device: DiscoveredDevice,
    onClick: () -> Unit,
    onPortScan: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCardDark),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder.copy(alpha = 0.3f))),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .testTag("device_card_${device.ip.replace(".", "_")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Device Type Icon & Basic Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Type Icon Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                device.deviceType.contains("راوتر") -> CyberCyan.copy(alpha = 0.2f)
                                device.deviceType.contains("هاتف") -> CyberGreen.copy(alpha = 0.2f)
                                device.deviceType.contains("وهمي") -> CyberPurple.copy(alpha = 0.2f)
                                else -> CyberAmber.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            device.deviceType.contains("راوتر") -> Icons.Default.Router
                            device.deviceType.contains("هاتف") -> Icons.Default.Smartphone
                            device.deviceType.contains("وهمي") -> Icons.Default.DeveloperMode
                            device.deviceType.contains("حاسوب") -> Icons.Default.Computer
                            else -> Icons.Default.Devices
                        },
                        contentDescription = device.deviceType,
                        tint = when {
                            device.deviceType.contains("راوتر") -> CyberCyanGlow
                            device.deviceType.contains("هاتف") -> CyberGreenGlow
                            device.deviceType.contains("وهمي") -> CyberPurple
                            else -> CyberAmber
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                // IP, Hostname, MAC & Vendor Text
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = device.ip,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )

                        if (device.latencyMs >= 0) {
                            Surface(
                                color = CyberGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "${device.latencyMs}ms",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = CyberGreenGlow,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "MAC: ${device.mac}",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "الشركة: ${device.vendor}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = CyberAmber
                        )
                        Text(
                            text = "• ${device.deviceType}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    if (device.openPorts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lan,
                                contentDescription = "المنافذ",
                                tint = CyberCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "المنافذ المفتوحة: ${device.openPorts.joinToString(", ")}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyberCyanGlow
                            )
                        }
                    }
                }
            }

            // Quick Actions Button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onPortScan,
                    modifier = Modifier.testTag("scan_ports_button_${device.ip.replace(".", "_")}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "فحص المنافذ",
                        tint = CyberCyan
                    )
                }
            }
        }
    }
}

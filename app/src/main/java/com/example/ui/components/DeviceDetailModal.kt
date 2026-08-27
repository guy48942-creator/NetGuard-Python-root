package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiscoveredDevice
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailModal(
    device: DiscoveredDevice?,
    onDismiss: () -> Unit,
    onPortScan: (String) -> Unit,
    onResolveHost: (String) -> Unit
) {
    if (device == null) return
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CyberSurfaceDark,
        scrimColor = CyberBgDark.copy(alpha = 0.8f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("device_detail_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "تفاصيل الجهاز المكتشف",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = device.ip,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyberCyanGlow
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_detail_modal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = CyberCardBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // Details List
            DetailRow(label = "عنوان IP:", value = device.ip, isCopyable = true)
            DetailRow(label = "عنوان MAC:", value = device.mac, isCopyable = true)
            DetailRow(label = "الشركة المصنعة:", value = device.vendor, isCopyable = false)
            DetailRow(label = "نوع الجهاز:", value = device.deviceType, isCopyable = false)
            DetailRow(label = "اسم المضيف (Hostname):", value = device.hostname, isCopyable = true)
            DetailRow(
                label = "زمن الاستجابة (Latency):",
                value = if (device.latencyMs >= 0) "${device.latencyMs} ms" else "Unknown",
                isCopyable = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Open Ports Card
            Surface(
                color = CyberCardDark,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "المنافذ المفتوحة (Open Ports)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (device.openPorts.isEmpty()) {
                        Text(
                            text = "لم يتم فحص المنافذ بعد، انقر على 'فحص المنافذ' بالأسفل.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    } else {
                        Text(
                            text = device.openPorts.joinToString(", ") { "Port $it" },
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyberGreenGlow
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onPortScan(device.ip) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberPurple),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("scan_ports_modal_button")
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = CyberPurple, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "فحص المنافذ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onResolveHost(device.ip) },
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("resolve_hostname_button")
                ) {
                    Icon(imageVector = Icons.Default.Dns, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "جلب الاسم", color = CyberCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isCopyable: Boolean) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
            if (isCopyable) {
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(label, value)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "تم النسخ: $value", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "نسخ",
                        tint = CyberCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

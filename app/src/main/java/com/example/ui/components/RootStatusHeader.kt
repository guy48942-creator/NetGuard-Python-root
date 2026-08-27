package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NetworkInterfaceInfo
import com.example.model.RootStatusInfo
import com.example.ui.theme.*

@Composable
fun RootStatusHeader(
    rootStatus: RootStatusInfo,
    networkInfo: NetworkInterfaceInfo?,
    onRequestRoot: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
        shape = RoundedCornerShape(24.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder.copy(alpha = 0.4f))),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("root_status_header_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Root Badge & Interface
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Root Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (rootStatus.isRooted) CyberGreen else CyberRed)
                    )
                    Column {
                        Text(
                            text = if (rootStatus.isRooted) "صلاحيات الروت: [ مفعل ومتاح ]" else "صلاحيات الروت: [ غير متاح / مرفوض ]",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (rootStatus.isRooted) CyberGreenGlow else CyberRed
                        )
                        Text(
                            text = if (rootStatus.isRooted) rootStatus.superuserApp else "Raw Sockets يتطلب Root",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Refresh Button
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("refresh_status_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تحديث",
                        tint = CyberCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CyberCardBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Active Network Interface Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "الشبكة",
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "واجهة الشبكة: ${networkInfo?.interfaceName ?: "wlan0"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "IP المحلي: ${networkInfo?.ipAddress ?: "127.0.0.1"} (${networkInfo?.networkCidr ?: "192.168.1.0/24"})",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                if (!rootStatus.isRooted) {
                    Button(
                        onClick = onRequestRoot,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberAmber,
                            contentColor = CyberBgDark
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("request_root_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "طلب الروت",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "طلب الروت (su)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Surface(
                        color = CyberGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberGreen.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Raw Socket Ready",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyberGreenGlow,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

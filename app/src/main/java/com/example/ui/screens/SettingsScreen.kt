package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.NetworkViewModel

@Composable
fun SettingsScreen(
    viewModel: NetworkViewModel
) {
    val rootStatus by viewModel.rootStatus.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBgDark)
            .padding(horizontal = 14.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "⚙️ الإعدادات ومعلومات الروت (Root & Raw Sockets)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "معلومات تقنية ودليل صلاحيات الروت لمكتبة Scapy على أندرويد",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Root Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCardDark),
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder.copy(alpha = 0.3f))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "حالة نظام الروت الحالي:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyanGlow
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "• الوصول لـ Su: ${if (rootStatus.isSuAvailable) "نعم المتاح" else "غير موجود"}", fontSize = 13.sp, color = TextPrimary)
                Text(text = "• تطبيق الروت: ${rootStatus.superuserApp}", fontSize = 13.sp, color = TextPrimary)
                Text(text = "• وضع SELinux: ${rootStatus.selinuxMode}", fontSize = 13.sp, color = TextPrimary)
                Text(text = "• معرف المستخدم (UID): ${rootStatus.uidInfo}", fontSize = 13.sp, color = TextPrimary)

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.requestRootPermission() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberPurple),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = CyberPurple)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "إعادة طلب صلاحيات الروت (su -c id)", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SELinux Permissive Explanation
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCardDark),
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder.copy(alpha = 0.3f))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💡 لماذا تتطلب مكتبة Scapy و ARP صلاحيات الروت؟",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberAmber
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "فحص ARP وإرسال حزم Raw Sockets على أندرويد يتطلب الوصول لمنفذ طبقة الربط (Layer 2 Data Link). يقوم نظام أندرويد بحظر هذه العملية للتطبيقات العادية بدون Root وحماية SELinux.",
                    fontSize = 12.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "لتحويل SELinux إلى Permissive في حالة الرفض:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyanGlow
                )
                Surface(
                    color = CyberBgDark,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = "su -c setenforce 0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreenGlow,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("SELinux Command", "su -c setenforce 0"))
                        Toast.makeText(context, "تم نسخ الأمر!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCardBorder, contentColor = TextPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "نسخ أمر SELinux", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // About Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCardDark),
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder.copy(alpha = 0.3f))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "حول تطبيق NetScan Root:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "الإصدار: 1.0.0 (Root Edition)", fontSize = 12.sp, color = TextSecondary)
                Text(text = "لغة البرمجة: Kotlin + Jetpack Compose & Python Scapy Engine", fontSize = 12.sp, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.PythonScapyRunner
import com.example.viewmodel.NetworkViewModel

@Composable
fun PythonStudioScreen(
    viewModel: NetworkViewModel
) {
    val pythonCode by viewModel.pythonCode.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    val rootStatus by viewModel.rootStatus.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Python Code, 1 = Output Console, 2 = Buildozer APK Guide

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBgDark)
            .padding(horizontal = 14.dp)
            .testTag("python_studio_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🐍 Python Scapy Studio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "محرر ومحاكي تشغيل سكربتات Scapy Raw Sockets",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Surface(
                color = if (rootStatus.isRooted) CyberGreen.copy(alpha = 0.2f) else CyberRed.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (rootStatus.isRooted) "Root: Activated" else "Root: Required",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (rootStatus.isRooted) CyberGreenGlow else CyberRed,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Segmented Tab Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurfaceDark, RoundedCornerShape(20.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("الكود (Code)", "السجل (Console)", "دليل Buildozer")
            tabs.forEachIndexed { index, title ->
                Button(
                    onClick = { selectedTab = index },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == index) CyberCyan else androidx.compose.ui.graphics.Color.Transparent,
                        contentColor = if (selectedTab == index) CyberPurple else TextSecondary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = null,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                // Python Editor Tab
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "محرر الكود (Scapy Script):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyanGlow
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Python Code", pythonCode))
                                Toast.makeText(context, "تم نسخ كود Python!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "نسخ", tint = CyberCyan)
                            }

                            IconButton(onClick = {
                                viewModel.setPythonCode(PythonScapyRunner.DEFAULT_PYTHON_SCAPY_CODE)
                                Toast.makeText(context, "تم إعادة تعيين الكود الأصلي", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(imageVector = Icons.Default.Restore, contentDescription = "استعادة", tint = TextSecondary)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = pythonCode,
                        onValueChange = { viewModel.setPythonCode(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("python_code_editor"),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TextPrimary
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CyberCardDark,
                            unfocusedContainerColor = CyberCardDark,
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberCardBorder
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Execute Button
                    Button(
                        onClick = {
                            selectedTab = 1
                            viewModel.simulateRunPythonScapyScript()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberPurple),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("run_python_script_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "تشغيل السكربت عبر بيئة الروت", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            1 -> {
                // Console Log Tab
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "سجل التنفيذ (Console Output):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberCyanGlow)
                        TextButton(onClick = { viewModel.clearLogs() }) {
                            Text(text = "مسح السجل", color = CyberRed, fontSize = 12.sp)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCardDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            items(terminalLogs) { log ->
                                Text(
                                    text = log,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = when {
                                        log.startsWith("[ERR]") -> CyberRed
                                        log.startsWith("[✅]") -> CyberGreenGlow
                                        log.startsWith("[*]") -> CyberCyanGlow
                                        else -> TextPrimary
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.simulateRunPythonScapyScript() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberBgDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "إعادة التشغيل", fontWeight = FontWeight.Bold)
                    }
                }
            }

            2 -> {
                // Buildozer APK Export Tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "🛠️ بناء وتجميع حزمة APK بنظام الروت (Buildozer):",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyanGlow
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Requirements & Spec Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCardDark),
                        shape = RoundedCornerShape(20.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder.copy(alpha = 0.3f))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📄 إعدادات buildozer.spec (المتطلبات):",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberAmber
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Buildozer Spec", PythonScapyRunner.BUILDOZER_SPEC_TEMPLATE))
                                        Toast.makeText(context, "تم نسخ إعدادات buildozer.spec بنجاح!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "نسخ", tint = CyberCyan)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = CyberBgDark,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = PythonScapyRunner.BUILDOZER_SPEC_TEMPLATE,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Terminal Build Commands Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCardDark),
                        shape = RoundedCornerShape(20.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder.copy(alpha = 0.3f))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚡ أوامر تثبيت الحزم وبناء الـ APK:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberGreenGlow
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Build Commands", PythonScapyRunner.BUILD_COMMANDS_GUIDE))
                                        Toast.makeText(context, "تم نسخ أوامر البناء!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "نسخ", tint = CyberGreen)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = CyberBgDark,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = PythonScapyRunner.BUILD_COMMANDS_GUIDE,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Copy All Button
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val fullGuide = "${PythonScapyRunner.BUILDOZER_SPEC_TEMPLATE}\n\n${PythonScapyRunner.BUILD_COMMANDS_GUIDE}"
                            clipboard.setPrimaryClip(ClipData.newPlainText("Buildozer Guide", fullGuide))
                            Toast.makeText(context, "تم نسخ كافة الإعدادات والأوامر!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberPurple),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = CyberPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "نسخ جميع متطلبات وأوامر البناء الكاملة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

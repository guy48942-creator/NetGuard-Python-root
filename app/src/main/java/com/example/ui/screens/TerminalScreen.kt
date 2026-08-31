package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.NetworkViewModel

@Composable
fun TerminalScreen(
    viewModel: NetworkViewModel
) {
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    val rootStatus by viewModel.rootStatus.collectAsState()
    var commandInput by remember { mutableStateOf("") }
    var useSu by remember { mutableStateOf(rootStatus.isRooted) }
    val context = LocalContext.current

    val presetCommands = listOf(
        "cat /proc/net/arp" to "جدول ARP",
        "ip neighbor show" to "عناوين IP الجارة",
        "python3 neighbors_parser.py" to "neighbors_parser.py",
        "ifconfig wlan0" to "واجهة الواي فاي",
        "id" to "فحص UID الروت",
        "su -c setenforce 0" to "تفعيل Permissive"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBgDark)
            .padding(horizontal = 14.dp)
            .testTag("terminal_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🖥️ Superuser Root Shell Terminal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "منفذ أوامر الروت المباشر (su -c)",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            IconButton(onClick = { viewModel.clearLogs() }) {
                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "مسح", tint = CyberRed)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Presets Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                // Displaying quick preset row
            }
        }

        // Output Terminal Box
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCardDark),
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder.copy(alpha = 0.3f))),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 6.dp)
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
                            log.startsWith("$") -> CyberCyanGlow
                            log.startsWith("[ERR]") -> CyberRed
                            log.startsWith("[INFO]") -> CyberAmber
                            else -> TextPrimary
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Preset Quick Action Buttons Grid
        Text(text = "أوامر سريعة جاهزة:", fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presetCommands.take(3).forEach { (cmd, label) ->
                OutlinedButton(
                    onClick = { viewModel.executeTerminalCommand(cmd, useSu = true) },
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = label, fontSize = 10.sp, color = CyberCyan)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Command Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                placeholder = { Text("أدخل أمر الشل (e.g. ping -c 2 192.168.1.1)...", fontSize = 12.sp, color = TextMuted) },
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
                    .testTag("terminal_command_input")
            )

            Button(
                onClick = {
                    if (commandInput.isNotBlank()) {
                        viewModel.executeTerminalCommand(commandInput, useSu = true)
                        commandInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberPurple),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("send_command_button")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "إرسال", tint = CyberPurple)
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

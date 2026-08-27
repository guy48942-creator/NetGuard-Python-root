package com.example.utils

import com.example.model.RootStatusInfo
import com.example.model.TerminalCommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object RootUtil {

    suspend fun checkRootStatus(): RootStatusInfo = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var isRooted = false
        var isSuAvailable = false
        var superuserApp = "غير متاح (Not Available)"
        var uidInfo = ""
        var selinuxMode = "Unknown"

        // Check if su binary exists in common paths
        val suPaths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su"
        )
        for (path in suPaths) {
            if (File(path).exists()) {
                isSuAvailable = true
                break
            }
        }

        // Try executing su command
        val suResult = executeCommand("su -c id")
        if (suResult.exitCode == 0 && suResult.stdout.contains("uid=0")) {
            isRooted = true
            isSuAvailable = true
            uidInfo = suResult.stdout.trim()
        }

        // Detect Superuser app
        if (isRooted || isSuAvailable) {
            when {
                File("/sbin/su").exists() || File("/data/adb/magisk").exists() -> superuserApp = "Magisk Root"
                File("/data/adb/ksu").exists() -> superuserApp = "KernelSU"
                File("/system/app/Superuser.apk").exists() -> superuserApp = "SuperSU"
                else -> superuserApp = if (isRooted) "Superuser Granted (Root)" else "Su Detected (Pending Grant)"
            }
        }

        // SELinux mode check
        val selinuxRes = executeCommand("getenforce")
        if (selinuxRes.exitCode == 0) {
            selinuxMode = selinuxRes.stdout.trim()
        }

        RootStatusInfo(
            isRooted = isRooted,
            isSuAvailable = isSuAvailable,
            superuserApp = superuserApp,
            uidInfo = if (uidInfo.isNotEmpty()) uidInfo else "Non-root user (uid=${android.os.Process.myUid()})",
            lastCheckTime = startTime,
            rawSocketSupported = isRooted,
            selinuxMode = selinuxMode
        )
    }

    suspend fun executeCommand(cmd: String, useSu: Boolean = false): TerminalCommandResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var process: Process? = null
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        var exitCode = -1

        try {
            val commandArray = if (useSu) {
                arrayOf("su", "-c", cmd)
            } else {
                arrayOf("sh", "-c", cmd)
            }

            process = Runtime.getRuntime().exec(commandArray)

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stdout.append(line).append("\n")
            }

            val errReader = BufferedReader(InputStreamReader(process.errorStream))
            while (errReader.readLine().also { line = it } != null) {
                stderr.append(line).append("\n")
            }

            exitCode = process.waitFor()
        } catch (e: Exception) {
            stderr.append("Execution Exception: ").append(e.localizedMessage ?: "Unknown error")
        } finally {
            process?.destroy()
        }

        val duration = System.currentTimeMillis() - startTime
        TerminalCommandResult(
            command = cmd,
            stdout = stdout.toString().trim(),
            stderr = stderr.toString().trim(),
            exitCode = exitCode,
            durationMs = duration
        )
    }

    /**
     * Parse system ARP table from /proc/net/arp or `ip neighbor` or `arp -a`
     */
    suspend fun readSystemArpTable(): Map<String, String> = withContext(Dispatchers.IO) {
        val arpMap = mutableMapOf<String, String>() // IP -> MAC

        // Attempt 1: Read /proc/net/arp
        try {
            val arpFile = File("/proc/net/arp")
            if (arpFile.exists() && arpFile.canRead()) {
                arpFile.useLines { lines ->
                    lines.drop(1).forEach { line ->
                        val tokens = line.split("\\s+".toRegex()).filter { it.isNotBlank() }
                        if (tokens.size >= 4) {
                            val ip = tokens[0]
                            val flags = tokens[2]
                            val mac = tokens[3]
                            if (mac != "00:00:00:00:00:00" && flags != "0x0") {
                                arpMap[ip] = mac.uppercase()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to shell
        }

        // Attempt 2: Shell `ip neighbor` or `arp` command (especially with root if available)
        if (arpMap.isEmpty()) {
            val cmdRes = executeCommand("ip neighbor show", useSu = true)
            if (cmdRes.exitCode == 0 && cmdRes.stdout.isNotBlank()) {
                cmdRes.stdout.lineSequence().forEach { line ->
                    // format: 192.168.1.1 dev wlan0 lladdr 00:11:22:33:44:55 REACHABLE
                    val tokens = line.split("\\s+".toRegex())
                    val lladdrIndex = tokens.indexOf("lladdr")
                    if (lladdrIndex != -1 && lladdrIndex + 1 < tokens.size) {
                        val ip = tokens[0]
                        val mac = tokens[lladdrIndex + 1]
                        if (mac != "00:00:00:00:00:00") {
                            arpMap[ip] = mac.uppercase()
                        }
                    }
                }
            }
        }

        arpMap
    }
}

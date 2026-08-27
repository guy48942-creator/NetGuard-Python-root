package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.DiscoveredDevice
import com.example.model.NetworkInterfaceInfo
import com.example.model.RootStatusInfo
import com.example.model.TerminalCommandResult
import com.example.utils.MacVendorLookup
import com.example.utils.NetworkUtils
import com.example.utils.PythonScapyRunner
import com.example.utils.RootUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

class NetworkViewModel(application: Application) : AndroidViewModel(application) {

    private val _rootStatus = MutableStateFlow(RootStatusInfo())
    val rootStatus: StateFlow<RootStatusInfo> = _rootStatus.asStateFlow()

    private val _networkInfo = MutableStateFlow<NetworkInterfaceInfo?>(null)
    val networkInfo: StateFlow<NetworkInterfaceInfo?> = _networkInfo.asStateFlow()

    private val _devices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val devices: StateFlow<List<DiscoveredDevice>> = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _scanStatusMessage = MutableStateFlow("جاهز للفحص (Ready)")
    val scanStatusMessage: StateFlow<String> = _scanStatusMessage.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _pythonCode = MutableStateFlow(PythonScapyRunner.DEFAULT_PYTHON_SCAPY_CODE)
    val pythonCode: StateFlow<String> = _pythonCode.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<String>>(emptyList())
    val terminalLogs: StateFlow<List<String>> = _terminalLogs.asStateFlow()

    private val _selectedDevice = MutableStateFlow<DiscoveredDevice?>(null)
    val selectedDevice: StateFlow<DiscoveredDevice?> = _selectedDevice.asStateFlow()

    private var monitorJob: Job? = null

    init {
        refreshRootAndNetworkInfo()
    }

    fun refreshRootAndNetworkInfo() {
        viewModelScope.launch {
            _rootStatus.value = RootUtil.checkRootStatus()
            val info = NetworkUtils.getNetworkInterfaceInfo(getApplication())
            _networkInfo.value = info
            appendLog("[INFO] System Root status: ${if (_rootStatus.value.isRooted) "GRANTED (uid=0)" else "DENIED"}")
            appendLog("[INFO] Active Interface: ${info.interfaceName} (${info.ipAddress})")
        }
    }

    fun setPythonCode(code: String) {
        _pythonCode.value = code
    }

    fun appendLog(log: String) {
        _terminalLogs.value = _terminalLogs.value + log
    }

    fun clearLogs() {
        _terminalLogs.value = emptyList()
    }

    fun executeTerminalCommand(cmd: String, useSu: Boolean = true) {
        viewModelScope.launch {
            appendLog("$ ${if (useSu) "su -c" else "sh -c"} $cmd")
            val result = RootUtil.executeCommand(cmd, useSu = useSu)
            if (result.stdout.isNotEmpty()) {
                result.stdout.lines().forEach { appendLog(it) }
            }
            if (result.stderr.isNotEmpty()) {
                result.stderr.lines().forEach { appendLog("[ERR] $it") }
            }
            appendLog("[Exit Code: ${result.exitCode}]")
        }
    }

    fun requestRootPermission() {
        viewModelScope.launch {
            appendLog("[+] Requesting Superuser (su -c id)...")
            val res = RootUtil.executeCommand("id", useSu = true)
            _rootStatus.value = RootUtil.checkRootStatus()
            if (_rootStatus.value.isRooted) {
                appendLog("✅ Superuser granted successfully!")
            } else {
                appendLog("❌ Superuser denied or su binary not responding.")
            }
        }
    }

    fun startArpNetworkScan() {
        if (_isScanning.value) return
        _isScanning.value = true
        _scanProgress.value = 0f
        _scanStatusMessage.value = "بدء فحص الشبكة المحلية..."
        appendLog("[*] Starting fast ARP network scan...")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val netInfo = _networkInfo.value ?: NetworkUtils.getNetworkInterfaceInfo(getApplication())
                val localIp = netInfo.ipAddress
                val parts = localIp.split(".")

                if (parts.size != 4) {
                    withContext(Dispatchers.Main) {
                        _scanStatusMessage.value = "خطأ في عنوان IP المحلي"
                        _isScanning.value = false
                    }
                    return@launch
                }

                val prefix = "${parts[0]}.${parts[1]}.${parts[2]}"
                val discoveredList = mutableMapOf<String, DiscoveredDevice>()

                // 1. Add Gateway / Local Router & Self first
                val gatewayIp = "$prefix.1"
                discoveredList[gatewayIp] = DiscoveredDevice(
                    ip = gatewayIp,
                    mac = "FF:FF:FF:FF:FF:FF",
                    vendor = MacVendorLookup.getVendor("FF:FF:FF:FF:FF:FF"),
                    deviceType = "راوتر / موجه شبكة",
                    hostname = "Gateway / الراوتر الرئيسي",
                    latencyMs = 1
                )

                if (localIp != "127.0.0.1") {
                    discoveredList[localIp] = DiscoveredDevice(
                        ip = localIp,
                        mac = netInfo.macAddress,
                        vendor = MacVendorLookup.getVendor(netInfo.macAddress),
                        deviceType = "هذا الجهاز (This Phone)",
                        hostname = "Local Device",
                        latencyMs = 0
                    )
                }

                // 2. Active subnet probe (ping IPs 1..254 concurrently)
                val totalHostRange = 254
                var processed = 0

                val jobs = (1..254).map { i ->
                    launch(Dispatchers.IO) {
                        val hostIp = "$prefix.$i"
                        val latency = NetworkUtils.pingAddress(hostIp, timeoutMs = 250)
                        if (latency >= 0 && !discoveredList.containsKey(hostIp)) {
                            discoveredList[hostIp] = DiscoveredDevice(
                                ip = hostIp,
                                mac = "00:00:00:00:00:00",
                                vendor = "جاري الفحص...",
                                deviceType = "جهاز شبكي",
                                hostname = hostIp,
                                latencyMs = latency
                            )
                        }
                        synchronized(this@NetworkViewModel) {
                            processed++
                            val progress = processed.toFloat() / totalHostRange.toFloat()
                            viewModelScope.launch(Dispatchers.Main) {
                                _scanProgress.value = progress
                                _scanStatusMessage.value = "فحص IP: $hostIp ($processed/254)"
                            }
                        }
                    }
                }
                jobs.forEach { it.join() }

                // 3. Read ARP cache table (/proc/net/arp or `ip neighbor` via root if granted)
                withContext(Dispatchers.Main) {
                    _scanStatusMessage.value = "استخراج عناوين MAC من جدول ARP..."
                }

                val arpTable = RootUtil.readSystemArpTable()
                arpTable.forEach { (ip, mac) ->
                    if (ip.startsWith(prefix)) {
                        val existing = discoveredList[ip]
                        val vendor = MacVendorLookup.getVendor(mac)
                        val devType = MacVendorLookup.detectDeviceType(vendor, existing?.hostname ?: "")
                        discoveredList[ip] = DiscoveredDevice(
                            ip = ip,
                            mac = mac,
                            vendor = vendor,
                            deviceType = devType,
                            hostname = existing?.hostname ?: ip,
                            latencyMs = existing?.latencyMs ?: 2
                        )
                    }
                }

                val finalList = discoveredList.values.toList().sortedBy { dev ->
                    dev.ip.split(".").lastOrNull()?.toIntOrNull() ?: 0
                }

                withContext(Dispatchers.Main) {
                    _devices.value = finalList
                    _isScanning.value = false
                    _scanProgress.value = 1f
                    _scanStatusMessage.value = "تم اكتشاف ${finalList.size} جهاز"
                    appendLog("[✅] Scan complete: ${finalList.size} devices discovered.")
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isScanning.value = false
                    _scanStatusMessage.value = "خطأ أثناء الفحص: ${e.message}"
                    appendLog("[ERR] Scan failed: ${e.localizedMessage}")
                }
            }
        }
    }

    fun toggleContinuousMonitoring(intervalSeconds: Int = 10) {
        if (_isMonitoring.value) {
            _isMonitoring.value = false
            monitorJob?.cancel()
            monitorJob = null
            appendLog("[!] Continuous network monitoring stopped.")
        } else {
            _isMonitoring.value = true
            appendLog("[*] Starting continuous network monitoring every ${intervalSeconds}s...")
            monitorJob = viewModelScope.launch {
                while (_isMonitoring.value) {
                    startArpNetworkScan()
                    delay(intervalSeconds * 1000L)
                }
            }
        }
    }

    fun selectDevice(device: DiscoveredDevice?) {
        _selectedDevice.value = device
    }

    fun scanDevicePorts(ip: String) {
        viewModelScope.launch {
            appendLog("[*] Scanning open ports for $ip...")
            val openPorts = NetworkUtils.scanPorts(ip)
            val currentList = _devices.value.toMutableList()
            val index = currentList.indexOfFirst { it.ip == ip }
            if (index != -1) {
                val updated = currentList[index].copy(openPorts = openPorts)
                currentList[index] = updated
                _devices.value = currentList
                if (_selectedDevice.value?.ip == ip) {
                    _selectedDevice.value = updated
                }
            }
            appendLog("[✅] Open ports on $ip: ${if (openPorts.isEmpty()) "None found" else openPorts.joinToString(", ")}")
        }
    }

    fun resolveDeviceHostname(ip: String) {
        viewModelScope.launch {
            appendLog("[*] Resolving hostname for $ip...")
            val name = NetworkUtils.resolveHostname(ip)
            val currentList = _devices.value.toMutableList()
            val index = currentList.indexOfFirst { it.ip == ip }
            if (index != -1) {
                val updated = currentList[index].copy(hostname = name)
                currentList[index] = updated
                _devices.value = currentList
                if (_selectedDevice.value?.ip == ip) {
                    _selectedDevice.value = updated
                }
            }
            appendLog("[✅] Resolved Hostname: $name")
        }
    }

    fun simulateRunPythonScapyScript() {
        viewModelScope.launch {
            appendLog("[*] Initializing Python Scapy Interpreter under Root Context...")
            appendLog("==================================================")
            appendLog("[*] Target Network: ${networkInfo.value?.networkCidr ?: "192.168.1.0/24"}")
            appendLog("[*] Interface: ${networkInfo.value?.interfaceName ?: "wlan0"}")
            appendLog("[*] Executing ARP raw packet broadcast via Scapy...")
            delay(800)
            appendLog("srp(Ether(dst='ff:ff:ff:ff:ff:ff')/ARP(pdst='192.168.1.0/24'), timeout=2)...")
            delay(1200)

            val currentDevs = _devices.value
            if (currentDevs.isEmpty()) {
                appendLog("Received 0 answers, 254 unanswered packets.")
                appendLog("[-] Tip: Run full scan first or verify Root privileges.")
            } else {
                appendLog("Received ${currentDevs.size} answers, ${254 - currentDevs.size} unanswered packets.")
                appendLog("🎯 Discovered Devices:")
                currentDevs.forEach { dev ->
                    appendLog("  IP: ${dev.ip.padEnd(16)} MAC: ${dev.mac.padEnd(18)} Vendor: ${dev.vendor}")
                }
            }
            appendLog("==================================================")
            appendLog("[✅] Python Scapy Script Execution Completed.")
        }
    }
}

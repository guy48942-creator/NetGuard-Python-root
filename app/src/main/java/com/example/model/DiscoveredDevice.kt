package com.example.model

data class DiscoveredDevice(
    val ip: String,
    val mac: String,
    val vendor: String,
    val deviceType: String, // e.g. "Router / راوتر", "Phone / هاتف", "PC / حاسوب", "Virtual Machine / جهاز وهمي", "IoT / جهاز ذكي"
    val hostname: String = "Unknown",
    val latencyMs: Long = -1,
    val openPorts: List<Int> = emptyList(),
    val isOnline: Boolean = true,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)

data class NetworkInterfaceInfo(
    val interfaceName: String,
    val ipAddress: String,
    val macAddress: String,
    val subnetMask: String,
    val networkCidr: String,
    val isConnected: Boolean
)

enum class DeviceTypeArabic(val arabicName: String, val iconType: String) {
    ROUTER("راوتر / موجه شبكة", "router"),
    PHONE("هاتف / جهاز ذكي", "phone"),
    PC("حاسوب / كمبيوتر", "computer"),
    VIRTUAL_MACHINE("جهاز وهمي", "vm"),
    PRINTER("طابعة شبكية", "printer"),
    IOT("جهاز IoT / ذكي", "iot"),
    UNKNOWN("جهاز غير معروف", "unknown")
}

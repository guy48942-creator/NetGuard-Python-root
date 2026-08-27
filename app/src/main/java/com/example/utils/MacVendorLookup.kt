package com.example.utils

object MacVendorLookup {

    private val OUI_DATABASE = mapOf(
        "00:1A:2B" to "Apple",
        "00:1B:63" to "Apple",
        "00:1E:C2" to "Apple",
        "00:23:12" to "Apple",
        "00:25:00" to "Apple",
        "00:26:BB" to "Apple",
        "00:A0:40" to "Apple",
        "3C:5A:B4" to "Google",
        "F4:F5:DB" to "Google",
        "D4:F5:13" to "Google",
        "38:87:D5" to "Samsung",
        "8C:85:90" to "Samsung",
        "50:01:D9" to "Samsung",
        "CC:6E:A4" to "Samsung",
        "F4:F5:D8" to "Huawei",
        "00:1D:92" to "Cisco",
        "00:50:56" to "VMware",
        "00:0C:29" to "VMware",
        "08:00:27" to "VirtualBox",
        "B8:27:EB" to "Raspberry Pi",
        "DC:A6:32" to "Raspberry Pi",
        "E4:5F:01" to "Raspberry Pi",
        "50:C7:BF" to "TP-Link",
        "C0:25:E9" to "TP-Link",
        "98:DA:C4" to "TP-Link",
        "00:26:CE" to "D-Link",
        "14:D6:4D" to "D-Link",
        "00:14:6C" to "Netgear",
        "28:80:23" to "Netgear",
        "00:1E:8C" to "ASUS",
        "04:D4:C4" to "ASUS",
        "AC:84:C6" to "Xiaomi",
        "28:6C:07" to "Xiaomi",
        "00:11:32" to "Synology",
        "00:08:9B" to "QNAP",
        "18:B4:30" to "Nest / Google Home",
        "64:16:66" to "Amazon Lab126 (Echo/Fire)",
        "FC:A1:83" to "Amazon",
        "00:04:20" to "Slim Devices / Logitech",
        "00:17:88" to "Philips Hue",
        "00:21:29" to "Sony",
        "00:04:15" to "Roku"
    )

    fun getVendor(mac: String): String {
        if (mac.isBlank() || mac.length < 8) return "غير معروف (Unknown)"
        val cleanMac = mac.replace("-", ":").replace(".", "").uppercase()
        val prefix = if (cleanMac.length >= 8) cleanMac.substring(0, 8) else cleanMac
        return OUI_DATABASE[prefix] ?: "غير معروف (Unknown)"
    }

    fun detectDeviceType(vendor: String, hostname: String = ""): String {
        val lowerVendor = vendor.lowercase()
        val lowerHost = hostname.lowercase()

        return when {
            routers.any { lowerVendor.contains(it) || lowerHost.contains(it) } -> "راوتر / موجه شبكة"
            phones.any { lowerVendor.contains(it) || lowerHost.contains(it) } -> "هاتف / جهاز محمول"
            vms.any { lowerVendor.contains(it) || lowerHost.contains(it) } -> "جهاز وهمي (VM)"
            pcs.any { lowerVendor.contains(it) || lowerHost.contains(it) } -> "حاسوب / كمبيوتر"
            iots.any { lowerVendor.contains(it) || lowerHost.contains(it) } -> "جهاز ذكي (IoT)"
            else -> "جهاز شبكي"
        }
    }

    private val routers = listOf("cisco", "tp-link", "d-link", "netgear", "asus", "linksys", "mikrotik", "ubiquiti", "huawei", "technicolor", "zte")
    private val phones = listOf("apple", "samsung", "xiaomi", "oneplus", "google", "oppo", "vivo", "realme", "android", "iphone", "galaxy")
    private val vms = listOf("vmware", "virtualbox", "qemu", "hyper-v", "xen", "kvm")
    private val pcs = listOf("dell", "hp", "lenovo", "intel", "gigabyte", "msi", "microsoft", "macbook")
    private val iots = listOf("raspberry", "nest", "amazon", "hue", "philips", "sonos", "roku", "logitech", "tuya", "esp8266", "esp32")
}

package com.example.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.example.model.NetworkInterfaceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.SocketAddress
import java.net.InetSocketAddress

object NetworkUtils {

    fun getNetworkInterfaceInfo(context: Context): NetworkInterfaceInfo {
        var ipAddress = "127.0.0.1"
        var interfaceName = "wlan0"
        var macAddress = "02:00:00:00:00:00"
        var subnetMask = "255.255.255.0"
        var networkCidr = "192.168.1.0/24"
        var isConnected = false

        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

            val network = connManager?.activeNetwork
            val capabilities = connManager?.getNetworkCapabilities(network)
            isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isUp && !iface.isLoopback) {
                    val addrs = iface.inetAddresses
                    while (addrs.hasMoreElements()) {
                        val addr = addrs.nextElement()
                        if (!addr.isLoopbackAddress && addr.address.size == 4) {
                            ipAddress = addr.hostAddress ?: ipAddress
                            interfaceName = iface.name
                            val hardwareAddr = iface.hardwareAddress
                            if (hardwareAddr != null && hardwareAddr.isNotEmpty()) {
                                macAddress = hardwareAddr.joinToString(":") { "%02X".format(it) }
                            }
                            break
                        }
                    }
                }
            }

            // Calculate Subnet CIDR
            val parts = ipAddress.split(".")
            if (parts.size == 4) {
                networkCidr = "${parts[0]}.${parts[1]}.${parts[2]}.0/24"
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return NetworkInterfaceInfo(
            interfaceName = interfaceName,
            ipAddress = ipAddress,
            macAddress = macAddress,
            subnetMask = subnetMask,
            networkCidr = networkCidr,
            isConnected = isConnected
        )
    }

    suspend fun pingAddress(ip: String, timeoutMs: Int = 1000): Long = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val address = InetAddress.getByName(ip)
            if (address.isReachable(timeoutMs)) {
                return@withContext System.currentTimeMillis() - startTime
            }
        } catch (e: Exception) {
            // Ignore
        }
        return@withContext -1L
    }

    suspend fun resolveHostname(ip: String): String = withContext(Dispatchers.IO) {
        try {
            val address = InetAddress.getByName(ip)
            val name = address.canonicalHostName
            if (name != ip) return@withContext name
        } catch (e: Exception) {
            // Ignore
        }
        return@withContext "Unknown / غير معروف"
    }

    suspend fun scanPorts(ip: String, ports: List<Int> = listOf(21, 22, 53, 80, 443, 8080, 8443)): List<Int> = withContext(Dispatchers.IO) {
        val openPorts = mutableListOf<Int>()
        for (port in ports) {
            try {
                val socket = Socket()
                val socketAddress: SocketAddress = InetSocketAddress(ip, port)
                socket.connect(socketAddress, 300) // 300ms timeout
                socket.close()
                openPorts.add(port)
            } catch (e: Exception) {
                // Port closed or filtered
            }
        }
        openPorts
    }
}

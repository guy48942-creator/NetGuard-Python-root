package com.example.utils

object PythonScapyRunner {

    val DEFAULT_PYTHON_SCAPY_CODE = """
from scapy.all import *
import socket
import ipaddress
import threading
import time
from datetime import datetime

class NetworkScanner:
    def __init__(self):
        self.devices = []
        self.lock = threading.Lock()
        
    def get_local_ip(self):
        '''استخراج IP المحلي'''
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            s.connect(("8.8.8.8", 80))
            local_ip = s.getsockname()[0]
            s.close()
            return local_ip
        except:
            return None
    
    def get_network_range(self):
        '''استخراج نطاق الشبكة'''
        local_ip = self.get_local_ip()
        if local_ip:
            # افتراض subnet mask هو 255.255.255.0
            network = ipaddress.IPv4Network(f"{local_ip}/24", strict=False)
            return network
        return None
    
    def get_mac_vendor(self, mac_address):
        '''تحديد الشركة المصنعة من عنوان MAC'''
        # قاعدة بيانات مبسطة - يمكن توسيعها
        vendors = {
            "00:1A:2B": "Apple",
            "00:1B:63": "Apple",
            "00:1E:C2": "Apple",
            "3C:5A:B4": "Google",
            "38:87:D5": "Samsung",
            "8C:85:90": "Samsung",
            "F4:F5:D8": "Huawei",
            "00:1D:92": "Cisco",
            "00:50:56": "VMware",
            "08:00:27": "VirtualBox",
            "B8:27:EB": "Raspberry Pi",
            "50:C7:BF": "TP-Link",
            "C0:25:E9": "TP-Link",
            "00:26:CE": "D-Link",
        }
        
        # البحث عن أول 3 أزواج من MAC
        prefix = mac_address[:8].upper()
        return vendors.get(prefix, "Unknown")
    
    def arp_scan(self, network, timeout=2):
        '''فحص الشبكة باستخدام ARP'''
        print(f"[*] بدء فحص الشبكة: {network}")
        print(f"[*] الوقت: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print("-" * 60)
        
        # إنشاء حزمة ARP
        arp_request = ARP(pdst=str(network))
        # إنشاء إطار Ethernet broadcast
        broadcast = Ether(dst="ff:ff:ff:ff:ff:ff")
        # دمج الحزمتين
        arp_request_broadcast = broadcast / arp_request
        
        # إرسال الحزم واستقبال الردود (يتطلب صلاحيات روت على أندرويد)
        answered_list = srp(arp_request_broadcast, timeout=timeout, verbose=False)[0]
        
        # معالجة النتائج
        devices = []
        for sent, received in answered_list:
            device_info = {
                "ip": received.psrc,
                "mac": received.hwsrc,
                "vendor": self.get_mac_vendor(received.hwsrc)
            }
            devices.append(device_info)
            
        return devices
    
    def scan_network(self):
        '''الوظيفة الرئيسية للفحص'''
        network = self.get_network_range()
        if not network:
            print("[-] فشل في تحديد نطاق الشبكة")
            return None
            
        print(f"[*] نطاق الشبكة: {network}")
        devices = self.arp_scan(network)
        
        return devices
    
    def display_devices(self, devices):
        '''عرض الأجهزة المكتشفة'''
        print("\n" + "=" * 70)
        print("🎯 الأجهزة المتصلة بالشبكة:")
        print("=" * 70)
        print(f"{'IP Address':<20} {'MAC Address':<20} {'النوع/الشركة':<15} {'الحالة':<10}")
        print("-" * 70)
        
        for device in devices:
            # تحديد نوع الجهاز
            device_type = self.detect_device_type(device['mac'])
            
            print(f"{device['ip']:<20} {device['mac']:<20} {device['vendor']:<15} {device_type:<10}")
        
        print("=" * 70)
        print(f"✅ تم اكتشاف {len(devices)} جهاز")
    
    def detect_device_type(self, mac):
        '''تحديد نوع الجهاز'''
        # الأجهزة المعروفة
        routers = ["Cisco", "TP-Link", "D-Link", "NetGear", "ASUS", "Linksys"]
        phones = ["Apple", "Samsung", "Huawei", "Xiaomi", "Google"]
        
        vendor = self.get_mac_vendor(mac)
        
        if vendor in routers:
            return "راوتر"
        elif vendor in phones:
            return "هاتف/جهاز"
        elif vendor == "VMware" or vendor == "VirtualBox":
            return "جهاز وهمي"
        else:
            return "جهاز"
    
    def monitor_network(self, interval=10):
        '''مراقبة الشبكة باستمرار'''
        print("[*] بدء مراقبة الشبكة (Ctrl+C للإيقاف)")
        print(f"[*] سيتم الفحص كل {interval} ثانية")
        
        try:
            while True:
                devices = self.scan_network()
                if devices:
                    self.display_devices(devices)
                
                print(f"\n[*] سيتم إعادة الفحص بعد {interval} ثانية...")
                time.sleep(interval)
                
        except KeyboardInterrupt:
            print("\n[!] تم إيقاف المراقبة")
    
    def get_device_details(self, ip, mac):
        '''الحصول على تفاصيل إضافية عن جهاز'''
        details = {
            "ip": ip,
            "mac": mac,
            "vendor": self.get_mac_vendor(mac),
            "type": self.detect_device_type(mac),
            "hostname": self.resolve_hostname(ip),
        }
        return details
    
    def resolve_hostname(self, ip):
        '''محاولة الحصول على اسم الجهاز'''
        try:
            hostname = socket.gethostbyaddr(ip)
            return hostname[0]
        except:
            return "Unknown"

# استخدام الكود
def main():
    scanner = NetworkScanner()
    
    print('''
    ╔══════════════════════════════════════════╗
    ║     Network Scanner - ماسح الشبكة       ║
    ║     اكتشاف الأجهزة المتصلة بالشبكة      ║
    ╚══════════════════════════════════════════╝
    ''')
    
    # فحص سريع
    devices = scanner.scan_network()
    if devices:
        scanner.display_devices(devices)

if __name__ == "__main__":
    main()
""".trimIndent()

    val BUILDOZER_SPEC_TEMPLATE = """
# Buildozer Specification for Kivy + Scapy Root APK
[app]
title = ARP Scanner Root
package.name = arpscanner
package.domain = org.network
source.dir = .
source.include_exts = py,png,jpg,kv,atlas

version = 1.0.0
requirements = python3,kivy,scapy,paramiko,libffi,openssl

orientation = portrait
fullscreen = 0

# Android Permissions
android.permissions = INTERNET, ACCESS_WIFI_STATE, ACCESS_NETWORK_STATE, CHANGE_WIFI_STATE
android.api = 33
android.minapi = 24
android.ndk = 25b
android.archs = arm64-v8a, armeabi-v7a

# Root / Native binaries permissions
android.add_src = .
""".trimIndent()

    val BUILD_COMMANDS_GUIDE = """
# 1. تثبيت المتطلبات الأساسية على Linux / Ubuntu / WSL:
sudo apt update
sudo apt install -y git zip unzip openjdk-17-jdk python3-pip autoconf libtool pkg-config zlib1g-dev libncurses5-dev libncursesw5-dev libtinfo5 cmake libffi-dev libssl-dev cython3

# 2. تثبيت Buildozer والحزم الإضافية (Scapy & Paramiko):
pip install --upgrade buildozer paramiko
wget -q https://raw.githubusercontent.com/C3s1um133/Network-Scanner-PORT-ALIVE/master/neighbors_parser.py
chmod +x neighbors_parser.py

# 3. بناء وتجميع ملف APK:
# بناء حزمة تصحيح الأخطاء (Debug APK):
buildozer android debug

# أو التجميع مع عرض تفاصيل السجل الكامل (Verbose):
buildozer -v android debug
""".trimIndent()
}

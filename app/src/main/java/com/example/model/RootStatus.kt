package com.example.model

data class RootStatusInfo(
    val isRooted: Boolean = false,
    val isSuAvailable: Boolean = false,
    val superuserApp: String = "None", // e.g. Magisk, KernelSU, SuperSU
    val uidInfo: String = "",
    val lastCheckTime: Long = System.currentTimeMillis(),
    val rawSocketSupported: Boolean = false,
    val selinuxMode: String = "Enforcing" // Enforcing / Permissive / Unknown
)

data class TerminalCommandResult(
    val command: String,
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val durationMs: Long
)

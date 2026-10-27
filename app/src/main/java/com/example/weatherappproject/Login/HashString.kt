package com.example.weatherappproject.Login

import java.security.MessageDigest

class HashString {
    fun sha256(input: String): String? {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(input.toByteArray())
            val hexString = StringBuilder()
            for (b in hash) {
                hexString.append(String.format("%02x", b))
            }
            hexString.toString()
        } catch (e: Exception) {
            null
        }
    }
}

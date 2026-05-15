package com.example.sceneenglish.util

import java.security.MessageDigest

object HashUtils {
    fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

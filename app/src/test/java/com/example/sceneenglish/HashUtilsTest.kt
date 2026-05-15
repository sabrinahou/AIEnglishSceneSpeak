package com.example.sceneenglish

import com.example.sceneenglish.util.HashUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class HashUtilsTest {
    @Test
    fun sha256_isStable() {
        assertEquals(
            "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
            HashUtils.sha256("hello")
        )
    }
}

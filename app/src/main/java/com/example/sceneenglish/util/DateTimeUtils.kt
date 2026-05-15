package com.example.sceneenglish.util

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    fun nowIso(): String = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

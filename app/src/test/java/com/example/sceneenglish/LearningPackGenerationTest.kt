package com.example.sceneenglish

import com.example.sceneenglish.data.ai.MockLearningPack
import com.example.sceneenglish.domain.model.LearningPack
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningPackGenerationTest {
    @Test
    fun mockTennisPack_hasRichGroupedVocabulary() {
        val pack = MockLearningPack.tennis("我是网球初学者，第一次和英语网球教练上课。", "A1-A2")
        val categories = pack.vocabulary.map { it.categoryZh }.toSet()

        assertTrue("expected rich vocabulary", pack.vocabulary.size >= 40)
        assertTrue("expected grouped vocabulary", categories.size >= 8)
        assertTrue(pack.vocabulary.any { it.categoryZh == "场地区域" && it.english == "baseline" })
        assertTrue(pack.vocabulary.any { it.categoryZh == "技术细节" && it.english == "follow-through" })
        assertTrue(pack.phrases.map { it.categoryZh }.toSet().size >= 4)
        assertTrue(pack.sentences.map { it.categoryZh }.toSet().size >= 3)
    }

    @Test
    fun groupedVocabulary_roundTripsThroughJson() {
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        val pack = MockLearningPack.tennis("网球课", "A1-A2")
        val decoded = json.decodeFromString<LearningPack>(json.encodeToString(pack))

        assertEquals(pack.vocabulary.first().categoryZh, decoded.vocabulary.first().categoryZh)
        assertEquals(pack.vocabulary.first().priority, decoded.vocabulary.first().priority)
    }
}

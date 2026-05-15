package com.example.sceneenglish.data.ai

import android.util.Base64
import android.util.Log
import com.example.sceneenglish.data.local.SecureSettingsStore
import com.example.sceneenglish.domain.model.EvaluateRequest
import com.example.sceneenglish.domain.model.EvaluationResult
import com.example.sceneenglish.domain.model.GeneratePackRequest
import com.example.sceneenglish.domain.model.Dialogue
import com.example.sceneenglish.domain.model.LearningPack
import com.example.sceneenglish.domain.model.PhraseItem
import com.example.sceneenglish.domain.model.ReviewQuizItem
import com.example.sceneenglish.domain.model.RoleplayRequest
import com.example.sceneenglish.domain.model.RoleplayResult
import com.example.sceneenglish.domain.model.RoleplayTask
import com.example.sceneenglish.domain.model.SentenceItem
import com.example.sceneenglish.domain.model.SpeechSpeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonPrimitive
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class OpenAiClient(
    private val secureSettingsStore: SecureSettingsStore,
    private val json: Json,
    private val textModel: String = "gpt-4.1-mini",
    private val imageModel: String = "gpt-image-1",
    private val speechModel: String = "gpt-4o-mini-tts",
    private val transcribeModel: String = "gpt-4o-mini-transcribe"
) : AiClient {
    override suspend fun generateLearningPack(request: GeneratePackRequest): LearningPack {
        return generateInitialLearningPack(request)
    }

    override suspend fun generateInitialLearningPack(request: GeneratePackRequest): LearningPack {
        val prompt = AiPrompts.initialLearningPackPrompt(request.sourceInput, request.level)
        val body = responsesJsonBody(prompt)
        Log.i(TAG, "Generating initial learning pack with model=$textModel")
        val response = postJson("https://api.openai.com/v1/responses", body, readTimeoutMs = 60_000)
        return json.decodeFromString<LearningPack>(
            normalizeLearningPackJson(
                rawText = extractOutputText(response),
                request = request
            )
        )
    }

    override suspend fun generatePhrases(pack: LearningPack): List<PhraseItem> {
        return generateList(
            prompt = AiPrompts.phrasesPrompt(pack),
            arrayKey = "phrases"
        )
    }

    override suspend fun generateSentences(pack: LearningPack): List<SentenceItem> {
        return generateList(
            prompt = AiPrompts.sentencesPrompt(pack),
            arrayKey = "sentences"
        )
    }

    override suspend fun generateDialogues(pack: LearningPack): List<Dialogue> {
        return generateList(
            prompt = AiPrompts.dialoguesPrompt(pack),
            arrayKey = "dialogues"
        )
    }

    override suspend fun generateRoleplayTasks(pack: LearningPack): List<RoleplayTask> {
        return generateList(
            prompt = AiPrompts.roleplayTasksPrompt(pack),
            arrayKey = "roleplayTasks"
        )
    }

    override suspend fun generateReviewQuiz(pack: LearningPack): List<ReviewQuizItem> {
        return generateList(
            prompt = AiPrompts.reviewQuizPrompt(pack),
            arrayKey = "reviewQuiz"
        )
    }

    override suspend fun generateSpeech(text: String, speed: SpeechSpeed): ByteArray {
        val body = buildJsonObject {
            put("model", speechModel)
            put("voice", "marin")
            put("input", text)
            put(
                "instructions",
                "Speak in a warm, clear, natural American English voice, like a patient real-life language coach. Pronounce vocabulary carefully, with natural rhythm and no extra explanation."
            )
            put("response_format", "mp3")
            put("speed", if (speed == SpeechSpeed.Slow) 0.75 else 1.0)
        }.toString()
        return postBytes("https://api.openai.com/v1/audio/speech", body)
    }

    override suspend fun generateImage(prompt: String): ByteArray {
        val body = buildJsonObject {
            put("model", imageModel)
            put("prompt", prompt)
            put("size", "1024x1024")
            put("quality", "medium")
        }.toString()
        Log.i(TAG, "Generating scene image with model=$imageModel")
        val response = postJson("https://api.openai.com/v1/images/generations", body, readTimeoutMs = 120_000)
        val imageBase64 = json.parseToJsonElement(response)
            .jsonObject["data"]
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?.get("b64_json")
            ?.jsonPrimitive
            ?.contentOrNull
            ?: error("AI response did not include image data.")
        return Base64.decode(imageBase64, Base64.DEFAULT)
    }

    override suspend fun transcribeAudio(audioBytes: ByteArray): String {
        val response = postMultipartAudio(
            url = "https://api.openai.com/v1/audio/transcriptions",
            fields = mapOf("model" to transcribeModel, "response_format" to "json"),
            audioBytes = audioBytes
        )
        val root = json.parseToJsonElement(response).jsonObject
        return root["text"]?.jsonPrimitive?.contentOrNull.orEmpty()
    }

    override suspend fun evaluateTranslation(request: EvaluateRequest): EvaluationResult {
        val prompt = """
            You are an English speaking coach for a Chinese-speaking learner.

            Evaluate the user's English answer for a Chinese prompt.
            Scenario: ${request.scenarioTitle}
            Chinese prompt: ${request.promptZh}
            Expected answer: ${request.expectedAnswer}
            User answer: ${request.userAnswer}

            Return JSON only with:
            - isCorrect: boolean
            - meaningScore: 0-100
            - grammarScore: 0-100
            - naturalnessScore: 0-100
            - correctedAnswer: string
            - naturalAnswer: string
            - feedbackZh: string
        """.trimIndent()
        val response = postJson("https://api.openai.com/v1/responses", responsesJsonBody(prompt))
        return json.decodeFromString<EvaluationResult>(extractOutputText(response))
    }

    override suspend fun nextRoleplayTurn(request: RoleplayRequest): RoleplayResult {
        val history = request.history.joinToString("\n") { "${it.speaker}: ${it.english}" }
        val prompt = """
            You are roleplaying as a realistic conversation partner in this scenario.

            Scenario: ${request.scenarioTitle}
            User role: ${request.userRole}
            Your role: ${request.assistantRole}
            Learner level: ${request.level}
            Conversation history:
            $history
            User answer:
            ${request.userAnswer}

            Rules:
            1. Use simple American English suitable for ${request.level}.
            2. Correct the user's English in Simplified Chinese.
            3. Then continue with only one next English message.
            4. Return JSON only.

            Return format:
            {
              "feedback": {
                "correctedAnswer": "...",
                "naturalAnswer": "...",
                "feedbackZh": "..."
              },
              "nextMessage": {
                "id": "...",
                "speaker": "...",
                "english": "...",
                "chinese": "..."
              }
            }
        """.trimIndent()
        val response = postJson("https://api.openai.com/v1/responses", responsesJsonBody(prompt))
        return json.decodeFromString<RoleplayResult>(extractOutputText(response))
    }

    private fun responsesJsonBody(prompt: String): String {
        return buildJsonObject {
            put("model", textModel)
            put("input", prompt)
            put("max_output_tokens", 5000)
            put("text", buildJsonObject {
                put("format", buildJsonObject {
                    put("type", "json_object")
                })
            })
        }.toString()
    }

    private suspend inline fun <reified T> generateList(prompt: String, arrayKey: String): List<T> {
        val response = postJson(
            url = "https://api.openai.com/v1/responses",
            body = responsesJsonBody(prompt),
            readTimeoutMs = 60_000
        )
        val output = json.parseToJsonElement(extractOutputText(response))
        val array = when (output) {
            is JsonArray -> output
            is JsonObject -> output[arrayKey] as? JsonArray ?: JsonArray(emptyList())
            else -> JsonArray(emptyList())
        }
        return json.decodeFromString(array.toString())
    }

    private suspend fun postJson(
        url: String,
        body: String,
        readTimeoutMs: Int = 45_000
    ): String = withContext(Dispatchers.IO) {
        val connection = openConnection(url, "application/json", readTimeoutMs)
        connection.outputStream.use { it.write(body.toByteArray()) }
        readResponse(connection).decodeToString()
    }

    private suspend fun postBytes(url: String, body: String): ByteArray = withContext(Dispatchers.IO) {
        val connection = openConnection(url, "application/json", 45_000)
        connection.outputStream.use { it.write(body.toByteArray()) }
        readResponse(connection)
    }

    private suspend fun postMultipartAudio(
        url: String,
        fields: Map<String, String>,
        audioBytes: ByteArray
    ): String = withContext(Dispatchers.IO) {
        val boundary = "SceneEnglish-${UUID.randomUUID()}"
        val connection = openConnection(url, "multipart/form-data; boundary=$boundary", 45_000)
        connection.outputStream.use { out ->
            fields.forEach { (name, value) ->
                out.write("--$boundary\r\n".toByteArray())
                out.write("Content-Disposition: form-data; name=\"$name\"\r\n\r\n".toByteArray())
                out.write(value.toByteArray())
                out.write("\r\n".toByteArray())
            }
            out.write("--$boundary\r\n".toByteArray())
            out.write("Content-Disposition: form-data; name=\"file\"; filename=\"recording.m4a\"\r\n".toByteArray())
            out.write("Content-Type: audio/mp4\r\n\r\n".toByteArray())
            out.write(audioBytes)
            out.write("\r\n--$boundary--\r\n".toByteArray())
        }
        readResponse(connection).decodeToString()
    }

    private fun openConnection(url: String, contentType: String, readTimeoutMs: Int): HttpURLConnection {
        val apiKey = secureSettingsStore.getApiKey()
            ?: error("请先在设置中填写 API Key。")
        return (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 20_000
            readTimeout = readTimeoutMs
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", contentType)
        }
    }

    private fun readResponse(connection: HttpURLConnection): ByteArray {
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val bytes = stream.use { input ->
            ByteArrayOutputStream().use { output ->
                input.copyTo(output)
                output.toByteArray()
            }
        }
        if (code !in 200..299) {
            error("OpenAI API error $code: ${bytes.decodeToString()}")
        }
        return bytes
    }

    private fun extractOutputText(response: String): String {
        val root = json.parseToJsonElement(response)
        return findFirstOutputText(root)?.trim()
            ?: error("AI response did not include output text.")
    }

    private fun normalizeLearningPackJson(rawText: String, request: GeneratePackRequest): String {
        val parsed = json.parseToJsonElement(rawText)
        val candidate = unwrapLearningPack(parsed)
        val obj = candidate.jsonObject
        val now = java.time.OffsetDateTime.now().toString()
        val patched = buildJsonObject {
            put("id", obj["id"] ?: JsonPrimitive("pack_${System.currentTimeMillis()}"))
            put("scenarioTitle", obj["scenarioTitle"] ?: obj["title"] ?: JsonPrimitive(request.sourceInput.take(40)))
            put(
                "scenarioDescription",
                obj["scenarioDescription"] ?: obj["description"] ?: JsonPrimitive(request.sourceInput)
            )
            put("sourceInput", obj["sourceInput"] ?: JsonPrimitive(request.sourceInput))
            put("level", obj["level"] ?: JsonPrimitive(request.level))
            put("targetAccent", obj["targetAccent"] ?: JsonPrimitive("american"))
            put("createdAt", obj["createdAt"] ?: JsonPrimitive(now))
            put("vocabulary", obj["vocabulary"] ?: JsonArray(emptyList()))
            put("phrases", obj["phrases"] ?: JsonArray(emptyList()))
            put("sentences", obj["sentences"] ?: JsonArray(emptyList()))
            put("dialogues", obj["dialogues"] ?: JsonArray(emptyList()))
            put("imagePrompt", obj["imagePrompt"] ?: JsonPrimitive("Wordless realistic scene image. No text."))
            put("imageLabels", obj["imageLabels"] ?: JsonArray(emptyList()))
            put("roleplayTasks", obj["roleplayTasks"] ?: JsonArray(emptyList()))
            put("reviewQuiz", obj["reviewQuiz"] ?: JsonArray(emptyList()))
        }
        return patched.toString()
    }

    private fun unwrapLearningPack(element: JsonElement): JsonElement {
        val obj = element.jsonObject
        val wrapperKeys = listOf("learningPack", "learning_pack", "pack", "data", "result")
        return wrapperKeys.firstNotNullOfOrNull { key ->
            obj[key]?.takeIf { it is JsonObject }
        } ?: element
    }

    private fun findFirstOutputText(element: JsonElement): String? {
        if (element is JsonObject && element["type"]?.jsonPrimitive?.contentOrNull == "output_text") {
            return element["text"]?.jsonPrimitive?.contentOrNull
        }
        return when (element) {
            is JsonObject -> element.values.firstNotNullOfOrNull(::findFirstOutputText)
            is JsonArray -> element.firstNotNullOfOrNull(::findFirstOutputText)
            else -> null
        }
    }

    private fun findFirstImageResult(element: JsonElement): String? {
        if (element is JsonObject && element["type"]?.jsonPrimitive?.contentOrNull == "image_generation_call") {
            return element["result"]?.jsonPrimitive?.contentOrNull
        }
        return when (element) {
            is JsonObject -> element.values.firstNotNullOfOrNull(::findFirstImageResult)
            is JsonArray -> element.firstNotNullOfOrNull(::findFirstImageResult)
            else -> null
        }
    }

    private companion object {
        const val TAG = "OpenAiClient"
    }
}

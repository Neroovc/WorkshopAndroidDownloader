package top.apricityx.workshop

import android.content.Context
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import top.apricityx.workshop.steam.protocol.applyDefaultHttpTimeouts

data class BaiduTranslationCredentials(
    val appId: String = "",
    val apiKey: String = "",
) {
    fun isConfigured(): Boolean = appId.isNotBlank() && apiKey.isNotBlank()
}

class BaiduAiTextTranslationClient(
    context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .applyDefaultHttpTimeouts()
        .applyAppNetworkLogging("baidu-translate")
        .build(),
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val baseUrl: HttpUrl = "https://fanyi-api.baidu.com/".toHttpUrl(),
) {
    private val context = context.applicationContext

    suspend fun translate(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        credentials: BaiduTranslationCredentials,
        reference: String? = null,
    ): String = withContext(Dispatchers.IO) {
        require(credentials.isConfigured()) {
            context.getString(R.string.error_baidu_not_configured)
        }

        val normalizedText = text.trim()
        if (normalizedText.isBlank()) {
            return@withContext normalizedText
        }
        val normalizedReference = reference
            ?.trim()
            ?.takeIf(String::isNotBlank)

        workshopLogInfo(
            "BAIDU translationPrompt from=$sourceLanguage to=$targetLanguage reference=${normalizedReference ?: "<none>"}",
        )

        val requestBody = buildJsonObject {
            put("appid", credentials.appId)
            put("from", sourceLanguage)
            put("to", targetLanguage)
            put("q", normalizedText)
            put("model_type", MODEL_TYPE_LLM)
            normalizedReference?.let { put("reference", it) }
        }.toString().toRequestBody(JSON_MEDIA_TYPE)

        val request = Request.Builder()
            .url(baseUrl.newBuilder().addPathSegments("ait/api/aiTextTranslate").build())
            .header("Authorization", "Bearer ${credentials.apiKey}")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val payload = response.body?.string().orEmpty()
            val root = parsePayload(payload)
            val errorCode = root["error_code"]?.jsonPrimitive?.contentOrNull
            if (!response.isSuccessful || !errorCode.isNullOrBlank() && errorCode != "0") {
                throw IllegalStateException(buildErrorMessage(errorCode, root["error_msg"]?.jsonPrimitive?.contentOrNull))
            }

            extractTranslatedText(root)?.let { translatedText ->
                return@withContext translatedText
            }

            throw IllegalStateException(context.getString(R.string.error_baidu_unrecognized))
        }
    }

    private fun parsePayload(payload: String): JsonObject {
        if (payload.isBlank()) {
            throw IllegalStateException(context.getString(R.string.error_baidu_empty_response))
        }
        return runCatching {
            json.parseToJsonElement(payload).jsonObject
        }.getOrElse {
            throw IllegalStateException(context.getString(R.string.error_baidu_unparseable))
        }
    }

    private fun buildErrorMessage(
        errorCode: String?,
        errorMessage: String?,
    ): String {
        val normalizedMessage = errorMessage?.takeIf(String::isNotBlank) ?: context.getString(R.string.error_baidu_request_failed)
        return if (errorCode.isNullOrBlank()) {
            context.getString(R.string.error_baidu_translation_failed, normalizedMessage)
        } else {
            context.getString(R.string.error_baidu_code_failed, normalizedMessage, errorCode.toIntOrNull() ?: 0)
        }
    }

    private fun extractTranslatedText(root: JsonObject): String? {
        val data = root["data"]
        val directDataText = data
            ?.takeIf { it !is JsonObject && it !is JsonArray }
            ?.jsonPrimitive
            ?.contentOrNull
            ?.takeIf(String::isNotBlank)
        if (directDataText != null) {
            return directDataText
        }

        val preferredMatches = collectPreferredTranslatedTexts(data).ifEmpty {
            collectPreferredTranslatedTexts(root)
        }
        return preferredMatches
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .takeIf(List<String>::isNotEmpty)
            ?.joinToString("\n")
    }

    private fun collectPreferredTranslatedTexts(element: JsonElement?): List<String> {
        return when (element) {
            is JsonObject -> {
                val directMatches = PREFERRED_RESULT_KEYS.mapNotNull { key ->
                    val value = element[key]
                    if (value == null || value is JsonObject || value is JsonArray) {
                        null
                    } else {
                        value.jsonPrimitive.contentOrNull?.takeIf(String::isNotBlank)
                    }
                }
                if (directMatches.isNotEmpty()) {
                    return directMatches
                }

                element.values.flatMap(::collectPreferredTranslatedTexts)
            }

            is JsonArray -> element.flatMap(::collectPreferredTranslatedTexts)
            else -> emptyList()
        }
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private const val MODEL_TYPE_LLM = "llm"
        private val PREFERRED_RESULT_KEYS = listOf(
            "dst",
            "translation",
            "translatedText",
            "targetText",
            "target_text",
            "result",
            "output",
        )
    }
}

fun mapLocaleLanguageToBaiduLanguage(locale: Locale): String? =
    when (locale.language.lowercase(Locale.ROOT)) {
        "zh" -> "zh"
        "en" -> "en"
        "ja" -> "jp"
        "ko" -> "kor"
        "de" -> "de"
        "ru" -> "ru"
        "es" -> "spa"
        "fr" -> "fra"
        "it" -> "it"
        "pt" -> "pt"
        "ar" -> "ara"
        "fi" -> "fin"
        "sk" -> "sk"
        "sv" -> "swe"
        else -> null
    }

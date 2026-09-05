package top.apricityx.workshop

import android.content.Context

private const val BAIDU_REFERENCE_LIMIT = 500
private const val BAIDU_REFERENCE_NAME_LIMIT = 80
private val BAIDU_REFERENCE_WHITESPACE = Regex("\\s+")

internal fun buildBaiduModDescriptionReference(
    context: Context,
    modTitle: String,
    gameTitle: String,
): String =
    buildString {
        append(context.getString(R.string.baidu_reference_intro))
        append(' ')

        normalizeBaiduReferenceValue(modTitle).takeIf(String::isNotEmpty)?.let { normalizedModTitle ->
            append(context.getString(R.string.baidu_reference_mod_name_prefix))
            append(' ')
            append(normalizedModTitle)
            append(". ")
        }

        normalizeBaiduReferenceValue(gameTitle).takeIf(String::isNotEmpty)?.let { normalizedGameTitle ->
            append(context.getString(R.string.baidu_reference_game_name_prefix))
            append(' ')
            append(normalizedGameTitle)
            append(". ")
        }

        append(context.getString(R.string.baidu_reference_guideline))
    }.take(BAIDU_REFERENCE_LIMIT)

private fun normalizeBaiduReferenceValue(value: String): String =
    value.trim()
        .replace(BAIDU_REFERENCE_WHITESPACE, " ")
        .take(BAIDU_REFERENCE_NAME_LIMIT)

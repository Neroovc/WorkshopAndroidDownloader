package top.apricityx.workshop

import android.content.Context

private const val STEAM_CDN_UNAUTHORIZED_MESSAGE = "Steam CDN request failed: 401"

internal fun formatDownloadFailureMessage(
    context: Context,
    rawMessage: String,
    gameTitle: String,
    hasBoundAccount: Boolean,
    ownershipStatus: SteamAppOwnershipStatus,
): String {
    val normalizedMessage = rawMessage.trim().ifBlank { context.getString(R.string.download_failed_generic) }
    if (!normalizedMessage.contains(STEAM_CDN_UNAUTHORIZED_MESSAGE)) {
        return normalizedMessage
    }

    if (!hasBoundAccount) {
        return context.getString(R.string.download_failed_401)
    }

    return when (ownershipStatus) {
        SteamAppOwnershipStatus.NotOwned -> {
            val resolvedGameTitle = gameTitle.ifBlank { context.getString(R.string.download_failed_fallback_game) }
            context.getString(R.string.download_failed_401_not_owned, resolvedGameTitle)
        }

        SteamAppOwnershipStatus.Owned -> normalizedMessage
        SteamAppOwnershipStatus.Unknown -> context.getString(R.string.download_failed_401)
    }
}

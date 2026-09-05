package top.apricityx.workshop

import android.content.Context
import kotlinx.serialization.Serializable

@Serializable
data class ModLibraryUpdateCheckState(
    val isChecking: Boolean = false,
    val summaryMessage: String? = null,
    val lastCheckedAtMillis: Long? = null,
    val results: Map<String, ModUpdateCheckResult> = emptyMap(),
)

@Serializable
data class ModUpdateCheckResult(
    val status: ModUpdateCheckStatus = ModUpdateCheckStatus.Unknown,
    val remoteUpdatedAtMillis: Long? = null,
    val checkedAtMillis: Long? = null,
    val message: String? = null,
)

@Serializable
enum class ModUpdateCheckStatus {
    Unknown,
    Checking,
    UpToDate,
    UpdateAvailable,
    Failed,
}

fun DownloadedModEntry.modLibraryKey(): String =
    "${appId}-${publishedFileId}-${normalizeModVersionId(versionId)}"

fun evaluateModUpdate(
    entry: DownloadedModEntry,
    remoteUpdatedEpochSeconds: Long?,
    checkedAtMillis: Long,
    context: Context,
): ModUpdateCheckResult {
    val remoteUpdatedAtMillis = remoteUpdatedEpochSeconds?.times(1000L)
    if (remoteUpdatedAtMillis == null) {
        return ModUpdateCheckResult(
            status = ModUpdateCheckStatus.Failed,
            checkedAtMillis = checkedAtMillis,
            message = context.getString(R.string.mod_update_no_time),
        )
    }

    return ModUpdateCheckResult(
        status = if (remoteUpdatedAtMillis > (entry.versionUpdatedAtMillis ?: entry.storedAtMillis)) {
            ModUpdateCheckStatus.UpdateAvailable
        } else {
            ModUpdateCheckStatus.UpToDate
        },
        remoteUpdatedAtMillis = remoteUpdatedAtMillis,
        checkedAtMillis = checkedAtMillis,
    )
}

fun buildModUpdateCheckSummary(
    results: Collection<ModUpdateCheckResult>,
    context: Context,
): String {
    if (results.isEmpty()) {
        return context.getString(R.string.mod_update_no_mods)
    }

    val availableCount = results.count { it.status == ModUpdateCheckStatus.UpdateAvailable }
    val upToDateCount = results.count { it.status == ModUpdateCheckStatus.UpToDate }
    val failedCount = results.count { it.status == ModUpdateCheckStatus.Failed }
    return context.getString(
        R.string.mod_update_summary,
        availableCount,
        upToDateCount,
        failedCount,
    )
}

fun ModLibraryUpdateCheckState.filterForEntries(
    entries: List<DownloadedModEntry>,
    context: Context,
): ModLibraryUpdateCheckState {
    val validKeys = entries.map(DownloadedModEntry::modLibraryKey).toSet()
    val filteredResults = results.filterKeys(validKeys::contains)
    return copy(
        summaryMessage = when {
            isChecking -> summaryMessage
            filteredResults.isEmpty() -> null
            summaryMessage == null -> null
            else -> buildModUpdateCheckSummary(filteredResults.values, context)
        },
        results = filteredResults,
    )
}

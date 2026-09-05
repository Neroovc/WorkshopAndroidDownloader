package top.apricityx.workshop.ui.component

import android.content.Context
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import top.apricityx.workshop.DownloadedModEntry
import top.apricityx.workshop.ModUpdateCheckResult
import top.apricityx.workshop.ModUpdateCheckStatus
import top.apricityx.workshop.R

private val modLibraryTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

internal fun buildModEntryMetrics(context: Context, entry: DownloadedModEntry): List<String> =
    listOf(
        "AppID ${entry.appId}",
        context.getString(R.string.metric_mod, entry.publishedFileId),
        context.getString(R.string.metric_files, entry.files.size),
        context.getString(R.string.metric_synced, formatModLibraryTimestamp(entry.storedAtMillis)),
    )

internal fun formatModLibraryTimestamp(timestampMillis: Long): String =
    modLibraryTimeFormatter.format(
        Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()),
    )

@Composable
internal fun ModUpdateStatusText(
    result: ModUpdateCheckResult?,
    modifier: Modifier = Modifier,
) {
    val message = result?.displayText()?.takeIf(String::isNotBlank) ?: return
    Text(
        text = message,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = when (result.status) {
            ModUpdateCheckStatus.UpdateAvailable -> MaterialTheme.colorScheme.primary
            ModUpdateCheckStatus.Failed -> MaterialTheme.colorScheme.error
            ModUpdateCheckStatus.Checking -> MaterialTheme.colorScheme.secondary
            ModUpdateCheckStatus.UpToDate,
            ModUpdateCheckStatus.Unknown,
            -> MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
}

@Composable
internal fun ModPreviewImage(
    previewImagePath: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    fillMaxWidth: Boolean = true,
) {
    if (previewImagePath.isNullOrBlank()) {
        return
    }

    AsyncImage(
        model = previewImagePath,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = if (fillMaxWidth) {
            modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        } else {
            modifier.aspectRatio(16f / 9f)
        },
    )
}

@Composable
private fun ModUpdateCheckResult.displayText(): String =
    when (status) {
        ModUpdateCheckStatus.Unknown -> ""
        ModUpdateCheckStatus.Checking -> stringResource(R.string.mod_update_checking)
        ModUpdateCheckStatus.UpToDate -> remoteUpdatedAtMillis
            ?.let { stringResource(R.string.mod_update_up_to_date_at, formatModLibraryTimestamp(it)) }
            ?: stringResource(R.string.mod_update_up_to_date)
        ModUpdateCheckStatus.UpdateAvailable -> remoteUpdatedAtMillis
            ?.let { stringResource(R.string.mod_update_available_at, formatModLibraryTimestamp(it)) }
            ?: stringResource(R.string.mod_update_available)
        ModUpdateCheckStatus.Failed -> message ?: stringResource(R.string.mod_update_check_failed)
    }

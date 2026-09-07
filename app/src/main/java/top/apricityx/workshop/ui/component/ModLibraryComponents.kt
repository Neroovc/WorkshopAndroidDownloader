package top.apricityx.workshop.ui.component

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
import top.apricityx.workshop.R
import top.apricityx.workshop.DownloadedModEntry
import top.apricityx.workshop.ModUpdateCheckResult
import top.apricityx.workshop.ModUpdateCheckStatus

private val modLibraryTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

@Composable
internal fun buildModEntryMetrics(entry: DownloadedModEntry): List<String> =
    listOf(
        stringResource(R.string.app_id_format, entry.appId.toString()),
        stringResource(R.string.mod_id_metric_format, entry.publishedFileId.toString()),
        stringResource(R.string.files_count_metric_format, entry.files.size),
        stringResource(R.string.synced_metric_format, formatModLibraryTimestamp(entry.storedAtMillis)),
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
private fun ModUpdateCheckResult.displayText(): String {
    val timestampText = remoteUpdatedAtMillis?.let(::formatModLibraryTimestamp)
    return when (status) {
        ModUpdateCheckStatus.Unknown -> ""
        ModUpdateCheckStatus.Checking -> stringResource(R.string.checking_workshop_updates)
        ModUpdateCheckStatus.UpToDate -> if (timestampText != null) {
            stringResource(R.string.up_to_date_with_date_format, timestampText)
        } else {
            stringResource(R.string.up_to_date)
        }
        ModUpdateCheckStatus.UpdateAvailable -> if (timestampText != null) {
            stringResource(R.string.update_available_with_date_format, timestampText)
        } else {
            stringResource(R.string.update_available)
        }
        ModUpdateCheckStatus.Failed -> message ?: stringResource(R.string.update_check_failed)
    }
}

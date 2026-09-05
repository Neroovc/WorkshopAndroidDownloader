package top.apricityx.workshop

import android.content.Context
import java.text.DecimalFormat
import kotlinx.serialization.Serializable
import top.apricityx.workshop.workshop.DownloadState

@Serializable
enum class DownloadCenterTaskStatus {
    Queued,
    Running,
    Paused,
    Success,
    Failed,
}

@Serializable
data class DownloadCenterProgressSnapshot(
    val writtenBytes: Long = 0L,
    val totalBytes: Long? = null,
    val completedChunks: Int = 0,
    val totalChunks: Int? = null,
    val completedFiles: Int = 0,
    val totalFiles: Int? = null,
    val speedBytesPerSecond: Long? = null,
)

@Serializable
data class DownloadCenterTaskUiState(
    val id: String,
    val appId: UInt,
    val publishedFileId: ULong,
    val gameTitle: String,
    val itemTitle: String,
    val boundAccountId: String? = null,
    val boundAccountName: String = "anonymous",
    val status: DownloadCenterTaskStatus = DownloadCenterTaskStatus.Queued,
    val phase: DownloadState = DownloadState.Idle,
    val logs: List<String> = emptyList(),
    val files: List<ExportedDownloadFile> = emptyList(),
    val progress: DownloadCenterProgressSnapshot = DownloadCenterProgressSnapshot(),
    val errorMessage: String? = null,
    val enqueuedAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = enqueuedAtMillis,
) {
    val title: String
        get() = "$gameTitle / $itemTitle"
}

data class DownloadCenterUiState(
    val tasks: List<DownloadCenterTaskUiState> = emptyList(),
) {
    val displayTasks: List<DownloadCenterTaskUiState>
        get() = tasks.sortedWith(
            compareBy<DownloadCenterTaskUiState> { it.status.sortOrder() }
                .thenByDescending(DownloadCenterTaskUiState::displayOrderTimestamp)
                .thenByDescending(DownloadCenterTaskUiState::enqueuedAtMillis)
                .thenByDescending(DownloadCenterTaskUiState::updatedAtMillis),
        )

    val queuedCount: Int
        get() = tasks.count { it.status == DownloadCenterTaskStatus.Queued }

    val runningCount: Int
        get() = tasks.count { it.status == DownloadCenterTaskStatus.Running }

    val pausedCount: Int
        get() = tasks.count { it.status == DownloadCenterTaskStatus.Paused }

    val finishedCount: Int
        get() = tasks.count { it.status == DownloadCenterTaskStatus.Success || it.status == DownloadCenterTaskStatus.Failed }

    val activeCount: Int
        get() = queuedCount + runningCount

    val activeTasks: List<DownloadCenterTaskUiState>
        get() = displayTasks.filter {
            it.status == DownloadCenterTaskStatus.Running ||
                it.status == DownloadCenterTaskStatus.Queued ||
                it.status == DownloadCenterTaskStatus.Paused
        }

    val historyTasks: List<DownloadCenterTaskUiState>
        get() = displayTasks.filter {
            it.status == DownloadCenterTaskStatus.Success ||
                it.status == DownloadCenterTaskStatus.Failed
        }
}

fun DownloadCenterTaskUiState.canPause(): Boolean =
    status == DownloadCenterTaskStatus.Queued || status == DownloadCenterTaskStatus.Running

fun DownloadCenterTaskUiState.canResume(): Boolean =
    status == DownloadCenterTaskStatus.Paused || status == DownloadCenterTaskStatus.Failed

fun DownloadCenterTaskUiState.resumeActionLabel(context: Context): String =
    when (status) {
        DownloadCenterTaskStatus.Failed -> context.getString(R.string.status_retry_download)
        DownloadCenterTaskStatus.Paused -> context.getString(R.string.status_resume_download)
        else -> context.getString(R.string.status_resume_download)
    }

fun DownloadCenterTaskUiState.removeActionLabel(context: Context): String =
    when (status) {
        DownloadCenterTaskStatus.Queued,
        DownloadCenterTaskStatus.Running,
        DownloadCenterTaskStatus.Paused,
        -> context.getString(R.string.btn_cancel)

        DownloadCenterTaskStatus.Success,
        DownloadCenterTaskStatus.Failed,
        -> context.getString(R.string.btn_delete)
    }

fun DownloadCenterTaskUiState.hasDeterminateProgress(): Boolean =
    status == DownloadCenterTaskStatus.Success ||
        progress.totalBytes != null ||
        progress.totalChunks != null ||
        progress.totalFiles != null

fun DownloadCenterTaskUiState.shouldAnimateProgress(): Boolean =
    status == DownloadCenterTaskStatus.Running && !hasDeterminateProgress()

fun DownloadCenterTaskUiState.progressFraction(): Float =
    when (status) {
        DownloadCenterTaskStatus.Queued -> 0f
        DownloadCenterTaskStatus.Success -> 1f
        DownloadCenterTaskStatus.Running,
        DownloadCenterTaskStatus.Paused,
        DownloadCenterTaskStatus.Failed,
        -> progress.fraction ?: 0f
    }

fun DownloadCenterTaskUiState.summaryText(context: Context): String =
    when (status) {
        DownloadCenterTaskStatus.Queued ->
            context.getString(R.string.task_summary_queued, boundAccountName)

        DownloadCenterTaskStatus.Running -> listOfNotNull(
            phase.displayName(context),
            progress.percentText(),
            progress.bytesText(),
            context.getString(R.string.metric_account, boundAccountName),
        ).joinToString(" · ").ifBlank { context.getString(R.string.status_downloading) }

        DownloadCenterTaskStatus.Paused -> listOfNotNull(
            context.getString(R.string.status_paused),
            progress.percentText(),
            progress.bytesText(),
            context.getString(R.string.metric_account, boundAccountName),
        ).joinToString(" · ").ifBlank { context.getString(R.string.task_summary_paused) }

        DownloadCenterTaskStatus.Success ->
            context.getString(R.string.task_summary_success, boundAccountName)

        DownloadCenterTaskStatus.Failed -> listOfNotNull(
            errorMessage ?: context.getString(R.string.task_summary_failed_retry),
            context.getString(R.string.metric_account, boundAccountName),
        ).joinToString(" · ")
    }

fun DownloadCenterTaskUiState.statusLabel(context: Context): String =
    status.displayName(context)

fun DownloadCenterTaskUiState.phaseLabel(context: Context): String =
    phase.displayName(context)

fun DownloadCenterTaskUiState.progressDetails(context: Context): List<String> =
    buildList {
        progress.percentText()?.let { add(context.getString(R.string.metric_total_progress, it)) }
        progress.bytesText()?.let { add(context.getString(R.string.metric_data, it)) }
        progress.chunkText()?.let { add(context.getString(R.string.metric_chunks, it)) }
        progress.fileText()?.let { add(context.getString(R.string.metric_file, it)) }
        progress.speedText()?.let { add(context.getString(R.string.metric_speed, it)) }
    }

private val DownloadCenterProgressSnapshot.fraction: Float?
    get() = when {
        totalBytes != null && totalBytes > 0L -> (writtenBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
        totalChunks != null && totalChunks > 0 -> (completedChunks.toFloat() / totalChunks.toFloat()).coerceIn(0f, 1f)
        totalFiles != null && totalFiles > 0 -> (completedFiles.toFloat() / totalFiles.toFloat()).coerceIn(0f, 1f)
        else -> null
    }

private fun DownloadCenterProgressSnapshot.percentText(): String? =
    fraction?.let { value ->
        "${DecimalFormat("0.0").format(value * 100f)}%"
    }

private fun DownloadCenterProgressSnapshot.bytesText(): String? =
    when {
        writtenBytes > 0L && totalBytes != null && totalBytes > 0L ->
            "${formatBinaryFileSize(writtenBytes)} / ${formatBinaryFileSize(totalBytes)}"

        writtenBytes > 0L -> formatBinaryFileSize(writtenBytes)
        totalBytes != null && totalBytes > 0L -> "0 B / ${formatBinaryFileSize(totalBytes)}"
        else -> null
    }

private fun DownloadCenterProgressSnapshot.chunkText(): String? =
    totalChunks?.let { total -> "${completedChunks.coerceAtMost(total)} / $total" }

private fun DownloadCenterProgressSnapshot.fileText(): String? =
    totalFiles?.let { total -> "${completedFiles.coerceAtMost(total)} / $total" }

private fun DownloadCenterProgressSnapshot.speedText(): String? =
    speedBytesPerSecond?.takeIf { it > 0L }?.let { speed ->
        "${formatBinaryFileSize(speed)}/s"
    }

private fun DownloadState.displayName(context: Context): String =
    when (this) {
        DownloadState.Idle -> context.getString(R.string.status_waiting)
        DownloadState.Resolving -> context.getString(R.string.phase_resolving)
        DownloadState.Connecting -> context.getString(R.string.phase_connecting)
        DownloadState.Downloading -> context.getString(R.string.status_downloading)
        DownloadState.Paused -> context.getString(R.string.status_paused)
        DownloadState.Success -> context.getString(R.string.status_completed)
        DownloadState.Failed -> context.getString(R.string.status_failed)
    }

fun DownloadCenterTaskStatus.displayName(context: Context): String =
    when (this) {
        DownloadCenterTaskStatus.Queued -> context.getString(R.string.status_queued)
        DownloadCenterTaskStatus.Running -> context.getString(R.string.status_downloading)
        DownloadCenterTaskStatus.Paused -> context.getString(R.string.status_paused)
        DownloadCenterTaskStatus.Success -> context.getString(R.string.status_completed)
        DownloadCenterTaskStatus.Failed -> context.getString(R.string.status_failed)
    }

private fun DownloadCenterTaskStatus.sortOrder(): Int =
    when (this) {
        DownloadCenterTaskStatus.Running -> 0
        DownloadCenterTaskStatus.Queued -> 1
        DownloadCenterTaskStatus.Paused -> 2
        DownloadCenterTaskStatus.Failed -> 3
        DownloadCenterTaskStatus.Success -> 4
    }

private fun DownloadCenterTaskUiState.displayOrderTimestamp(): Long =
    when (status) {
        DownloadCenterTaskStatus.Running,
        DownloadCenterTaskStatus.Queued,
        DownloadCenterTaskStatus.Paused,
        -> enqueuedAtMillis

        DownloadCenterTaskStatus.Success,
        DownloadCenterTaskStatus.Failed,
        -> updatedAtMillis
    }

package top.apricityx.workshop

import android.content.Context
import kotlin.math.roundToInt

data class DownloadForegroundNotificationSnapshot(
    val isActive: Boolean,
    val title: String = "",
    val text: String = "",
    val subText: String? = null,
    val lines: List<String> = emptyList(),
    val progress: Int = 0,
    val progressMax: Int = 100,
    val progressIndeterminate: Boolean = true,
)

fun DownloadCenterUiState.toForegroundNotificationSnapshot(context: Context): DownloadForegroundNotificationSnapshot {
    val foregroundTasks = displayTasks.filter {
        it.status == DownloadCenterTaskStatus.Running || it.status == DownloadCenterTaskStatus.Queued
    }
    if (foregroundTasks.isEmpty()) {
        return DownloadForegroundNotificationSnapshot(isActive = false)
    }

    val primaryTask = foregroundTasks.firstOrNull { it.status == DownloadCenterTaskStatus.Running } ?: foregroundTasks.first()
    val indeterminate = primaryTask.status == DownloadCenterTaskStatus.Queued || !primaryTask.hasDeterminateProgress()
    val progress = if (indeterminate) {
        0
    } else {
        (primaryTask.progressFraction() * 100f).roundToInt().coerceIn(0, 100)
    }
    val lines = buildList {
        foregroundTasks.take(MAX_EXPANDED_LINES).forEach { task ->
            add("${task.itemTitle} · ${task.summaryText(context)}")
        }
        val remaining = foregroundTasks.size - MAX_EXPANDED_LINES
        if (remaining > 0) {
            add(context.getString(R.string.notif_more_tasks, remaining))
        }
    }

    return DownloadForegroundNotificationSnapshot(
        isActive = true,
        title = if (foregroundTasks.size == 1) {
            context.getString(R.string.notif_bg_downloading)
        } else {
            context.getString(R.string.notif_bg_downloading_count, foregroundTasks.size)
        },
        text = primaryTask.title,
        subText = buildForegroundCountSummary(
            context = context,
            runningCount = foregroundTasks.count { it.status == DownloadCenterTaskStatus.Running },
            queuedCount = foregroundTasks.count { it.status == DownloadCenterTaskStatus.Queued },
        ),
        lines = lines,
        progress = progress,
        progressIndeterminate = indeterminate,
    )
}

private fun buildForegroundCountSummary(
    context: Context,
    runningCount: Int,
    queuedCount: Int,
): String =
    listOfNotNull(
        runningCount.takeIf { it > 0 }?.let { context.getString(R.string.notif_running, it) },
        queuedCount.takeIf { it > 0 }?.let { context.getString(R.string.notif_queued, it) },
    ).joinToString(" · ").ifBlank { context.getString(R.string.notif_preparing) }

private const val MAX_EXPANDED_LINES = 4

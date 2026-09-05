package top.apricityx.workshop.ui.screen
import top.apricityx.workshop.R

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.apricityx.workshop.DownloadCenterTaskStatus
import top.apricityx.workshop.DownloadCenterTaskUiState
import top.apricityx.workshop.canPause
import top.apricityx.workshop.canResume
import top.apricityx.workshop.hasDeterminateProgress
import top.apricityx.workshop.phaseLabel
import top.apricityx.workshop.progressDetails
import top.apricityx.workshop.progressFraction
import top.apricityx.workshop.removeActionLabel
import top.apricityx.workshop.resumeActionLabel
import top.apricityx.workshop.shouldAnimateProgress
import top.apricityx.workshop.statusLabel
import top.apricityx.workshop.summaryText
import top.apricityx.workshop.ui.component.MetricFlow
import top.apricityx.workshop.ui.component.ScreenSummaryCard
import top.apricityx.workshop.ui.component.WorkshopOutlinedButton
import top.apricityx.workshop.ui.component.WorkshopPanelCard
import top.apricityx.workshop.ui.theme.isLiquidGlassFrontendEnabled
import top.apricityx.workshop.ui.theme.workshopChromePadding

@Composable
fun DownloadTaskDetailScreen(
    task: DownloadCenterTaskUiState,
    onPauseTask: () -> Unit,
    onResumeTask: () -> Unit,
    onRemoveTask: () -> Unit,
    onShareDebugLog: () -> Unit,
    onShareRuntimeLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .workshopChromePadding(topExtra = 8.dp, bottomExtra = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenSummaryCard(
            title = task.itemTitle,
            subtitle = task.gameTitle,
            metrics = listOf(
                task.statusLabel(context),
                stringResource(R.string.metric_phase, task.phaseLabel(context)),
                stringResource(R.string.metric_account, task.boundAccountName),
            ),
        ) {
            Text(
                text = task.summaryText(context),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            if (task.hasDeterminateProgress()) {
                LinearProgressIndicator(
                    progress = { task.progressFraction() },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else if (task.shouldAnimateProgress()) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                LinearProgressIndicator(
                    progress = { task.progressFraction() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            MetricFlow(metrics = task.progressDetails(context).take(4))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (task.canPause()) {
                    WorkshopOutlinedButton(onClick = onPauseTask) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = null,
                        )
                        Text(stringResource(R.string.btn_pause))
                    }
                }
                if (task.canResume()) {
                    WorkshopOutlinedButton(onClick = onResumeTask) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                        )
                        Text(" ${task.resumeActionLabel(context)}")
                    }
                }
                WorkshopOutlinedButton(onClick = onRemoveTask) {
                    Text(task.removeActionLabel(context))
                }
            }
        }

        task.errorMessage?.let {
            DownloadTaskFailureCard(
                message = it,
            )
        }

        SectionCard(title = stringResource(R.string.section_download_progress)) {
            task.progressDetails(context).forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (task.progressDetails(context).isEmpty()) {
                Text(stringResource(R.string.task_detail_no_detailed_progress), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (task.status == DownloadCenterTaskStatus.Success) {
            SectionCard(title = stringResource(R.string.section_file_management)) {
                Text(
                    text = if (task.files.isEmpty()) {
                        stringResource(R.string.task_detail_success_files_synced)
                    } else {
                        stringResource(R.string.task_detail_success_files, task.files.size)
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SectionCard(title = stringResource(R.string.section_logs)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.task_detail_logs_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                WorkshopOutlinedButton(onClick = onShareDebugLog) {
                    Text(stringResource(R.string.btn_share_debug_log))
                }
                WorkshopOutlinedButton(onClick = onShareRuntimeLog) {
                    Text(stringResource(R.string.btn_share_runtime_log))
                }
                if (task.logs.isEmpty()) {
                    Text(stringResource(R.string.task_detail_no_logs), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        task.logs.forEach { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadTaskFailureCard(
    message: String,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.35f
    val useTransparentCardStyle = isLiquidGlassFrontendEnabled()
    val containerColor = if (isDark) {
        Color(0xFF4A1F24).copy(alpha = 0.8f)
    } else {
        Color(0xFFFFE0E0)
    }
    val borderColor = if (isDark) {
        Color(0xFFFF9B9B).copy(alpha = 0.34f)
    } else {
        Color(0xFFF0A5A5)
    }
    val contentColor = if (isDark) {
        Color(0xFFFFE0E0)
    } else {
        Color(0xFF7F2A2A)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = containerColor.copy(
            alpha = if (useTransparentCardStyle) {
                if (isDark) 0.28f else 0.32f
            } else {
                if (isDark) 0.42f else 0.5f
            },
        ),
        border = BorderStroke(
            1.dp,
            borderColor.copy(
                alpha = if (useTransparentCardStyle) {
                    if (isDark) 0.28f else 0.36f
                } else {
                    if (isDark) 0.3f else 0.42f
                },
            ),
        ),
        tonalElevation = if (useTransparentCardStyle) 0.dp else 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.task_detail_failed_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    WorkshopPanelCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            content()
        }
    }
}


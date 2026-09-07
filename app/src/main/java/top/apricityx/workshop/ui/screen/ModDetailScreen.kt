package top.apricityx.workshop.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.ArrayDeque
import top.apricityx.workshop.R
import top.apricityx.workshop.DownloadedModEntry
import top.apricityx.workshop.DownloadedModGroup
import top.apricityx.workshop.ExportedDownloadFile
import top.apricityx.workshop.ModLibraryDescriptionTranslationUiState
import top.apricityx.workshop.ModUpdateCheckResult
import top.apricityx.workshop.ModUpdateCheckStatus
import top.apricityx.workshop.formatBinaryFileSize
import top.apricityx.workshop.latestVersionOrNull
import top.apricityx.workshop.modLibraryKey
import top.apricityx.workshop.primaryFile
import top.apricityx.workshop.storedVersions
import top.apricityx.workshop.totalFileCount
import top.apricityx.workshop.trackingEntryOrNull
import top.apricityx.workshop.updateReferenceEntry
import top.apricityx.workshop.ui.component.MessageTone
import top.apricityx.workshop.ui.component.MetricFlow
import top.apricityx.workshop.ui.component.ModPreviewImage
import top.apricityx.workshop.ui.component.ModUpdateStatusText
import top.apricityx.workshop.ui.component.ScreenSummaryCard
import top.apricityx.workshop.ui.component.WorkshopMessageBanner
import top.apricityx.workshop.ui.component.WorkshopButton
import top.apricityx.workshop.ui.component.WorkshopDestructiveButton
import top.apricityx.workshop.ui.component.WorkshopOutlinedButton
import top.apricityx.workshop.ui.component.WorkshopPanelCard
import top.apricityx.workshop.ui.component.formatModLibraryTimestamp
import top.apricityx.workshop.ui.theme.workshopChromePadding
import top.apricityx.workshop.versionCount
import top.apricityx.workshop.versionLabel

@Composable
fun ModDetailScreen(
    group: DownloadedModGroup,
    descriptionTranslationState: ModLibraryDescriptionTranslationUiState,
    updateResults: Map<String, ModUpdateCheckResult>,
    onTranslateDescription: () -> Unit,
    onRenameMod: () -> Unit,
    onOpenFile: (ExportedDownloadFile) -> Unit,
    onShareFile: (ExportedDownloadFile) -> Unit,
    onUpdateMod: (DownloadedModEntry) -> Unit,
    onRemoveMod: (DownloadedModEntry) -> Unit,
    onViewChangeNotes: (DownloadedModGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestVersion = group.latestVersionOrNull()
    val trackingEntry = group.trackingEntryOrNull()
    val latestUpdateResult = updateResults[group.updateReferenceEntry().modLibraryKey()]
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .workshopChromePadding(topExtra = 8.dp, bottomExtra = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenSummaryCard(
            title = group.itemTitle,
            subtitle = group.gameTitle,
            metrics = listOf(
                stringResource(R.string.app_id_format, group.appId.toString()),
                stringResource(R.string.mod_id_metric_format, group.publishedFileId.toString()),
                stringResource(R.string.versions_count_metric_format, group.versionCount()),
                stringResource(R.string.files_count_metric_format, group.totalFileCount()),
            ),
        ) {
            ModPreviewImage(
                previewImagePath = group.previewImagePath,
                contentDescription = group.itemTitle,
            )
            val latestPrimaryFile = latestVersion?.primaryFile()
            if (latestPrimaryFile != null) {
                Text(
                    text = stringResource(R.string.latest_primary_file_format, latestPrimaryFile.relativePath),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            ModUpdateStatusText(result = latestUpdateResult)
            WorkshopOutlinedButton(
                onClick = onRenameMod,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.rename_mod_title))
            }
            if (group.versionCount() > 1) {
                Text(
                    text = stringResource(R.string.saved_versions_all_format, group.versionCount()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (group.versionCount() == 0) {
                Text(
                    text = stringResource(R.string.tracking_only_no_versions),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                trackingEntry?.let { entry ->
                    WorkshopButton(
                        onClick = { onUpdateMod(entry) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        CompositionLocalProvider(LocalContentColor provides Color.Black) {
                            Text(
                                text = stringResource(R.string.download_latest_version),
                                color = Color.Black,
                            )
                        }
                    }
                    WorkshopDestructiveButton(
                        onClick = { onRemoveMod(entry) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.delete_this_mod))
                    }
                }
            }
        }

        if (group.description.isNotBlank() || group.changeNotesFetched || group.changeNotes.isNotBlank()) {
            WorkshopPanelCard {
                Text(
                    text = stringResource(R.string.description_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (descriptionTranslationState.translatedDescription != null) {
                    Text(
                        text = stringResource(R.string.original_text),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                SelectionContainer {
                    Text(
                        text = group.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                WorkshopOutlinedButton(
                    onClick = onTranslateDescription,
                    enabled = !descriptionTranslationState.isTranslatingDescription,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (descriptionTranslationState.isTranslatingDescription) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                        Text(stringResource(R.string.translating_introduction))
                    } else {
                        Text(
                            if (descriptionTranslationState.translatedDescription == null) {
                                stringResource(R.string.translate_introduction)
                            } else {
                                stringResource(R.string.retranslate_introduction)
                            },
                        )
                    }
                }
                WorkshopOutlinedButton(
                    onClick = { onViewChangeNotes(group) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.view_change_notes))
                }
                descriptionTranslationState.translationErrorMessage?.let { translationErrorMessage ->
                    WorkshopMessageBanner(
                        message = translationErrorMessage,
                        tone = MessageTone.Error,
                    )
                }
                val translatedDescription = descriptionTranslationState.translatedDescription?.takeIf(String::isNotBlank)
                if (translatedDescription != null) {
                    Text(
                        text = stringResource(R.string.translated_text),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    SelectionContainer {
                        Text(
                            text = translatedDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        group.storedVersions().forEach { entry ->
            ModVersionPanel(
                entry = entry,
                updateResult = updateResults[entry.modLibraryKey()],
                onOpenFile = onOpenFile,
                onShareFile = onShareFile,
                onUpdateMod = { onUpdateMod(entry) },
                onRemoveMod = { onRemoveMod(entry) },
            )
        }
    }
}

@Composable
private fun ModVersionPanel(
    entry: DownloadedModEntry,
    updateResult: ModUpdateCheckResult?,
    onOpenFile: (ExportedDownloadFile) -> Unit,
    onShareFile: (ExportedDownloadFile) -> Unit,
    onUpdateMod: () -> Unit,
    onRemoveMod: () -> Unit,
) {
    val orderedFiles = remember(entry.files) {
        entry.files.breadthFirstDisplayOrder()
    }
    val pagedFiles = remember(orderedFiles) {
        orderedFiles.chunked(MOD_DETAIL_FILES_PER_PAGE)
    }
    var requestedPageIndex by rememberSaveable(entry.modLibraryKey(), orderedFiles.size) {
        mutableIntStateOf(0)
    }
    val currentPageIndex = requestedPageIndex.coerceIn(0, (pagedFiles.size - 1).coerceAtLeast(0))
    val currentPageFiles = if (pagedFiles.isEmpty()) {
        emptyList()
    } else {
        pagedFiles[currentPageIndex]
    }

    WorkshopPanelCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = entry.versionLabel(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        MetricFlow(
            metrics = listOf(
                stringResource(R.string.files_count_metric_format, entry.files.size),
                stringResource(R.string.synced_metric_format, formatModLibraryTimestamp(entry.storedAtMillis)),
            ),
        )
        val primaryFile = entry.primaryFile()
        if (primaryFile != null) {
            Text(
                text = stringResource(R.string.primary_file_format, primaryFile.relativePath),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ModUpdateStatusText(result = updateResult)
        if (updateResult?.status == ModUpdateCheckStatus.UpdateAvailable) {
            WorkshopButton(
                onClick = onUpdateMod,
                modifier = Modifier.fillMaxWidth(),
            ) {
                CompositionLocalProvider(LocalContentColor provides Color.Black) {
                    Text(
                        text = stringResource(R.string.update_to_latest_version),
                        color = Color.Black,
                    )
                }
            }
        }

        HorizontalDivider()

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (orderedFiles.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_actionable_files),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                if (pagedFiles.size > 1) {
                    Text(
                        text = stringResource(R.string.files_page_format, currentPageIndex + 1, pagedFiles.size, MOD_DETAIL_FILES_PER_PAGE),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                currentPageFiles.forEach { file ->
                    FileRow(
                        file = file,
                        sizeText = formatBinaryFileSize(file.sizeBytes),
                        onOpenFile = { onOpenFile(file) },
                        onShareFile = { onShareFile(file) },
                    )
                }
                if (pagedFiles.size > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WorkshopOutlinedButton(
                            onClick = { requestedPageIndex = (currentPageIndex - 1).coerceAtLeast(0) },
                            enabled = currentPageIndex > 0,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.previous_page))
                        }
                        WorkshopOutlinedButton(
                            onClick = {
                                requestedPageIndex = (currentPageIndex + 1)
                                    .coerceAtMost(pagedFiles.lastIndex)
                            },
                            enabled = currentPageIndex < pagedFiles.lastIndex,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.next_page))
                        }
                    }
                }
            }
        }

        WorkshopDestructiveButton(
            onClick = onRemoveMod,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.delete_this_version))
        }
    }
}

@Composable
private fun FileRow(
    file: ExportedDownloadFile,
    sizeText: String,
    onOpenFile: () -> Unit,
    onShareFile: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = file.relativePath,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        MetricFlow(
            metrics = listOf(
                sizeText,
                formatModLibraryTimestamp(file.modifiedEpochMillis),
            ),
        )
        Text(
            text = file.userVisiblePath,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WorkshopOutlinedButton(
                onClick = onOpenFile,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.open))
            }
            WorkshopOutlinedButton(
                onClick = onShareFile,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.share))
            }
        }
    }
}

private fun List<ExportedDownloadFile>.breadthFirstDisplayOrder(): List<ExportedDownloadFile> {
    if (isEmpty()) {
        return emptyList()
    }

    val root = FileTreeNode()
    forEach { file ->
        val normalizedPath = file.relativePath.replace('\\', '/')
        val segments = normalizedPath.split('/').filter(String::isNotBlank)
        if (segments.isEmpty()) {
            root.files += file
            return@forEach
        }

        var current = root
        segments.dropLast(1).forEach { directoryName ->
            current = current.children.getOrPut(directoryName) { FileTreeNode() }
        }
        current.files += file
    }

    val queue = ArrayDeque<FileTreeNode>()
    val orderedFiles = mutableListOf<ExportedDownloadFile>()
    queue.add(root)
    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        orderedFiles += current.files.sortedWith(
            compareBy<ExportedDownloadFile>(
                { it.relativePath.substringAfterLast('/').lowercase() },
                { it.relativePath.lowercase() },
            ),
        )
        current.children
            .toSortedMap(String.CASE_INSENSITIVE_ORDER)
            .values
            .forEach(queue::addLast)
    }
    return orderedFiles
}

private class FileTreeNode(
    val files: MutableList<ExportedDownloadFile> = mutableListOf(),
    val children: MutableMap<String, FileTreeNode> = linkedMapOf(),
)

private const val MOD_DETAIL_FILES_PER_PAGE = 5

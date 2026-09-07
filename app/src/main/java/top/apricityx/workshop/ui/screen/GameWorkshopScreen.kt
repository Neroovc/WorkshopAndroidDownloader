package top.apricityx.workshop.ui.screen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import top.apricityx.workshop.GameWorkshopUiState
import top.apricityx.workshop.WorkshopModStatus
import top.apricityx.workshop.WorkshopModStatusResolver
import top.apricityx.workshop.WorkshopPublishedFileIdParser
import top.apricityx.workshop.WorkshopBrowseSortOption
import top.apricityx.workshop.WorkshopBrowseTimeWindow
import top.apricityx.workshop.displayName
import top.apricityx.workshop.formatBinaryFileSize
import top.apricityx.workshop.data.WorkshopBrowseItem
import top.apricityx.workshop.ui.component.MessageTone
import top.apricityx.workshop.ui.component.MetricPill
import top.apricityx.workshop.ui.component.ScreenSummaryCard
import top.apricityx.workshop.ui.component.SectionHeading
import top.apricityx.workshop.ui.component.WorkshopCenteredState
import top.apricityx.workshop.ui.component.WorkshopGlassIconButton
import top.apricityx.workshop.ui.component.WorkshopLoadingBlock
import top.apricityx.workshop.ui.component.WorkshopMessageBanner
import top.apricityx.workshop.ui.component.WorkshopButton
import top.apricityx.workshop.ui.component.WorkshopDialog
import top.apricityx.workshop.ui.component.WorkshopOutlinedButton
import top.apricityx.workshop.ui.component.WorkshopOutlinedTextField
import top.apricityx.workshop.ui.component.WorkshopPanelCard
import top.apricityx.workshop.ui.theme.workshopListContentPadding

@Composable
fun GameWorkshopScreen(
    state: GameWorkshopUiState,
    modStatusResolver: WorkshopModStatusResolver,
    isBrowsingUnauthenticated: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSortOptionSelected: (WorkshopBrowseSortOption) -> Unit,
    onTimeWindowSelected: (WorkshopBrowseTimeWindow) -> Unit,
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenItemDetail: (WorkshopBrowseItem) -> Unit,
    onDownloadSingleItem: (WorkshopBrowseItem) -> Unit,
    onDismissDirectDownloadDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberSaveable(
        state.game.appId.toString(),
        saver = LazyListState.Saver,
    ) {
        LazyListState()
    }
    var directPublishedFileIdText by rememberSaveable(state.game.appId.toString()) {
        mutableStateOf("")
    }
    val showingRefreshState = state.isLoading && state.items.isNotEmpty()
    val directPublishedFileId = WorkshopPublishedFileIdParser.parse(directPublishedFileIdText)
    val canDirectDownload = directPublishedFileId != null
    val directModStatus = directPublishedFileId?.let { publishedFileId ->
        modStatusResolver.resolve(appId = state.game.appId, publishedFileId = publishedFileId)
    } ?: WorkshopModStatus.NotDownloaded

    if (state.showDirectDownloadDialog) {
        DirectPublishedIdDownloadDialog(
            directPublishedFileIdText = directPublishedFileIdText,
            modStatus = directModStatus,
            canDirectDownload = canDirectDownload,
            onPublishedFileIdChange = { value -> directPublishedFileIdText = value },
            onDismiss = onDismissDirectDownloadDialog,
            onDownload = {
                val publishedFileId = directPublishedFileId ?: return@DirectPublishedIdDownloadDialog
                onDownloadSingleItem(
                    WorkshopBrowseItem(
                        appId = state.game.appId,
                        publishedFileId = publishedFileId,
                        title = "Workshop $publishedFileId",
                        authorName = "",
                        previewImageUrl = "",
                        descriptionSnippet = "",
                    ),
                )
                onDismissDirectDownloadDialog()
            },
        )
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = workshopListContentPadding(topExtra = 20.dp, bottomExtra = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            val defaultSubtitle = stringResource(R.string.game_supports_workshop)
            val appIdMetric = stringResource(R.string.app_id_format, state.game.appId.toString())
            val loadedModsMetric = stringResource(R.string.loaded_mods_metric, state.items.size)
            val sortMetric = stringResource(R.string.sort_metric_format, state.selectedSortOption.displayName())
            val timeWindowMetric = if (state.selectedSortOption.supportsTimeWindow) {
                stringResource(R.string.workshop_time_window_format, state.selectedTimeWindow.displayName())
            } else {
                null
            }
            val searchingMetric = if (state.searchQuery.isNotBlank()) {
                stringResource(R.string.searching)
            } else {
                null
            }
            ScreenSummaryCard(
                title = state.game.name,
                subtitle = state.game.shortDescription.ifBlank { defaultSubtitle },
                metrics = buildList {
                    add(appIdMetric)
                    add(loadedModsMetric)
                    add(sortMetric)
                    timeWindowMetric?.let { add(it) }
                    searchingMetric?.let { add(it) }
                },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                AsyncImage(
                    model = state.game.headerImageUrl.ifBlank { state.game.capsuleImageUrl },
                    contentDescription = state.game.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(188.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    WorkshopOutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = { Text(stringResource(R.string.search_mods)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        modifier = Modifier.weight(1f),
                    )
                    WorkshopButton(onClick = onSearch, modifier = Modifier.padding(top = 8.dp)) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                }

                WorkshopBrowseSortControls(
                    state = state,
                    onSortOptionSelected = onSortOptionSelected,
                    onTimeWindowSelected = onTimeWindowSelected,
                )
            }
        }

        if (isBrowsingUnauthenticated) {
            item {
                WorkshopPanelCard {
                    Text(
                        text = stringResource(R.string.browsing_unauthenticated_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.browsing_unauthenticated_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (state.showConnectionErrorState) {
            item {
                WorkshopCenteredState(
                    title = stringResource(R.string.connection_timeout_title),
                    message = state.message
                        ?: stringResource(R.string.connection_timeout_default_message),
                    actionLabel = stringResource(R.string.retry),
                    onAction = if (state.retryLoadMoreOnError) onLoadMore else onSearch,
                )
            }
        } else {
            state.message?.let { message ->
                item {
                    WorkshopMessageBanner(
                        message = message,
                        tone = if (message.contains("失败")) MessageTone.Error else MessageTone.Info,
                    )
                }
            }

            item {
                SectionHeading(
                    title = stringResource(R.string.workshop_mods_title),
                    subtitle = if (state.searchQuery.isBlank()) {
                        stringResource(R.string.browse_public_workshop)
                    } else {
                        stringResource(R.string.current_search_format, state.searchQuery)
                    },
                )
            }

            if (state.isLoading && state.items.isEmpty()) {
                item {
                    WorkshopLoadingBlock(label = stringResource(R.string.loading_workshop_list))
                }
            } else if (state.items.isEmpty()) {
                item {
                    WorkshopCenteredState(
                        title = if (state.searchQuery.isBlank()) {
                            stringResource(R.string.no_mods_available)
                        } else {
                            stringResource(R.string.no_results_found)
                        },
                        message = if (state.searchQuery.isBlank()) {
                            state.message ?: stringResource(R.string.no_mods_message)
                        } else {
                            state.message ?: stringResource(R.string.try_different_keyword)
                        },
                    )
                }
            } else {
                if (showingRefreshState) {
                    item {
                        WorkshopMessageBanner(
                            message = stringResource(R.string.refreshing_list_message),
                            tone = MessageTone.Info,
                        )
                    }
                }

                items(state.items, key = { it.publishedFileId.toString() }) { item ->
                    val modStatus = modStatusResolver.resolve(item)
                    WorkshopItemCard(
                        item = item,
                        modStatus = modStatus,
                        onOpenDetail = { onOpenItemDetail(item) },
                        onDownload = { onDownloadSingleItem(item) },
                    )
                }
            }

            if (state.isLoadingMore) {
                item {
                    WorkshopLoadingBlock(label = stringResource(R.string.loading_more_mods))
                }
            } else if (state.hasNextPage) {
                item {
                    WorkshopOutlinedButton(
                        onClick = onLoadMore,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.load_more))
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectPublishedIdDownloadDialog(
    directPublishedFileIdText: String,
    modStatus: WorkshopModStatus,
    canDirectDownload: Boolean,
    onPublishedFileIdChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
) {
    WorkshopDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.direct_download_dialog_title)) },
        buttons = {
            WorkshopOutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
            WorkshopButton(
                onClick = {
                    if (canDirectDownload && modStatus.isDownloadActionEnabled()) {
                        onDownload()
                    }
                },
                enabled = canDirectDownload && modStatus.isDownloadActionEnabled(),
            ) {
                Text(modStatus.actionLabel())
            }
        },
    ) {
        Text(
            text = stringResource(R.string.direct_download_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        WorkshopOutlinedTextField(
            value = directPublishedFileIdText,
            onValueChange = onPublishedFileIdChange,
            label = { Text(stringResource(R.string.enter_published_id_or_link)) },
            supportingText = {
                Text(stringResource(R.string.published_id_example))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (directPublishedFileIdText.isNotBlank() && !canDirectDownload) {
            Text(
                text = stringResource(R.string.invalid_published_id),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        when {
            !canDirectDownload -> Unit
            modStatus == WorkshopModStatus.NotDownloaded -> Unit
            modStatus == WorkshopModStatus.Downloading -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DownloadingAnimatedIcon()
                    Text(stringResource(R.string.mod_downloading))
                }
            }
            else -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = modStatus.actionIcon(),
                        contentDescription = null,
                    )
                    Text(
                        when (modStatus) {
                            WorkshopModStatus.LatestDownloaded -> stringResource(R.string.mod_latest_local)
                            WorkshopModStatus.UpdateAvailable -> stringResource(R.string.mod_update_available)
                            WorkshopModStatus.NotDownloaded,
                            WorkshopModStatus.Downloading,
                            -> ""
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkshopBrowseSortControls(
    state: GameWorkshopUiState,
    onSortOptionSelected: (WorkshopBrowseSortOption) -> Unit,
    onTimeWindowSelected: (WorkshopBrowseTimeWindow) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.sort_by),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WorkshopBrowseSortOption.entries.forEach { option ->
                WorkshopBrowseSelectionChip(
                    label = option.displayName(),
                    selected = option == state.selectedSortOption,
                    onClick = { onSortOptionSelected(option) },
                )
            }
        }

        if (state.selectedSortOption.supportsTimeWindow) {
            Text(
                text = stringResource(R.string.popular_range),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WorkshopBrowseTimeWindow.entries.forEach { option ->
                    WorkshopBrowseSelectionChip(
                        label = option.displayName(),
                        selected = option == state.selectedTimeWindow,
                        onClick = { onTimeWindowSelected(option) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkshopBrowseSelectionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.35f
    val containerColor = when {
        selected && isDark -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        isDark -> MaterialTheme.colorScheme.surface.copy(alpha = 0.22f)
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    }
    val borderColor = when {
        selected && isDark -> MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
        isDark -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    }
    val contentColor = when {
        selected && isDark -> Color(0xFFEAF4FF)
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
        )
    }
}

@Composable
private fun WorkshopItemCard(
    item: WorkshopBrowseItem,
    modStatus: WorkshopModStatus,
    onOpenDetail: () -> Unit,
    onDownload: () -> Unit,
) {
    val fileSizeBytes = item.fileSizeBytes
    val sizeLabel = if (fileSizeBytes != null) {
        stringResource(R.string.size_metric_format, formatBinaryFileSize(fileSizeBytes))
    } else {
        null
    }

    WorkshopPanelCard(
        modifier = Modifier.clickable(onClick = onOpenDetail),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            AsyncImage(
                model = item.previewImageUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(104.dp)
                    .clip(MaterialTheme.shapes.medium),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.by_author_format, item.authorName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                sizeLabel?.let { label ->
                    MetricPill(text = label)
                }
                if (item.descriptionSnippet.isNotBlank()) {
                    Text(
                        text = item.descriptionSnippet,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            WorkshopDownloadActionButton(
                modStatus = modStatus,
                onClick = onDownload,
                modifier = Modifier.align(Alignment.Top),
            )
        }
    }
}

@Composable
private fun WorkshopDownloadActionButton(
    modStatus: WorkshopModStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WorkshopGlassIconButton(
        onClick = onClick,
        imageVector = modStatus.actionIcon(),
        contentDescription = modStatus.actionLabel(),
        modifier = modifier,
        enabled = modStatus.isDownloadActionEnabled(),
        content = when (modStatus) {
            WorkshopModStatus.Downloading -> {
                {
                    DownloadingAnimatedIcon()
                }
            }

            WorkshopModStatus.LatestDownloaded -> {
                {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                    )
                }
            }

            WorkshopModStatus.UpdateAvailable,
            WorkshopModStatus.NotDownloaded,
            -> null
        },
    )
}

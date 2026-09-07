package top.apricityx.workshop.ui.screen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import top.apricityx.workshop.R
import top.apricityx.workshop.WorkshopModStatus
import top.apricityx.workshop.WorkshopItemDetailUiState
import top.apricityx.workshop.formatBinaryFileSize
import top.apricityx.workshop.workshopChangeNotesUrl
import top.apricityx.workshop.data.WorkshopBrowseItem
import top.apricityx.workshop.data.WorkshopComment
import top.apricityx.workshop.data.WorkshopRequiredItem
import top.apricityx.workshop.ui.component.WorkshopChangeNotesDialog
import top.apricityx.workshop.ui.component.MessageTone
import top.apricityx.workshop.ui.component.MetricPill
import top.apricityx.workshop.ui.component.MetricFlow
import top.apricityx.workshop.ui.component.ScreenSummaryCard
import top.apricityx.workshop.ui.component.WorkshopButton
import top.apricityx.workshop.ui.component.WorkshopCenteredState
import top.apricityx.workshop.ui.component.WorkshopLoadingBlock
import top.apricityx.workshop.ui.component.WorkshopMessageBanner
import top.apricityx.workshop.ui.component.WorkshopOutlinedButton
import top.apricityx.workshop.ui.component.WorkshopPanelCard
import top.apricityx.workshop.ui.theme.workshopChromePadding

@Composable
internal fun WorkshopItemDetailScreen(
    state: WorkshopItemDetailUiState,
    downloadedItemIds: Set<ULong>,
    isInModLibrary: Boolean,
    modStatus: WorkshopModStatus,
    onRetry: () -> Unit,
    onRetryComments: () -> Unit,
    onLoadPreviousCommentsPage: () -> Unit,
    onLoadNextCommentsPage: () -> Unit,
    onTranslateDescription: () -> Unit,
    onAddToLibrary: () -> Unit,
    onDownload: (WorkshopBrowseItem) -> Unit,
    onViewDownloadedMod: (WorkshopBrowseItem) -> Unit,
    onOpenRequiredItem: (WorkshopBrowseItem) -> Unit,
    onOpenExternalUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val detail = state.detail
    val description = detail?.description?.ifBlank { state.item.descriptionSnippet }.orEmpty()
    val canTranslateDescription = detail != null && description.isNotBlank()
    val changeNotes = detail?.changeNotes.orEmpty()
    val changeNotesUrl = detail?.changeNotesUrl ?: workshopChangeNotesUrl(state.item.publishedFileId)
    var showChangeNotesDialog by remember(
        state.item.publishedFileId,
        changeNotes,
    ) {
        mutableStateOf(false)
    }
    val authorMetricLabel = stringResource(R.string.metric_author)
    val subscriptionsMetricLabel = stringResource(R.string.metric_subscriptions)
    val viewsMetricLabel = stringResource(R.string.metric_views)
    val sizeMetricLabel = stringResource(R.string.metric_size)
    val prerequisitesMetricLabel = stringResource(R.string.metric_prerequisites)
    val commentsMetricLabel = stringResource(R.string.metric_comments)
    val summaryMetrics = buildList {
        add(
            WorkshopItemSummaryMetric(
                icon = Icons.Default.Person,
                label = authorMetricLabel,
                value = detail?.authorName ?: state.item.authorName,
            ),
        )
        detail?.subscriptions?.let {
            add(
                WorkshopItemSummaryMetric(
                    icon = Icons.Default.Download,
                    label = subscriptionsMetricLabel,
                    value = formatCount(it),
                ),
            )
        }
        detail?.views?.let {
            add(
                WorkshopItemSummaryMetric(
                    icon = Icons.Default.Visibility,
                    label = viewsMetricLabel,
                    value = formatCount(it),
                ),
            )
        }
        detail?.fileSizeBytes?.let {
            add(
                WorkshopItemSummaryMetric(
                    icon = Icons.Default.Storage,
                    label = sizeMetricLabel,
                    value = formatBinaryFileSize(it),
                ),
            )
        }
        detail?.requiredItems?.takeIf { requiredItems -> requiredItems.isNotEmpty() }?.let { requiredItems ->
            add(
                WorkshopItemSummaryMetric(
                    icon = Icons.Default.Extension,
                    label = prerequisitesMetricLabel,
                    value = requiredItems.size.toString(),
                ),
            )
        }
        detail?.commentCount?.let {
            add(
                WorkshopItemSummaryMetric(
                    icon = Icons.AutoMirrored.Filled.Comment,
                    label = commentsMetricLabel,
                    value = formatCount(it),
                ),
            )
        }
    }

    if (state.showConnectionErrorState) {
        WorkshopCenteredState(
            title = stringResource(R.string.connection_timeout_title),
            message = state.message
                ?: stringResource(R.string.connection_timeout_default_message),
            actionLabel = stringResource(R.string.retry),
            onAction = onRetry,
            modifier = modifier.workshopChromePadding(topExtra = 24.dp, bottomExtra = 24.dp),
        )
    } else {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .workshopChromePadding(topExtra = 8.dp, bottomExtra = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ScreenSummaryCard(
                title = detail?.title ?: state.item.title,
                subtitle = stringResource(R.string.published_file_id_format, state.item.publishedFileId.toString()),
            ) {
                if (summaryMetrics.isNotEmpty()) {
                    WorkshopItemSummaryMetricFlow(metrics = summaryMetrics)
                }
                WorkshopDetailHeaderImage(
                    thumbnailUrl = state.item.previewImageUrl,
                    fullImageUrl = detail?.previewImageUrl,
                    contentDescription = state.item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                )

                WorkshopOutlinedButton(
                    onClick = onAddToLibrary,
                    enabled = !isInModLibrary,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                    text = if (isInModLibrary) {
                        stringResource(R.string.already_in_mod_library)
                    } else {
                        stringResource(R.string.add_to_library)
                    },
                )
                }

                WorkshopButton(
                    onClick = {
                        if (modStatus.isViewActionEnabled()) {
                            onViewDownloadedMod(state.item)
                        } else if (modStatus.isDownloadActionEnabled()) {
                            onDownload(state.item)
                        }
                    },
                    enabled = modStatus.isViewActionEnabled() || modStatus.isDownloadActionEnabled(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    CompositionLocalProvider(LocalContentColor provides Color.Black) {
                        if (modStatus == WorkshopModStatus.Downloading) {
                            DownloadingAnimatedIcon(tint = Color.Black)
                        } else {
                            Icon(
                                imageVector = modStatus.actionIcon(),
                                contentDescription = null,
                                tint = Color.Black,
                            )
                        }
                        Text(
                            text = " ${modStatus.actionLabel()}",
                            color = Color.Black,
                        )
                    }
                }

                if (state.message != null) {
                    WorkshopOutlinedButton(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                        )
                        Text(" " + stringResource(R.string.retry_load_details))
                    }
                }
            }

            if (
                description.isNotBlank() ||
                canTranslateDescription ||
                detail != null ||
                state.translationErrorMessage != null ||
                !state.translatedDescription.isNullOrBlank()
            ) {
                WorkshopPanelCard {
                    Text(
                        text = stringResource(R.string.description_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (description.isNotBlank()) {
                        if (state.translatedDescription != null) {
                            Text(
                                text = stringResource(R.string.original_text),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        SelectionContainer {
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    if (canTranslateDescription) {
                        WorkshopOutlinedButton(
                            onClick = onTranslateDescription,
                            enabled = !state.isTranslatingDescription,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (state.isTranslatingDescription) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                )
                                Text(" " + stringResource(R.string.translating_description))
                            } else {
                                Text(
                                    if (state.translatedDescription == null) {
                                        stringResource(R.string.translate_description)
                                    } else {
                                        stringResource(R.string.retranslate_description)
                                    },
                                )
                            }
                        }
                    }

                    if (detail != null) {
                        WorkshopOutlinedButton(
                            onClick = {
                                if (changeNotes.isNotBlank()) {
                                    showChangeNotesDialog = true
                                } else {
                                    onOpenExternalUrl(changeNotesUrl)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.view_change_notes))
                        }
                    }

                    state.translationErrorMessage?.let { translationErrorMessage ->
                        WorkshopMessageBanner(
                            message = translationErrorMessage,
                            tone = MessageTone.Error,
                        )
                    }

                    state.translatedDescription?.takeIf(String::isNotBlank)?.let { translatedDescription ->
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

            if (state.isLoading) {
                WorkshopLoadingBlock(label = stringResource(R.string.loading_item_detail))
            }

            state.message?.let { message ->
                WorkshopMessageBanner(
                    message = stringResource(R.string.detail_message_retry_hint_format, message),
                    tone = MessageTone.Error,
                )
            }

            detail?.requiredItems?.takeIf { requiredItems -> requiredItems.isNotEmpty() }?.let { requiredItems ->
                WorkshopPanelCard {
                    Text(
                        text = stringResource(R.string.prerequisites_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.prerequisites_count_format, requiredItems.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    requiredItems.forEach { requiredItem ->
                        RequiredItemLine(
                            item = requiredItem,
                            isDownloaded = requiredItem.publishedFileId in downloadedItemIds,
                            onClick = { onOpenRequiredItem(requiredItem.toBrowseItem()) },
                        )
                    }
                }
            }

            detail?.let {
                WorkshopPanelCard {
                    Text(
                        text = stringResource(R.string.comments_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = when {
                            state.isLoadingComments && it.comments.isEmpty() ->
                                stringResource(R.string.comments_loading)
                            it.commentCount == 0L ->
                                stringResource(R.string.no_public_comments)
                            it.commentCount != null && it.commentTotalPages != null ->
                                stringResource(
                                    R.string.comments_page_total_format,
                                    it.commentPage,
                                    it.commentTotalPages,
                                    formatCount(it.commentCount),
                                )
                            it.commentCount != null ->
                                stringResource(
                                    R.string.comments_page_format,
                                    it.commentPage,
                                    formatCount(it.commentCount),
                                )
                            it.comments.isNotEmpty() ->
                                stringResource(
                                    R.string.comments_page_loaded_format,
                                    it.commentPage,
                                    it.comments.size,
                                )
                            else ->
                                stringResource(R.string.no_public_comments_hint)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    state.commentErrorMessage?.let { commentErrorMessage ->
                        WorkshopMessageBanner(
                            message = commentErrorMessage,
                            tone = MessageTone.Error,
                        )
                        WorkshopOutlinedButton(
                            onClick = onRetryComments,
                            enabled = !state.isLoadingComments,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                            )
                            Text(" " + stringResource(R.string.retry_load_comments))
                        }
                    }

                    if (state.isLoadingComments) {
                        CommentLoadingBlock(
                            message = if (it.comments.isEmpty()) {
                                stringResource(R.string.loading_shorthand)
                            } else {
                                stringResource(R.string.loading_comments)
                            },
                        )
                    }

                    if (it.commentTotalPages?.let { totalPages -> totalPages > 1 } == true ||
                        it.hasPreviousCommentPage ||
                        it.hasNextCommentPage
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            WorkshopOutlinedButton(
                                onClick = onLoadPreviousCommentsPage,
                                enabled = !state.isLoadingComments && it.hasPreviousCommentPage,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(stringResource(R.string.previous_page))
                            }
                            WorkshopOutlinedButton(
                                onClick = onLoadNextCommentsPage,
                                enabled = !state.isLoadingComments && it.hasNextCommentPage,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(stringResource(R.string.next_page))
                            }
                        }
                    }

                    it.comments.forEach { comment ->
                        CommentLine(comment = comment)
                    }

                    WorkshopOutlinedButton(
                        onClick = { onOpenExternalUrl(it.commentsUrl) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(R.string.open_steam_comments),
                        )
                    }
                }

                WorkshopPanelCard {
                    Text(
                        text = stringResource(R.string.mod_info_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    val updatedMetricFormat = stringResource(R.string.updated_metric_format)
                    val favoritedMetricFormat = stringResource(R.string.favorited_metric_format)
                    val tagsMetricFormat = stringResource(R.string.tags_metric_format)
                    MetricFlow(
                        metrics = listOfNotNull(
                            it.timeUpdatedEpochSeconds?.let(::formatUpdatedTime)?.let { value ->
                                String.format(updatedMetricFormat, value)
                            },
                            it.favorited?.let(::formatCount)?.let { value ->
                                String.format(favoritedMetricFormat, value)
                            },
                            it.tags.takeIf { tags -> tags.isNotEmpty() }?.let { tags ->
                                String.format(tagsMetricFormat, tags.size)
                            },
                        ),
                    )
                    DetailLine(
                        label = stringResource(R.string.file_size),
                        value = it.fileSizeBytes?.let(::formatBinaryFileSize) ?: stringResource(R.string.unknown),
                    )
                    DetailLine(
                        label = stringResource(R.string.updated_time),
                        value = it.timeUpdatedEpochSeconds?.let(::formatUpdatedTime) ?: stringResource(R.string.unknown),
                    )
                    DetailLine(
                        label = stringResource(R.string.subscription_count),
                        value = it.subscriptions?.let(::formatCount) ?: stringResource(R.string.unknown),
                    )
                    DetailLine(
                        label = stringResource(R.string.favorites_count),
                        value = it.favorited?.let(::formatCount) ?: stringResource(R.string.unknown),
                    )
                    DetailLine(
                        label = stringResource(R.string.views_count),
                        value = it.views?.let(::formatCount) ?: stringResource(R.string.unknown),
                    )
                    DetailLine(
                        label = stringResource(R.string.tags_label),
                        value = it.tags.takeIf { tags -> tags.isNotEmpty() }?.joinToString(" / ") ?: stringResource(R.string.no_tags),
                    )
                }
            }
        }
    }

    if (showChangeNotesDialog) {
        WorkshopChangeNotesDialog(
            title = detail?.title?.ifBlank { state.item.title } ?: state.item.title,
            markdown = changeNotes,
            onDismissRequest = { showChangeNotesDialog = false },
            onOpenExternalUrl = { onOpenExternalUrl(changeNotesUrl) },
        )
    }
}

@Composable
private fun CommentLoadingBlock(
    message: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
        )
        Text(
            text = " $message",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CommentLine(
    comment: WorkshopComment,
) {
    val timeUnknownLabel = stringResource(R.string.time_unknown)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = comment.authorName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                MetricPill(
                    text = comment.postedEpochSeconds
                        ?.let(::formatUpdatedTime)
                        ?: comment.postedDisplayText.ifBlank { timeUnknownLabel },
                )
            }
            SelectionContainer {
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun WorkshopDetailHeaderImage(
    thumbnailUrl: String,
    fullImageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.clip(MaterialTheme.shapes.large)) {
        if (thumbnailUrl.isNotBlank()) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        val resolvedFullImageUrl = fullImageUrl?.ifBlank { null }
        if (resolvedFullImageUrl != null && resolvedFullImageUrl != thumbnailUrl) {
            AsyncImage(
                model = resolvedFullImageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (thumbnailUrl.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun RequiredItemLine(
    item: WorkshopRequiredItem,
    isDownloaded: Boolean,
    onClick: () -> Unit,
) {
    val noDescriptionLabel = stringResource(R.string.no_description)
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isDownloaded) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (item.previewImageUrl.isNotBlank()) {
                AsyncImage(
                    model = item.previewImageUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(MaterialTheme.shapes.medium),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.no_cover),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (isDownloaded) {
                        MetricPill(text = stringResource(R.string.downloaded))
                    }
                }
                Text(
                    text = item.descriptionSnippet.ifBlank { noDescriptionLabel },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun formatUpdatedTime(epochSeconds: Long): String =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .format(
            Instant.ofEpochSecond(epochSeconds)
                .atZone(ZoneId.systemDefault()),
        )

private fun formatCount(value: Long): String = "%,d".format(value)

private data class WorkshopItemSummaryMetric(
    val icon: ImageVector,
    val label: String,
    val value: String,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkshopItemSummaryMetricFlow(
    metrics: List<WorkshopItemSummaryMetric>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        metrics.forEach { metric ->
            WorkshopItemSummaryMetricPill(metric = metric)
        }
    }
}

@Composable
private fun WorkshopItemSummaryMetricPill(
    metric: WorkshopItemSummaryMetric,
    modifier: Modifier = Modifier,
) {
    val isLiquidGlass = top.apricityx.workshop.ui.theme.isLiquidGlassFrontendEnabled()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.35f
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (isLiquidGlass) {
            MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.22f else 0.16f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = if (isLiquidGlass) {
            BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.14f else 0.1f),
            )
        } else {
            null
        },
        tonalElevation = if (isLiquidGlass) 0.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = metric.icon,
                contentDescription = metric.label,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = metric.value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

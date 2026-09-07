package top.apricityx.workshop.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.apricityx.workshop.R
import top.apricityx.workshop.LibraryErrorUiState
import top.apricityx.workshop.data.SteamGame
import top.apricityx.workshop.ui.component.GameShowcaseCard
import top.apricityx.workshop.ui.component.MessageTone
import top.apricityx.workshop.ui.component.ScreenSummaryCard
import top.apricityx.workshop.ui.component.SectionHeading
import top.apricityx.workshop.ui.component.WorkshopCenteredState
import top.apricityx.workshop.ui.component.WorkshopLoadingBlock
import top.apricityx.workshop.ui.component.WorkshopMessageBanner
import top.apricityx.workshop.ui.theme.workshopChromePadding
import top.apricityx.workshop.ui.theme.workshopListContentPadding

@Composable
fun LibraryScreen(
    games: List<SteamGame>,
    isLoading: Boolean,
    message: String?,
    error: LibraryErrorUiState?,
    onRetry: () -> Unit,
    onOpenGame: (SteamGame) -> Unit,
    onRemoveGame: (SteamGame) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading && games.isEmpty() -> WorkshopLoadingBlock(
            label = "",
            modifier = modifier.workshopChromePadding(topExtra = 24.dp, bottomExtra = 24.dp),
        )

        error != null && games.isEmpty() -> WorkshopCenteredState(
            title = stringResource(R.string.game_library_load_failed),
            message = if (error.showAcceleratorHint) {
                error.reason + "\n\n" + stringResource(R.string.library_accelerator_hint)
            } else {
                error.reason
            },
            actionLabel = stringResource(R.string.retry),
            onAction = onRetry,
            modifier = modifier.workshopChromePadding(topExtra = 24.dp, bottomExtra = 24.dp),
        )

        games.isEmpty() -> WorkshopCenteredState(
            title = stringResource(R.string.game_library_empty),
            message = message ?: stringResource(R.string.game_library_empty_hint),
            modifier = modifier.workshopChromePadding(topExtra = 24.dp, bottomExtra = 24.dp),
        )

        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = workshopListContentPadding(topExtra = 20.dp, bottomExtra = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            if (isLoading) {
                item {
                    WorkshopMessageBanner(
                        message = stringResource(R.string.refreshing_game_library),
                        tone = MessageTone.Info,
                    )
                }
            }

            message?.let { info ->
                item {
                    WorkshopMessageBanner(
                        message = info,
                        tone = MessageTone.Info,
                    )
                }
            }

            item {
                SectionHeading(
                    title = stringResource(R.string.added_games),
                    subtitle = stringResource(R.string.added_games_subtitle),
                )
            }

            items(games, key = { it.appId.toString() }) { game ->
                GameShowcaseCard(
                    game = game,
                    primaryActionLabel = stringResource(R.string.view_workshop),
                    onPrimaryAction = { onOpenGame(game) },
                    secondaryActionLabel = stringResource(R.string.remove_from_library),
                    onSecondaryAction = { onRemoveGame(game) },
                )
            }
        }
    }
}

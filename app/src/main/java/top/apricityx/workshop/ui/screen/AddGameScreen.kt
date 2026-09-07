package top.apricityx.workshop.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import top.apricityx.workshop.R
import top.apricityx.workshop.AddGameUiState
import top.apricityx.workshop.data.SteamGame
import top.apricityx.workshop.ui.component.GameShowcaseCard
import top.apricityx.workshop.ui.component.MessageTone
import top.apricityx.workshop.ui.component.ScreenSummaryCard
import top.apricityx.workshop.ui.component.SectionHeading
import top.apricityx.workshop.ui.component.WorkshopCenteredState
import top.apricityx.workshop.ui.component.WorkshopLoadingBlock
import top.apricityx.workshop.ui.component.WorkshopMessageBanner
import top.apricityx.workshop.ui.component.WorkshopPanelCard
import top.apricityx.workshop.ui.component.WorkshopButton
import top.apricityx.workshop.ui.component.WorkshopOutlinedTextField
import top.apricityx.workshop.ui.theme.workshopListContentPadding

@Composable
fun AddGameScreen(
    state: AddGameUiState,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onDirectAppIdChange: (String) -> Unit,
    onAddById: () -> Unit,
    onAddGame: (SteamGame) -> Unit,
    onOpenGame: (SteamGame) -> Unit,
    onRetryFeaturedLoad: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = workshopListContentPadding(topExtra = 20.dp, bottomExtra = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenSummaryCard(
                title = stringResource(R.string.add_game),
                subtitle = stringResource(R.string.add_game_subtitle),
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        item {
            WorkshopPanelCard {
                Text(
                    text = stringResource(R.string.search_game),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    WorkshopOutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = { Text(stringResource(R.string.search_game)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    WorkshopButton(onClick = onSearch, modifier = Modifier.padding(top = 8.dp)) {
                        Text(stringResource(R.string.search))
                    }
                }
            }
        }

        item {
            WorkshopPanelCard {
                Text(
                    text = stringResource(R.string.direct_game_id),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    WorkshopOutlinedTextField(
                        value = state.directAppIdText,
                        onValueChange = onDirectAppIdChange,
                        label = { Text(stringResource(R.string.direct_game_id)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    WorkshopButton(onClick = onAddById, modifier = Modifier.padding(top = 8.dp)) {
                        Text(stringResource(R.string.add))
                    }
                }
            }
        }

        state.message?.let { message ->
            item {
                WorkshopMessageBanner(
                    message = message,
                    tone = if (message.contains("失败") || message.contains("超时")) MessageTone.Error else MessageTone.Info,
                )
            }
        }

        if (state.isSearching) {
            item {
                WorkshopLoadingBlock(label = stringResource(R.string.searching_supported_workshop_games))
            }
        }

        if (state.searchResults.isNotEmpty()) {
            item {
                SectionHeading(
                    title = stringResource(R.string.search_results),
                    subtitle = stringResource(R.string.search_results_subtitle),
                )
            }

            items(state.searchResults, key = { "search-${it.appId}" }) { game ->
                GameShowcaseCard(
                    game = game,
                    primaryActionLabel = stringResource(R.string.add_to_game_library),
                    onPrimaryAction = { onAddGame(game) },
                    secondaryActionLabel = stringResource(R.string.open_directly),
                    onSecondaryAction = { onOpenGame(game) },
                )
            }
        } else if (state.searchQuery.isNotBlank() && !state.isSearching && !state.searchRequestFailed) {
            item {
                WorkshopCenteredState(
                    title = stringResource(R.string.no_results_found),
                    message = stringResource(R.string.no_results_hint),
                )
            }
        }

        item {
            SectionHeading(
                title = stringResource(R.string.featured_workshop_games),
            )
        }

        if (state.isLoadingFeatured) {
            item {
                WorkshopLoadingBlock(label = stringResource(R.string.loading_featured_games))
            }
        } else if (state.featuredGames.isEmpty() && !state.featuredErrorMessage.isNullOrBlank()) {
            item {
                WorkshopCenteredState(
                    title = stringResource(R.string.load_failed),
                    message = state.featuredErrorMessage,
                    actionLabel = stringResource(R.string.retry),
                    onAction = onRetryFeaturedLoad,
                )
            }
        } else if (state.featuredGames.isEmpty()) {
            item {
                WorkshopCenteredState(
                    title = stringResource(R.string.no_featured_games),
                    message = stringResource(R.string.no_featured_games_hint),
                )
            }
        } else {
            items(state.featuredGames, key = { "featured-${it.appId}" }) { game ->
                GameShowcaseCard(
                    game = game,
                    primaryActionLabel = stringResource(R.string.add_to_game_library),
                    onPrimaryAction = { onAddGame(game) },
                    secondaryActionLabel = stringResource(R.string.open_directly),
                    onSecondaryAction = { onOpenGame(game) },
                )
            }
        }
    }
}

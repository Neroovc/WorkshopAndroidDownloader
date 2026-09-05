package top.apricityx.workshop.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.apricityx.workshop.AppFrontendMode
import top.apricityx.workshop.AppThemeMode
import top.apricityx.workshop.DownloadSettingsRepository
import top.apricityx.workshop.SettingsUiState
import top.apricityx.workshop.SteamLoginDialogMode
import top.apricityx.workshop.SteamLoginInputMode
import top.apricityx.workshop.SteamLanguagePreference
import top.apricityx.workshop.accountListItems
import top.apricityx.workshop.canSwitchSteamLoginInputMode
import top.apricityx.workshop.displayName
import top.apricityx.workshop.isSteamConfirmationChallenge
import top.apricityx.workshop.steam.protocol.SteamGuardChallengeType
import top.apricityx.workshop.update.UpdateSource
import top.apricityx.workshop.ui.component.MessageTone
import top.apricityx.workshop.ui.component.WorkshopButton
import top.apricityx.workshop.ui.component.WorkshopMessageBanner
import top.apricityx.workshop.ui.component.WorkshopOutlinedButton
import top.apricityx.workshop.ui.component.WorkshopOutlinedTextField
import top.apricityx.workshop.ui.component.WorkshopPopupMenu
import top.apricityx.workshop.ui.component.WorkshopPopupMenuItem
import top.apricityx.workshop.ui.component.WorkshopSlider
import top.apricityx.workshop.ui.component.WorkshopSwitch
import top.apricityx.workshop.ui.component.WorkshopTextButton
import top.apricityx.workshop.ui.component.WorkshopTransparentGlassDialog
import top.apricityx.workshop.ui.theme.isLiquidGlassFrontendEnabled
import top.apricityx.workshop.ui.theme.workshopChromePadding
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onOpenSteamLoginDialog: () -> Unit,
    onDismissSteamLoginDialog: () -> Unit,
    onUpdateSteamLoginUsername: (String) -> Unit,
    onUpdateSteamLoginPassword: (String) -> Unit,
    onUpdateSteamLoginRefreshToken: (String) -> Unit,
    onUpdateSteamGuardCode: (String) -> Unit,
    onSwitchSteamLoginInputMode: (SteamLoginInputMode) -> Unit,
    onSubmitSteamLogin: () -> Unit,
    onOpenRuntimeLog: () -> Unit,
    onShareRuntimeLogBundle: () -> Unit,
    onExportRuntimeLogBundle: () -> Unit,
    onSwitchToAnonymousSteamAccount: () -> Unit,
    onSetActiveSteamAccount: (String) -> Unit,
    onReauthenticateSteamAccount: (String) -> Unit,
    onRemoveSteamAccount: (String) -> Unit,
    onFrontendModeSelected: (AppFrontendMode) -> Unit,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onSteamLanguagePreferenceSelected: (SteamLanguagePreference) -> Unit,
    onOpenBaiduTranslationApiKeyScreen: () -> Unit,
    onAutoCheckUpdatesChanged: (Boolean) -> Unit,
    onPreferredUpdateSourceSelected: (UpdateSource) -> Unit,
    onManualCheckUpdates: () -> Unit,
    onOpenExternalUrl: (String) -> Unit,
    onThreadCountChange: (String) -> Unit,
    onConcurrentTaskCountChange: (String) -> Unit,
    onModUpdateConcurrentCheckCountChange: (String) -> Unit,
    onAllowSteamAuthenticatedCleartextHttpChanged: (Boolean) -> Unit,
    onExperimentalWorkshopDirectAccessChanged: (Boolean) -> Unit,
    onAutoRenameModFilesToModNameChanged: (Boolean) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSliderInteracting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val useLightSettingsText =
        MaterialTheme.colorScheme.background.luminance() < 0.35f &&
            MaterialTheme.colorScheme.onSurface.luminance() < 0.45f
    val settingsColorScheme = if (useLightSettingsText) {
        MaterialTheme.colorScheme.copy(
            primary = Color(0xFFAED6FF),
            secondary = Color(0xFFFFCCB3),
            tertiary = Color(0xFF9EE6D7),
            onBackground = Color(0xFFF1F7FF),
            onSurface = Color(0xFFF1F7FF),
            onSurfaceVariant = Color(0xFFBED1E2),
            onPrimary = Color(0xFFF1F7FF),
            onSecondary = Color(0xFFF1F7FF),
            onTertiary = Color(0xFFF1F7FF),
        )
    } else {
        MaterialTheme.colorScheme
    }

    MaterialTheme(colorScheme = settingsColorScheme) {
        Column(
            modifier = modifier
                .verticalScroll(
                    state = rememberScrollState(),
                    enabled = !isSliderInteracting,
                )
                .padding(horizontal = 16.dp)
                .workshopChromePadding(topExtra = 16.dp, bottomExtra = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val steamAccountItems = state.steamAuthState.accountListItems(context)
            val selectedSteamAccount = steamAccountItems.firstOrNull { it.isActive } ?: steamAccountItems.first()
            SettingsSectionCard {
                Text(stringResource(R.string.section_steam_accounts), style = MaterialTheme.typography.titleLarge)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WorkshopButton(
                        onClick = onOpenSteamLoginDialog,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.btn_add_account))
                    }
                    if (selectedSteamAccount.accountId != null) {
                        SteamAccountActionsButton(
                            modifier = Modifier.weight(1f),
                            onReauthenticate = { onReauthenticateSteamAccount(selectedSteamAccount.accountId) },
                            onRemove = { onRemoveSteamAccount(selectedSteamAccount.accountId) },
                        )
                    }
                }
//            Text(
//                "Steam login process summaries are written to the runtime log; export the log bundle from the log area below when troubleshooting.",
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//            )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.current_account),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    SettingsChoiceDropdown(
                        selectedOption = selectedSteamAccount,
                        options = steamAccountItems,
                        optionLabel = { account ->
                            if (account.accountName == "anonymous") {
                                stringResource(R.string.common_anonymous)
                            } else {
                                account.accountName
                            }
                        },
                        onOptionSelected = { account ->
                            account.accountId?.let(onSetActiveSteamAccount)
                                ?: onSwitchToAnonymousSteamAccount()
                        },
                    )

                    if (state.steamAuthState.accounts.isEmpty()) {
                        Text(
                            stringResource(R.string.no_saved_accounts),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (selectedSteamAccount.accountId == null) {
                        WorkshopMessageBanner(
                            message = stringResource(R.string.anonymous_browsing_hint),
                            tone = MessageTone.Info,
                        )
                    } else {
                        Text(
                            text = selectedSteamAccount.statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            SettingsSectionCard {
            Text(stringResource(R.string.section_logs), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.logs_support_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
//            Text(
//                text = "Log directory: ${state.runtimeLogDirectoryPath.ifBlank { "not initialized" }}",
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//            )
//            Text(
//                text = "Latest runtime log: ${state.latestRuntimeLogPath ?: "not generated yet"}",
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                maxLines = 3,
//                overflow = TextOverflow.Ellipsis,
//            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                WorkshopOutlinedButton(
                    onClick = onOpenRuntimeLog,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.btn_view_latest_log))
                }
                WorkshopOutlinedButton(
                    onClick = onShareRuntimeLogBundle,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.btn_share_log_bundle))
                }
            }
            WorkshopButton(
                onClick = onExportRuntimeLogBundle,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.btn_export_log_bundle))
            }
            }

            SettingsSectionCard {
            Text(stringResource(R.string.section_translation_settings), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.translation_settings_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (state.baiduTranslationApiKeyConfigured) {
                    stringResource(R.string.baidu_configured_hint)
                } else {
                    stringResource(R.string.baidu_not_configured_hint)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            WorkshopOutlinedButton(
                onClick = onOpenBaiduTranslationApiKeyScreen,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (state.baiduTranslationApiKeyConfigured) {
                        stringResource(R.string.btn_configure_baidu)
                    } else {
                        stringResource(R.string.btn_add_baidu)
                    },
                )
            }
            }

            SettingsSectionCard {
            Text(stringResource(R.string.section_appearance), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.appearance_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(stringResource(R.string.frontend_style), style = MaterialTheme.typography.titleMedium)

            SettingsChoiceDropdown(
                selectedOption = state.selectedFrontendMode,
                options = AppFrontendMode.entries,
                optionLabel = { it.displayName(context) },
                onOptionSelected = onFrontendModeSelected,
            )

            Text(stringResource(R.string.color_theme), style = MaterialTheme.typography.titleMedium)

            SettingsChoiceDropdown(
                selectedOption = state.selectedThemeMode,
                options = AppThemeMode.entries,
                optionLabel = { it.displayName(context) },
                onOptionSelected = onThemeModeSelected,
            )
            }

            SettingsSectionCard {
            Text(stringResource(R.string.section_language), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.language_preference_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SettingsChoiceDropdown(
                selectedOption = state.selectedSteamLanguagePreference,
                options = SteamLanguagePreference.entries,
                optionLabel = { it.displayName(context) },
                onOptionSelected = onSteamLanguagePreferenceSelected,
            )
            }

            SettingsSectionCard {
            Text(stringResource(R.string.section_app_updates), style = MaterialTheme.typography.titleLarge)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(stringResource(R.string.auto_check_updates), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.auto_check_updates_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                WorkshopSwitch(
                    checked = state.autoCheckUpdatesEnabled,
                    onCheckedChange = onAutoCheckUpdatesChanged,
                )
            }

            Text(stringResource(R.string.preferred_update_source), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.preferred_update_source_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SettingsChoiceDropdown(
                    selectedOption = state.preferredUpdateSource,
                    options = state.availableUpdateSources,
                    optionLabel = { it.displayName },
                    onOptionSelected = onPreferredUpdateSourceSelected,
                    modifier = Modifier.weight(1f),
                )
                WorkshopButton(
                    onClick = onManualCheckUpdates,
                    enabled = !state.updateCheckInProgress,
                ) {
                    if (state.updateCheckInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                    Text(
                        text = if (state.updateCheckInProgress) {
                            stringResource(R.string.checking_updates)
                        } else {
                            stringResource(R.string.btn_check_updates_now)
                        },
                    )
                }
            }

            Text(stringResource(R.string.current_version, state.currentVersionText), style = MaterialTheme.typography.bodyMedium)

            Text(stringResource(R.string.last_check_result), style = MaterialTheme.typography.titleMedium)
            Text(
                text = state.updateStatusSummary.ifBlank { stringResource(R.string.update_status_never_checked) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            }

            SettingsSectionCard {
            Text(stringResource(R.string.section_download_settings), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.download_threads_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(stringResource(R.string.allow_cleartext_http), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.allow_cleartext_http_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                WorkshopSwitch(
                    checked = state.allowSteamAuthenticatedCleartextHttp,
                    onCheckedChange = onAllowSteamAuthenticatedCleartextHttpChanged,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(stringResource(R.string.experimental_direct_access), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.experimental_direct_access_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                WorkshopSwitch(
                    checked = state.experimentalWorkshopDirectAccessEnabled,
                    onCheckedChange = onExperimentalWorkshopDirectAccessChanged,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(stringResource(R.string.auto_rename_mod_files), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.auto_rename_mod_files_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                WorkshopSwitch(
                    checked = state.autoRenameModFilesToModNameEnabled,
                    onCheckedChange = onAutoRenameModFilesToModNameChanged,
                )
            }

            SettingsDiscreteSlider(
                title = context.getString(R.string.thread_count_title),
                value = sliderSettingValue(
                    input = state.downloadThreadCountInput,
                    savedValue = state.savedDownloadThreadCount,
                    minValue = DownloadSettingsRepository.MIN_DOWNLOAD_THREADS,
                    maxValue = DownloadSettingsRepository.MAX_DOWNLOAD_THREADS,
                ),
                minValue = DownloadSettingsRepository.MIN_DOWNLOAD_THREADS,
                maxValue = DownloadSettingsRepository.MAX_DOWNLOAD_THREADS,
                supportingText = context.getString(
                    R.string.thread_range_hint,
                    DownloadSettingsRepository.MIN_DOWNLOAD_THREADS,
                    DownloadSettingsRepository.MAX_DOWNLOAD_THREADS,
                ),
                onValueChange = { onThreadCountChange(it.toString()) },
                onValueChangeFinished = onSave,
                onInteractionActiveChange = { isSliderInteracting = it },
            )

            SettingsDiscreteSlider(
                title = context.getString(R.string.concurrent_tasks_title),
                value = sliderSettingValue(
                    input = state.concurrentDownloadTaskCountInput,
                    savedValue = state.savedConcurrentDownloadTaskCount,
                    minValue = DownloadSettingsRepository.MIN_CONCURRENT_DOWNLOAD_TASKS,
                    maxValue = DownloadSettingsRepository.MAX_CONCURRENT_DOWNLOAD_TASKS,
                ),
                minValue = DownloadSettingsRepository.MIN_CONCURRENT_DOWNLOAD_TASKS,
                maxValue = DownloadSettingsRepository.MAX_CONCURRENT_DOWNLOAD_TASKS,
                supportingText = context.getString(
                    R.string.thread_range_hint,
                    DownloadSettingsRepository.MIN_CONCURRENT_DOWNLOAD_TASKS,
                    DownloadSettingsRepository.MAX_CONCURRENT_DOWNLOAD_TASKS,
                ),
                onValueChange = { onConcurrentTaskCountChange(it.toString()) },
                onValueChangeFinished = onSave,
                onInteractionActiveChange = { isSliderInteracting = it },
            )

            SettingsDiscreteSlider(
                title = context.getString(R.string.concurrent_checks_title),
                value = sliderSettingValue(
                    input = state.modUpdateConcurrentCheckCountInput,
                    savedValue = state.savedModUpdateConcurrentCheckCount,
                    minValue = DownloadSettingsRepository.MIN_MOD_UPDATE_CONCURRENT_CHECKS,
                    maxValue = DownloadSettingsRepository.MAX_MOD_UPDATE_CONCURRENT_CHECKS,
                ),
                minValue = DownloadSettingsRepository.MIN_MOD_UPDATE_CONCURRENT_CHECKS,
                maxValue = DownloadSettingsRepository.MAX_MOD_UPDATE_CONCURRENT_CHECKS,
                supportingText = context.getString(
                    R.string.thread_range_hint,
                    DownloadSettingsRepository.MIN_MOD_UPDATE_CONCURRENT_CHECKS,
                    DownloadSettingsRepository.MAX_MOD_UPDATE_CONCURRENT_CHECKS,
                ),
                onValueChange = { onModUpdateConcurrentCheckCountChange(it.toString()) },
                onValueChangeFinished = onSave,
                onInteractionActiveChange = { isSliderInteracting = it },
            )
            }

            state.message?.let {
                WorkshopMessageBanner(
                    message = it,
                    tone = MessageTone.Success,
                )
            }

            SettingsSectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.section_about), style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(R.string.developer_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "apricityx",
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onOpenExternalUrl(primaryDeveloperUrl) },
                )
                Text(
                    text = "ZJustin117",
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onOpenExternalUrl(secondaryDeveloperUrl) },
                )
                Text(
                    stringResource(R.string.repo_url_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = repositoryUrl,
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onOpenExternalUrl(repositoryUrl) },
                )
                Text(
                    stringResource(R.string.about_sts),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = slayTheAmethystModdedUrl,
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onOpenExternalUrl(slayTheAmethystModdedUrl) },
                )
                Text(
                    stringResource(R.string.about_star),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                }
            }
        }
    }

    state.steamAuthState.loginDialogState?.let { dialogState ->
        SteamLoginDialog(
            state = dialogState,
            onDismiss = onDismissSteamLoginDialog,
            onUsernameChange = onUpdateSteamLoginUsername,
            onPasswordChange = onUpdateSteamLoginPassword,
            onRefreshTokenChange = onUpdateSteamLoginRefreshToken,
            onGuardCodeChange = onUpdateSteamGuardCode,
            onSwitchInputMode = onSwitchSteamLoginInputMode,
            onSubmit = onSubmitSteamLogin,
        )
    }
}

@Composable
private fun SettingsSectionCard(
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val isLiquidFrontend = isLiquidGlassFrontendEnabled()
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isLiquidFrontend) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.22f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            },
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = if (isLiquidFrontend) {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            )
        } else {
            null
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun SettingsDiscreteSlider(
    title: String,
    value: Int,
    minValue: Int,
    maxValue: Int,
    supportingText: String,
    onValueChange: (Int) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    onInteractionActiveChange: ((Boolean) -> Unit)? = null,
) {
    val clampedValue = value.coerceIn(minValue, maxValue)
    var sliderValue by remember(minValue, maxValue) {
        mutableFloatStateOf(clampedValue.toFloat())
    }
    var committedValue by remember(minValue, maxValue) {
        mutableStateOf(clampedValue)
    }
    val latestOnValueChange by rememberUpdatedState(onValueChange)
    val latestOnValueChangeFinished by rememberUpdatedState(onValueChangeFinished)
    val displayValue = sliderValue.roundToInt().coerceIn(minValue, maxValue)

    LaunchedEffect(clampedValue) {
        if (clampedValue != committedValue) {
            committedValue = clampedValue
            sliderValue = clampedValue.toFloat()
        }
    }
    LaunchedEffect(displayValue) {
        if (displayValue == committedValue) {
            return@LaunchedEffect
        }
        delay(180)
        if (displayValue != committedValue) {
            committedValue = displayValue
            latestOnValueChange(displayValue)
            latestOnValueChangeFinished?.invoke()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = displayValue.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        WorkshopSlider(
            value = sliderValue,
            onValueChange = {
                val nextValue = it.coerceIn(minValue.toFloat(), maxValue.toFloat())
                sliderValue = nextValue
            },
            modifier = Modifier.fillMaxWidth(),
            valueRange = minValue.toFloat()..maxValue.toFloat(),
            steps = (maxValue - minValue - 1).coerceAtLeast(0),
            onInteractionActiveChange = {
                onInteractionActiveChange?.invoke(it)
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = minValue.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = maxValue.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun sliderSettingValue(
    input: String,
    savedValue: Int,
    minValue: Int,
    maxValue: Int,
): Int =
    input.toIntOrNull()?.coerceIn(minValue, maxValue)
        ?: savedValue.coerceIn(minValue, maxValue)

@Composable
private fun <T> SettingsChoiceDropdown(
    selectedOption: T,
    options: Iterable<T>,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        WorkshopOutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = optionLabel(selectedOption),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = stringResource(R.string.cd_expand_options),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        WorkshopPopupMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                WorkshopPopupMenuItem(
                    text = { Text(optionLabel(option)) },
                    reserveLeadingSpace = true,
                    leadingIcon = {
                        if (option == selectedOption) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    },
                )
            }
        }
    }
}

@Composable
private fun SteamAccountActionsButton(
    modifier: Modifier = Modifier,
    onReauthenticate: () -> Unit,
    onRemove: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        WorkshopOutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.section_actions))
        }
        WorkshopPopupMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            WorkshopPopupMenuItem(
                text = { Text(stringResource(R.string.btn_reauthenticate)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                    )
                },
                onClick = {
                    expanded = false
                    onReauthenticate()
                },
            )
            WorkshopPopupMenuItem(
                text = { Text(stringResource(R.string.btn_delete)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                },
                onClick = {
                    expanded = false
                    onRemove()
                },
            )
        }
    }
}

@Composable
private fun SteamLoginDialog(
    state: top.apricityx.workshop.SteamLoginDialogUiState,
    onDismiss: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRefreshTokenChange: (String) -> Unit,
    onGuardCodeChange: (String) -> Unit,
    onSwitchInputMode: (SteamLoginInputMode) -> Unit,
    onSubmit: () -> Unit,
) {
    val isTokenMode = state.inputMode == SteamLoginInputMode.RefreshToken
    val isConfirmationChallenge = state.challengeType.isSteamConfirmationChallenge()

    WorkshopTransparentGlassDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (state.mode == SteamLoginDialogMode.Reauthenticate) {
                    stringResource(R.string.login_reauthenticate_title)
                } else {
                    stringResource(R.string.login_login_title)
                },
            )
        },
        dismissOnClickOutside = false,
        buttons = {
            WorkshopOutlinedButton(onClick = onDismiss, enabled = !state.isSubmitting) {
                Text(stringResource(R.string.btn_close))
            }
            WorkshopButton(
                onClick = onSubmit,
                enabled = !state.isSubmitting && !state.isPollingConfirmation,
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text(
                    when {
                        isTokenMode -> if (state.mode == SteamLoginDialogMode.Reauthenticate) {
                            stringResource(R.string.btn_import_token)
                        } else {
                            stringResource(R.string.btn_token_login)
                        }

                        state.challengeType == SteamGuardChallengeType.EmailCode ||
                            state.challengeType == SteamGuardChallengeType.DeviceCode ->
                            stringResource(R.string.btn_submit_code)

                        isConfirmationChallenge -> stringResource(R.string.btn_keep_waiting)

                        else -> if (state.mode == SteamLoginDialogMode.Reauthenticate) {
                            stringResource(R.string.btn_reauthenticate_short)
                        } else {
                            stringResource(R.string.btn_login)
                        }
                    },
                )
            }
        },
    ) {
        when {
            isTokenMode -> {
                Text(
                    if (state.mode == SteamLoginDialogMode.Reauthenticate) {
                        stringResource(R.string.token_reauth_hint)
                    } else {
                        stringResource(R.string.token_import_hint)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                WorkshopOutlinedTextField(
                    value = state.username,
                    onValueChange = onUsernameChange,
                    label = {
                        Text(
                            if (state.mode == SteamLoginDialogMode.Reauthenticate) {
                                stringResource(R.string.label_account_display_name)
                            } else {
                                stringResource(R.string.label_account_display_name_optional)
                            },
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = state.mode != SteamLoginDialogMode.Reauthenticate,
                )
                WorkshopOutlinedTextField(
                    value = state.refreshToken,
                    onValueChange = onRefreshTokenChange,
                    label = { Text("Refresh Token") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            }

            state.challengeType == SteamGuardChallengeType.EmailCode ||
                state.challengeType == SteamGuardChallengeType.DeviceCode -> {
                Text(
                    state.challengeMessage ?: stringResource(R.string.steam_guard_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                WorkshopOutlinedTextField(
                    value = state.guardCode,
                    onValueChange = onGuardCodeChange,
                    label = { Text(stringResource(R.string.label_code)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            isConfirmationChallenge -> {
                Text(
                    state.challengeMessage ?: stringResource(R.string.steam_confirm_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.isPollingConfirmation) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                        Text(
                            stringResource(R.string.waiting_steam_confirm),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            else -> {
                WorkshopOutlinedTextField(
                    value = state.username,
                    onValueChange = onUsernameChange,
                    label = { Text(stringResource(R.string.label_username)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = state.mode != SteamLoginDialogMode.Reauthenticate,
                )
                WorkshopOutlinedTextField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(R.string.label_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }

        if (state.canSwitchSteamLoginInputMode()) {
            WorkshopTextButton(
                onClick = {
                    onSwitchInputMode(
                        if (isTokenMode) {
                            SteamLoginInputMode.Credentials
                        } else {
                            SteamLoginInputMode.RefreshToken
                        },
                    )
                },
            ) {
                Text(
                    if (isTokenMode) {
                        if (state.mode == SteamLoginDialogMode.Reauthenticate) {
                            stringResource(R.string.btn_reauth_with_password)
                        } else {
                            stringResource(R.string.btn_login_with_password)
                        }
                    } else {
                        stringResource(R.string.btn_switch_to_token)
                    },
                )
            }
        }

        state.errorMessage?.takeIf(String::isNotBlank)?.let { message ->
            WorkshopMessageBanner(
                message = message,
                tone = MessageTone.Error,
            )
        }
        Text(
            text = stringResource(R.string.login_summary_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun sourceDescription(source: UpdateSource, context: android.content.Context): String =
    when (source) {
        UpdateSource.GH_PROXY_COM -> context.getString(R.string.update_source_gh_proxy_com)
        UpdateSource.GH_PROXY_VIP -> context.getString(R.string.update_source_gh_proxy_vip)
        UpdateSource.GH_LLKK -> context.getString(R.string.update_source_gh_llkk)
        UpdateSource.GH_PROXY_NET -> context.getString(R.string.update_source_gh_proxy_net)
        UpdateSource.OFFICIAL -> context.getString(R.string.update_source_official)
    }

private fun steamLanguagePreferenceDescription(
    preference: SteamLanguagePreference,
    context: android.content.Context,
): String =
    when (preference) {
        SteamLanguagePreference.SimplifiedChinese -> context.getString(R.string.steam_lang_chinese_desc)
        SteamLanguagePreference.English -> context.getString(R.string.steam_lang_english_desc)
    }

private const val repositoryUrl = "https://github.com/Apricityx/WorkshopAndroidDownloader"
private const val primaryDeveloperUrl = "https://github.com/Apricityx"
private const val secondaryDeveloperUrl = "https://github.com/ZJustin117"
private const val slayTheAmethystModdedUrl =
    "https://github.com/ModinMobileSTS/SlayTheAmethystModded"

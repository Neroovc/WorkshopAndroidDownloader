package top.apricityx.workshop

import android.content.Context
import top.apricityx.workshop.data.SteamGame
import top.apricityx.workshop.data.WorkshopBrowseItem
import top.apricityx.workshop.steam.protocol.STEAM_LANGUAGE_ENGLISH
import top.apricityx.workshop.steam.protocol.STEAM_LANGUAGE_SIMPLIFIED_CHINESE
import top.apricityx.workshop.update.UpdateSource
import top.apricityx.workshop.steam.protocol.SteamGuardChallengeType

enum class WorkshopScreenDestination {
    GameLibrary,
    ModLibrary,
    AddGame,
    GameWorkshop,
    WorkshopItemDetail,
    ModDetail,
    DownloadCenter,
    DownloadTaskDetail,
    Settings,
    BaiduTranslationApiKey,
}

fun WorkshopScreenDestination.isLibraryRoot(): Boolean =
    this == WorkshopScreenDestination.GameLibrary || this == WorkshopScreenDestination.ModLibrary

fun WorkshopScreenDestination.showsDownloadCenterShortcut(): Boolean =
    this != WorkshopScreenDestination.DownloadCenter &&
        this != WorkshopScreenDestination.DownloadTaskDetail &&
        this != WorkshopScreenDestination.Settings &&
        this != WorkshopScreenDestination.BaiduTranslationApiKey

fun WorkshopScreenDestination.showsSettingsShortcut(): Boolean =
    this != WorkshopScreenDestination.GameWorkshop &&
        this != WorkshopScreenDestination.DownloadCenter &&
        this != WorkshopScreenDestination.DownloadTaskDetail &&
        this != WorkshopScreenDestination.Settings &&
        this != WorkshopScreenDestination.BaiduTranslationApiKey

fun WorkshopScreenDestination.showsGameWorkshopMoreShortcut(): Boolean =
    this == WorkshopScreenDestination.GameWorkshop

enum class AppThemeMode(
    val storageValue: String,
) {
    FollowSystem("follow_system"),
    Light("light"),
    Dark("dark");

    companion object {
        fun fromStorageValue(value: String): AppThemeMode =
            entries.firstOrNull { it.storageValue == value } ?: FollowSystem
    }
}

enum class AppFrontendMode(
    val storageValue: String,
) {
    LiquidGlass("liquid_glass"),
    LiteLiquidGlass("lite_liquid_glass"),
    Legacy("legacy");

    companion object {
        fun fromStorageValue(value: String): AppFrontendMode =
            entries.firstOrNull { it.storageValue == value } ?: LiquidGlass
    }
}

enum class SteamLanguagePreference(
    val storageValue: String,
    val requestValue: String,
    val acceptLanguageValue: String,
) {
    SimplifiedChinese(
        storageValue = "schinese",
        requestValue = "schinese",
        acceptLanguageValue = "zh-CN,zh;q=0.9",
    ),
    English(
        storageValue = "english",
        requestValue = "english",
        acceptLanguageValue = "en-US,en;q=0.9",
    );

    companion object {
        fun fromStorageValue(value: String): SteamLanguagePreference =
            entries.firstOrNull { it.storageValue == value } ?: SimplifiedChinese
    }
}

enum class ModLibraryDisplayMode(
    val storageValue: String,
) {
    LargePreview("large_preview"),
    CompactList("compact_list"),
    Overview("overview");

    fun next(): ModLibraryDisplayMode =
        when (this) {
            LargePreview -> CompactList
            CompactList -> Overview
            Overview -> LargePreview
        }

    companion object {
        fun fromStorageValue(value: String): ModLibraryDisplayMode =
            entries.firstOrNull { it.storageValue == value } ?: CompactList
    }
}

data class ModLibraryFilterState(
    val searchQuery: String = "",
    val selectedGameTitle: String? = null,
)

enum class ModLibrarySortOption {
    LatestSynced,
    ModTitle,
    GameTitle,
}

data class AddGameUiState(
    val featuredGames: List<SteamGame> = emptyList(),
    val searchResults: List<SteamGame> = emptyList(),
    val searchQuery: String = "",
    val directAppIdText: String = "",
    val isLoadingFeatured: Boolean = false,
    val isSearching: Boolean = false,
    val searchRequestFailed: Boolean = false,
    val featuredErrorMessage: String? = null,
    val message: String? = null,
    val messageIsError: Boolean = false,
)

data class GameWorkshopUiState(
    val game: SteamGame,
    val searchQuery: String = "",
    val selectedSortOption: WorkshopBrowseSortOption = WorkshopBrowseSortOption.MostPopular,
    val selectedTimeWindow: WorkshopBrowseTimeWindow = WorkshopBrowseTimeWindow.OneWeek,
    val items: List<WorkshopBrowseItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val page: Int = 1,
    val hasNextPage: Boolean = false,
    val message: String? = null,
    val showConnectionErrorState: Boolean = false,
    val retryLoadMoreOnError: Boolean = false,
    val isMoreActionsExpanded: Boolean = false,
    val showDirectDownloadDialog: Boolean = false,
)

data class WorkshopItemDetailUiState(
    val item: WorkshopBrowseItem,
    val detail: top.apricityx.workshop.data.WorkshopItemDetail? = null,
    val isLoading: Boolean = false,
    val isLoadingComments: Boolean = false,
    val isTranslatingDescription: Boolean = false,
    val translatedDescription: String? = null,
    val translationErrorMessage: String? = null,
    val commentErrorMessage: String? = null,
    val message: String? = null,
    val showConnectionErrorState: Boolean = false,
)

data class ModLibraryDescriptionTranslationUiState(
    val isTranslatingDescription: Boolean = false,
    val translatedDescription: String? = null,
    val translationErrorMessage: String? = null,
)

data class ModLibraryChangeNotesDialogUiState(
    val group: DownloadedModGroup,
    val markdown: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class ModLibraryUiState(
    val items: List<DownloadedModGroup> = emptyList(),
    val selectedEntry: DownloadedModGroup? = null,
    val detailDescriptionTranslation: ModLibraryDescriptionTranslationUiState =
        ModLibraryDescriptionTranslationUiState(),
    val changeNotesDialogState: ModLibraryChangeNotesDialogUiState? = null,
    val displayMode: ModLibraryDisplayMode = DownloadSettingsRepository.DEFAULT_MOD_LIBRARY_DISPLAY_MODE,
    val filterState: ModLibraryFilterState = ModLibraryFilterState(),
    val filterPanelExpanded: Boolean = false,
    val sortOption: ModLibrarySortOption = ModLibrarySortOption.LatestSynced,
    val updateCheckState: ModLibraryUpdateCheckState = ModLibraryUpdateCheckState(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
)

data class SteamDirectAccessFallbackDialogUiState(
    val message: String,
)

data class WorkshopUiState(
    val currentScreen: WorkshopScreenDestination = WorkshopScreenDestination.GameLibrary,
    val previousScreen: WorkshopScreenDestination = WorkshopScreenDestination.GameLibrary,
    val themeMode: AppThemeMode = DownloadSettingsRepository.DEFAULT_THEME_MODE,
    val frontendMode: AppFrontendMode = DownloadSettingsRepository.DEFAULT_FRONTEND_MODE,
    val libraryGames: List<SteamGame> = emptyList(),
    val isLibraryLoading: Boolean = true,
    val libraryMessage: String? = null,
    val libraryError: LibraryErrorUiState? = null,
    val pendingRemoveGame: SteamGame? = null,
    val modLibraryState: ModLibraryUiState = ModLibraryUiState(isLoading = true),
    val pendingRemoveMod: DownloadedModEntry? = null,
    val pendingRenameMod: DownloadedModGroup? = null,
    val renameModTitleInput: String = "",
    val showUsageNoticeDialog: Boolean = false,
    val steamDirectAccessFallbackDialogState: SteamDirectAccessFallbackDialogUiState? = null,
    val addGameState: AddGameUiState = AddGameUiState(),
    val gameWorkshopState: GameWorkshopUiState? = null,
    val workshopItemDetailState: WorkshopItemDetailUiState? = null,
    val workshopItemDetailBackStack: List<WorkshopItemDetailUiState> = emptyList(),
    val downloadCenterState: DownloadCenterUiState = DownloadCenterUiState(),
    val selectedDownloadTaskId: String? = null,
    val settingsState: SettingsUiState = SettingsUiState(),
    val baiduTranslationApiKeyState: BaiduTranslationApiKeyUiState = BaiduTranslationApiKeyUiState(),
)

data class LibraryErrorUiState(
    val reason: String,
    val showAcceleratorHint: Boolean = false,
)

data class SettingsUiState(
    val downloadThreadCountInput: String = "",
    val savedDownloadThreadCount: Int = DownloadSettingsRepository.DEFAULT_DOWNLOAD_THREADS,
    val concurrentDownloadTaskCountInput: String = "",
    val savedConcurrentDownloadTaskCount: Int = DownloadSettingsRepository.DEFAULT_CONCURRENT_DOWNLOAD_TASKS,
    val modUpdateConcurrentCheckCountInput: String = "",
    val savedModUpdateConcurrentCheckCount: Int = DownloadSettingsRepository.DEFAULT_MOD_UPDATE_CONCURRENT_CHECKS,
    val selectedThemeMode: AppThemeMode = DownloadSettingsRepository.DEFAULT_THEME_MODE,
    val selectedFrontendMode: AppFrontendMode = DownloadSettingsRepository.DEFAULT_FRONTEND_MODE,
    val selectedSteamLanguagePreference: SteamLanguagePreference =
        DownloadSettingsRepository.DEFAULT_STEAM_LANGUAGE_PREFERENCE,
    val allowSteamAuthenticatedCleartextHttp: Boolean =
        DownloadSettingsRepository.DEFAULT_ALLOW_STEAM_AUTHENTICATED_CLEARTEXT_HTTP,
    val experimentalWorkshopDirectAccessEnabled: Boolean =
        DownloadSettingsRepository.DEFAULT_EXPERIMENTAL_WORKSHOP_DIRECT_ACCESS_ENABLED,
    val autoRenameModFilesToModNameEnabled: Boolean =
        DownloadSettingsRepository.DEFAULT_AUTO_RENAME_MOD_FILES_TO_MOD_NAME_ENABLED,
    val baiduTranslationApiKeyConfigured: Boolean = false,
    val steamAuthState: SteamAuthUiState = SteamAuthUiState(),
    val autoCheckUpdatesEnabled: Boolean = DownloadSettingsRepository.DEFAULT_AUTO_CHECK_UPDATES_ENABLED,
    val preferredUpdateSource: UpdateSource = UpdateSource.DEFAULT_PREFERRED_USER_SOURCE,
    val availableUpdateSources: List<UpdateSource> = UpdateSource.userSelectableSources(),
    val currentVersionText: String = "",
    val updateStatusSummary: String = "",
    val updateCheckInProgress: Boolean = false,
    val updatePromptState: UpdatePromptState? = null,
    val runtimeLogDirectoryPath: String = "",
    val latestRuntimeLogPath: String? = null,
    val message: String? = null,
)

data class BaiduTranslationApiKeyUiState(
    val appIdInput: String = "",
    val apiKeyInput: String = "",
    val hasSavedCredentials: Boolean = false,
    val isTesting: Boolean = false,
    val sampleSourceText: String = BAIDU_TRANSLATION_SAMPLE_TEXT,
    val testResultText: String? = null,
    val testFailureReason: String? = null,
    val message: String? = null,
)

data class SteamAuthUiState(
    val accounts: List<SteamAccountSummary> = emptyList(),
    val activeAccountId: String? = null,
    val isBrowsingUnauthenticated: Boolean = true,
    val statusSummary: String = "",
    val loginDialogState: SteamLoginDialogUiState? = null,
)

data class SteamAccountListItemUiState(
    val accountId: String? = null,
    val accountName: String,
    val isActive: Boolean,
    val statusText: String,
    val canManage: Boolean,
)

enum class SteamLoginInputMode {
    Credentials,
    RefreshToken,
}

data class SteamLoginDialogUiState(
    val mode: SteamLoginDialogMode = SteamLoginDialogMode.Add,
    val inputMode: SteamLoginInputMode = SteamLoginInputMode.Credentials,
    val username: String = "",
    val password: String = "",
    val refreshToken: String = "",
    val guardCode: String = "",
    val challengeType: SteamGuardChallengeType? = null,
    val challengeMessage: String? = null,
    val isPollingConfirmation: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val targetAccountId: String? = null,
)

enum class SteamLoginDialogMode {
    Add,
    Reauthenticate,
}

data class UpdatePromptState(
    val currentVersion: String,
    val latestVersion: String,
    val publishedAtText: String,
    val downloadSourceDisplayName: String,
    val notesText: String,
    val downloadUrl: String,
    val defaultDownloadSourceId: String,
    val downloadOptions: List<UpdateDownloadOptionState>,
)

data class UpdateDownloadOptionState(
    val label: String,
    val url: String,
    val source: UpdateSource,
)

fun AppThemeMode.displayName(context: Context): String =
    when (this) {
        AppThemeMode.FollowSystem -> context.getString(R.string.theme_follow_system)
        AppThemeMode.Light -> context.getString(R.string.theme_light)
        AppThemeMode.Dark -> context.getString(R.string.theme_dark)
    }

fun AppFrontendMode.displayName(context: Context): String =
    when (this) {
        AppFrontendMode.LiquidGlass -> context.getString(R.string.frontend_liquid_glass)
        AppFrontendMode.LiteLiquidGlass -> context.getString(R.string.frontend_lite_liquid_glass)
        AppFrontendMode.Legacy -> context.getString(R.string.frontend_legacy)
    }

fun SteamLanguagePreference.displayName(context: Context): String =
    when (this) {
        SteamLanguagePreference.SimplifiedChinese -> context.getString(R.string.lang_simplified_chinese)
        SteamLanguagePreference.English -> context.getString(R.string.lang_english)
    }

fun SteamLanguagePreference.toSteamPublishedFileLanguage(): Int =
    when (this) {
        SteamLanguagePreference.SimplifiedChinese -> STEAM_LANGUAGE_SIMPLIFIED_CHINESE
        SteamLanguagePreference.English -> STEAM_LANGUAGE_ENGLISH
    }

fun ModLibraryDisplayMode.screenSubtitle(context: Context): String =
    when (this) {
        ModLibraryDisplayMode.LargePreview -> context.getString(R.string.mode_large_preview_desc)
        ModLibraryDisplayMode.CompactList -> context.getString(R.string.mode_compact_list_desc)
        ModLibraryDisplayMode.Overview -> context.getString(R.string.mode_overview_desc)
    }

fun ModLibraryDisplayMode.sectionSubtitle(context: Context): String =
    when (this) {
        ModLibraryDisplayMode.LargePreview -> context.getString(R.string.mode_large_preview_section_hint)
        ModLibraryDisplayMode.CompactList -> context.getString(R.string.mode_compact_list_section_hint)
        ModLibraryDisplayMode.Overview -> context.getString(R.string.mode_overview_section_hint)
    }

fun ModLibraryDisplayMode.toggleContentDescription(context: Context): String =
    when (next()) {
        ModLibraryDisplayMode.LargePreview -> context.getString(R.string.mode_large_preview_toggle)
        ModLibraryDisplayMode.CompactList -> context.getString(R.string.mode_compact_list_toggle)
        ModLibraryDisplayMode.Overview -> context.getString(R.string.mode_overview_toggle)
    }

fun ModLibrarySortOption.displayName(context: Context): String =
    when (this) {
        ModLibrarySortOption.LatestSynced -> context.getString(R.string.sort_latest_synced)
        ModLibrarySortOption.ModTitle -> context.getString(R.string.sort_mod_title)
        ModLibrarySortOption.GameTitle -> context.getString(R.string.sort_game_title)
    }

fun ModLibraryFilterState.hasActiveFilters(): Boolean =
    searchQuery.isNotBlank() ||
        !selectedGameTitle.isNullOrBlank()

fun SteamAccountsSnapshot.toUiState(
    context: Context,
    loginDialogState: SteamLoginDialogUiState? = null,
): SteamAuthUiState =
    activeAccount.let { currentActiveAccount ->
        SteamAuthUiState(
            accounts = accounts,
            activeAccountId = activeAccountId,
            isBrowsingUnauthenticated = currentActiveAccount?.requiresReauthentication != false,
            statusSummary = if (currentActiveAccount != null) {
                if (currentActiveAccount.requiresReauthentication) {
                    context.getString(R.string.auth_status_needs_reauth, currentActiveAccount.accountName)
                } else {
                    context.getString(R.string.auth_status_active, currentActiveAccount.accountName)
                }
            } else {
                context.getString(R.string.auth_status_anonymous)
            },
            loginDialogState = loginDialogState,
        )
    }

fun SteamAuthUiState.accountListItems(context: Context): List<SteamAccountListItemUiState> =
    buildList {
        add(
            SteamAccountListItemUiState(
                accountName = "anonymous",
                isActive = activeAccountId == null,
                statusText = if (activeAccountId == null) {
                    context.getString(R.string.account_status_active)
                } else {
                    context.getString(R.string.account_anonymous_browsing)
                },
                canManage = false,
            ),
        )
        accounts.forEach { account ->
            add(
                SteamAccountListItemUiState(
                    accountId = account.accountId,
                    accountName = account.accountName,
                    isActive = account.isActive,
                    statusText = when {
                        account.requiresReauthentication ->
                            context.getString(R.string.account_status_need_reauth)
                        account.isActive -> context.getString(R.string.account_status_active)
                        else -> context.getString(R.string.account_status_saved)
                    },
                    canManage = true,
                ),
            )
        }
    }

fun SteamGuardChallengeType?.isSteamConfirmationChallenge(): Boolean =
    this == SteamGuardChallengeType.DeviceConfirmation || this == SteamGuardChallengeType.EmailConfirmation

fun SteamLoginDialogUiState.canSwitchSteamLoginInputMode(): Boolean =
    !isSubmitting &&
        (
            inputMode == SteamLoginInputMode.RefreshToken ||
                challengeType == null ||
                challengeType.isSteamConfirmationChallenge()
        )

const val BAIDU_TRANSLATION_SAMPLE_TEXT =
    "This mod adds a new relic and several balance changes for a smoother run."

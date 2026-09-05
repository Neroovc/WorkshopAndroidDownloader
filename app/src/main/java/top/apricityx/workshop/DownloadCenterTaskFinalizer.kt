package top.apricityx.workshop

import android.app.Application
import java.io.File
import top.apricityx.workshop.workshop.DownloadedFileInfo

class DownloadCenterTaskFinalizer(
    private val application: Application,
    private val publicExportManager: WorkshopPublicExportManager = WorkshopPublicExportManager(application),
    private val modLibraryRepository: ModLibraryRepository = ModLibraryRepository(application),
    private val previewImageCache: WorkshopPreviewImageCache = WorkshopPreviewImageCache(application),
) {
    suspend fun finalizeSuccessfulDownload(
        task: DownloadCenterTaskUiState,
        stagingDir: File,
        downloadedFiles: List<DownloadedFileInfo>,
        log: suspend (String) -> Unit,
    ): FinalizedDownloadArtifacts {
        log(application.getString(R.string.log_finalize_start, stagingDir.absolutePath, downloadedFiles.size))
        val metadata = readWorkshopDownloadMetadata(stagingDir)
        val resolvedItemTitle = metadata?.title?.takeIf(String::isNotBlank) ?: task.itemTitle
        val version = resolveWorkshopModVersion(metadata)
        log(application.getString(R.string.log_metadata_done, resolvedItemTitle))
        val exportedFiles = publicExportManager.exportDownloadedFiles(
            gameTitle = task.gameTitle,
            itemTitle = resolvedItemTitle,
            versionId = version.versionId,
            stagingDir = stagingDir,
            files = downloadedFiles,
            log = log,
        )
        log(application.getString(R.string.log_export_done, exportedFiles.size))
        val previewImagePath = cachePreviewImage(
            task = task,
            previewImageUrl = metadata?.previewImageUrl,
            log = log,
        )
        log(application.getString(R.string.log_preview_phase_done, previewImagePath ?: "<none>"))
        syncModLibrary(
            task = task,
            itemTitle = resolvedItemTitle,
            description = metadata?.description.orEmpty(),
            version = version,
            previewImagePath = previewImagePath,
            previewImageUrl = metadata?.previewImageUrl.orEmpty(),
            exportedFiles = exportedFiles,
            log = log,
        )
        log(application.getString(R.string.log_library_sync_done))
        return FinalizedDownloadArtifacts(
            itemTitle = resolvedItemTitle,
            exportedFiles = exportedFiles,
        )
    }

    private suspend fun cachePreviewImage(
        task: DownloadCenterTaskUiState,
        previewImageUrl: String?,
        log: suspend (String) -> Unit,
    ): String? {
        log(application.getString(R.string.log_preview_start, previewImageUrl ?: "<none>"))
        return runCatching {
            previewImageCache.cachePreviewImage(
                appId = task.appId,
                publishedFileId = task.publishedFileId,
                imageUrl = previewImageUrl,
            )
        }.fold(
            onSuccess = { path ->
                log(application.getString(R.string.log_preview_done, path ?: "<none>"))
                path
            },
            onFailure = { error ->
            log(application.getString(R.string.log_preview_failed, error.summary(application)))
            null
            },
        )
    }

    private suspend fun syncModLibrary(
        task: DownloadCenterTaskUiState,
        itemTitle: String,
        description: String,
        version: WorkshopModVersion,
        previewImagePath: String?,
        previewImageUrl: String,
        exportedFiles: List<ExportedDownloadFile>,
        log: suspend (String) -> Unit,
    ) {
        log(application.getString(R.string.log_library_index_start, exportedFiles.size))
        val result = runCatching {
            modLibraryRepository.upsertDownloadedMod(
                appId = task.appId,
                publishedFileId = task.publishedFileId,
                gameTitle = task.gameTitle,
                itemTitle = itemTitle,
                description = description,
                previewImagePath = previewImagePath,
                previewImageUrl = previewImageUrl,
                versionId = version.versionId,
                versionUpdatedAtMillis = version.updatedAtMillis,
                files = exportedFiles,
            )
        }
        if (result.isSuccess) {
            log(application.getString(R.string.log_library_index_done))
        } else {
            log(application.getString(R.string.log_library_index_failed, result.exceptionOrNull()?.summary(application) ?: application.getString(R.string.common_unknown_error)))
        }
    }
}

data class FinalizedDownloadArtifacts(
    val itemTitle: String,
    val exportedFiles: List<ExportedDownloadFile>,
)

private fun Throwable.summary(context: android.content.Context): String =
    message ?: this::class.simpleName ?: context.getString(R.string.common_unknown_error)

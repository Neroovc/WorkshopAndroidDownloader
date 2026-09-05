package top.apricityx.workshop

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLConnection

object WorkshopFileOpenManager {
    fun createOpenFileIntent(context: Context, file: ExportedDownloadFile): Intent? {
        val uri = runCatching { Uri.parse(file.contentUri) }
            .getOrNull()
            ?.takeIf { it.toString().isNotBlank() }
            ?: return null

        val mimeType = URLConnection.guessContentTypeFromName(file.relativePath.substringAfterLast('/')) ?: "*/*"
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return Intent.createChooser(viewIntent, context.getString(R.string.chooser_open_file)).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}

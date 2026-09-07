package top.apricityx.workshop.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.apricityx.workshop.R
import top.apricityx.workshop.data.WorkshopBrowseItem
import top.apricityx.workshop.data.WorkshopRequiredItem

@Composable
fun DownloadDependencyWarningDialog(
    item: WorkshopBrowseItem,
    requiredItems: List<WorkshopRequiredItem>,
    onDismissRequest: () -> Unit,
    onDownloadAllWithDependencies: () -> Unit,
    onDownloadOnlyCurrent: () -> Unit,
) {
    WorkshopDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.dependency_not_downloaded_title)) },
        buttons = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End,
            ) {
                WorkshopButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDownloadAllWithDependencies,
                ) {
                    Text(stringResource(R.string.download_all_prerequisites_and_mod))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                ) {
                    WorkshopTextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.cancel))
                    }
                    WorkshopOutlinedButton(onClick = onDownloadOnlyCurrent) {
                        Text(stringResource(R.string.download_mod_only))
                    }
                }
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.prerequisites_pending_format, item.title, requiredItems.size))
            Text(stringResource(R.string.prerequisites_choice_hint))
            Text(
                text = requiredItems.joinToString(separator = "\n") { "• ${it.title}" },
            )
        }
    }
}

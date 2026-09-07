package top.apricityx.workshop.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.apricityx.workshop.BaiduTranslationApiKeyUiState
import top.apricityx.workshop.R
import top.apricityx.workshop.ui.component.MessageTone
import top.apricityx.workshop.ui.component.WorkshopMessageBanner
import top.apricityx.workshop.ui.component.WorkshopPanelCard
import top.apricityx.workshop.ui.component.WorkshopButton
import top.apricityx.workshop.ui.component.WorkshopOutlinedButton
import top.apricityx.workshop.ui.component.WorkshopOutlinedTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import top.apricityx.workshop.ui.theme.workshopChromePadding

@Composable
fun BaiduTranslationApiKeyScreen(
    state: BaiduTranslationApiKeyUiState,
    onAppIdChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onSave: () -> Unit,
    onTestTranslation: () -> Unit,
    onOpenApiKeyGuide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .workshopChromePadding(topExtra = 16.dp, bottomExtra = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val testResultTitle = stringResource(R.string.test_result)
        val failureReasonTitle = stringResource(R.string.failure_reason)
        WorkshopPanelCard {
            Text(stringResource(R.string.baidu_llm_credentials_title), style = MaterialTheme.typography.titleLarge)
            Text(
                text = if (state.hasSavedCredentials) {
                    stringResource(R.string.baidu_credentials_saved_hint)
                } else {
                    stringResource(R.string.baidu_credentials_unsaved_hint)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            WorkshopOutlinedTextField(
                value = state.appIdInput,
                onValueChange = onAppIdChange,
                label = { Text("AppID") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            WorkshopOutlinedTextField(
                value = state.apiKeyInput,
                onValueChange = onApiKeyChange,
                label = { Text("API Key") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            WorkshopButton(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save))
            }

            WorkshopOutlinedButton(
                onClick = onTestTranslation,
                enabled = !state.isTesting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp,
                    )
                    Text(stringResource(R.string.testing_translation))
                } else {
                    Text(stringResource(R.string.test_translation))
                }
            }
        }

        WorkshopPanelCard {
            Text(stringResource(R.string.sample_text), style = MaterialTheme.typography.titleMedium)
            Text(
                text = state.sampleSourceText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            state.testResultText?.let { testResultText ->
                Text(testResultTitle, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = testResultText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            state.testFailureReason?.let { testFailureReason ->
                Text(
                    text = failureReasonTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = testFailureReason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        state.message?.let { message ->
            WorkshopMessageBanner(
                message = message,
                tone = MessageTone.Success,
            )
        }

        BaiduTranslationTutorialCard(
            onOpenApiKeyGuide = onOpenApiKeyGuide,
        )
    }
}

@Composable
private fun BaiduTranslationTutorialCard(
    onOpenApiKeyGuide: () -> Unit,
) {
    WorkshopPanelCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.tutorial), style = MaterialTheme.typography.titleLarge)
            WorkshopOutlinedButton(
                onClick = onOpenApiKeyGuide,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.open_baidu_translation_platform))
            }

            baiduTutorialSteps.forEachIndexed { index, step ->
                BaiduTranslationTutorialStep(
                    stepNumber = index + 1,
                    step = step,
                    topPadding = if (index == 0) 0.dp else 4.dp,
                )
            }
        }
    }
}

@Composable
private fun BaiduTranslationTutorialStep(
    stepNumber: Int,
    step: BaiduTutorialStep,
    topPadding: Dp,
) {
    val stepText = stringResource(step.textResId)
    val noteText = if (step.noteResId != null) stringResource(step.noteResId) else null
    Column(
        modifier = Modifier.padding(top = topPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "$stepNumber. $stepText",
            style = MaterialTheme.typography.titleMedium,
        )

        noteText?.let { note ->
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        step.imageResId?.let { imageResId ->
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = stepText,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.FillWidth,
            )
        }
    }
}

private data class BaiduTutorialStep(
    @StringRes val textResId: Int,
    val imageResId: Int? = null,
    @StringRes val noteResId: Int? = null,
)

private val baiduTutorialSteps = listOf(
    BaiduTutorialStep(
        textResId = R.string.baidu_tutorial_step_01,
        noteResId = R.string.baidu_tutorial_entry_note,
    ),
    BaiduTutorialStep(
        textResId = R.string.baidu_tutorial_step_02,
        imageResId = R.drawable.baidu_ai_text_tutorial_step_02,
    ),
    BaiduTutorialStep(
        textResId = R.string.baidu_tutorial_step_03,
        imageResId = R.drawable.baidu_ai_text_tutorial_step_03,
    ),
    BaiduTutorialStep(
        textResId = R.string.baidu_tutorial_step_04,
        imageResId = R.drawable.baidu_ai_text_tutorial_step_04,
    ),
    BaiduTutorialStep(
        textResId = R.string.baidu_tutorial_step_05,
        imageResId = R.drawable.baidu_ai_text_tutorial_step_05,
    ),
    BaiduTutorialStep(
        textResId = R.string.baidu_tutorial_step_06,
        imageResId = R.drawable.baidu_ai_text_tutorial_step_06,
    ),
    BaiduTutorialStep(
        textResId = R.string.baidu_tutorial_step_07,
        imageResId = R.drawable.baidu_ai_text_tutorial_step_07,
    ),
    BaiduTutorialStep(
        textResId = R.string.baidu_tutorial_step_08,
        imageResId = R.drawable.baidu_ai_text_tutorial_step_08,
    ),
    BaiduTutorialStep(
        textResId = R.string.baidu_tutorial_step_09,
    ),
    BaiduTutorialStep(
        textResId = R.string.baidu_tutorial_step_10,
        imageResId = R.drawable.baidu_ai_text_tutorial_step_10,
    ),
    BaiduTutorialStep(
        textResId = R.string.baidu_tutorial_step_11,
        imageResId = R.drawable.baidu_ai_text_tutorial_step_10_result,
    ),
    BaiduTutorialStep(
        textResId = R.string.baidu_tutorial_step_12,
    ),
)

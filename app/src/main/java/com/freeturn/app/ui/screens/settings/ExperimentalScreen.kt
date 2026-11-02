@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class
)

package com.freeturn.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freeturn.app.R
import com.freeturn.app.data.config.SfuPreset
import com.freeturn.app.ui.components.ChoiceOption
import com.freeturn.app.ui.components.ConnectedChoiceRow
import com.freeturn.app.ui.components.LabeledTextField
import com.freeturn.app.ui.components.SettingsBackButton
import com.freeturn.app.ui.components.SettingsCard
import com.freeturn.app.ui.components.SettingsContentMaxWidth
import com.freeturn.app.ui.components.SettingsSwitchRow
import com.freeturn.app.ui.theme.Spacing
import com.freeturn.app.viewmodel.settings.SettingsViewModel

private val vkCallLink = Regex("""^https://(m\.)?vk\.(ru|com)/call/join/[A-Za-z0-9_-]+/?$""")

/**
 * "Экспериментальные функции": всё, что ещё не доросло до обычных настроек. Пока тут
 * только SFU-канал - он перекрывает провайдер любого сервера, пока включён.
 */
@Composable
fun ExperimentalScreen(
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val sfuEnabled by settingsViewModel.sfuEnabled.collectAsStateWithLifecycle()
    val sfuLink by settingsViewModel.sfuLink.collectAsStateWithLifecycle()
    val sfuPreset by settingsViewModel.sfuPreset.collectAsStateWithLifecycle()
    // Своё поле на время ввода: DataStore отвечает асинхронно и курсор бы прыгал.
    var draft by rememberSaveable { mutableStateOf<String?>(null) }
    val link = draft ?: sfuLink
    val linkBad = link.isNotBlank() && !vkCallLink.matches(link.trim())
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.settings_experimental)) },
                navigationIcon = { SettingsBackButton(onBack) },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = SettingsContentMaxWidth)
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                SettingsCard {
                    SettingsSwitchRow(
                        title = stringResource(R.string.sfu_title),
                        subtitle = stringResource(R.string.sfu_desc),
                        iconRes = R.drawable.videocam_24px,
                        checked = sfuEnabled,
                        onCheckedChange = { settingsViewModel.setSfuEnabled(it) }
                    )
                }

                if (sfuEnabled) {
                    LabeledTextField(
                        value = link,
                        onValueChange = {
                            draft = it
                            settingsViewModel.setSfuLink(it.trim())
                        },
                        labelRes = R.string.sfu_link_label,
                        placeholderRes = R.string.sfu_link_hint,
                        supportingRes = R.string.sfu_apply_note,
                        errorRes = R.string.sfu_link_error,
                        isError = linkBad,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                    SettingsCard {
                        Column(
                            modifier = Modifier.padding(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Text(
                                text = stringResource(R.string.sfu_preset_title),
                                style = MaterialTheme.typography.titleMedium
                            )
                            ConnectedChoiceRow(
                                options = listOf(
                                    ChoiceOption(SfuPreset.SAVER, "60 fps"),
                                    ChoiceOption(SfuPreset.BALANCED, "120 fps"),
                                    ChoiceOption(SfuPreset.LATENCY, "240 fps")
                                ),
                                selected = sfuPreset,
                                onSelect = { settingsViewModel.setSfuPreset(it) }
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.settings_experimental_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

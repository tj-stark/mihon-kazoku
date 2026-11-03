package eu.kanade.presentation.more.settings.screen.data

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.ParentPinDialog
import eu.kanade.presentation.components.ParentPinDialogMode
import eu.kanade.presentation.components.WarningBanner
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.core.security.ParentalStateManager
import eu.kanade.tachiyomi.core.security.SecurityPreferences
import eu.kanade.tachiyomi.data.backup.BackupFileValidator
import eu.kanade.tachiyomi.data.backup.models.BackupParentalControls
import eu.kanade.tachiyomi.data.backup.restore.BackupRestoreWorker
import eu.kanade.tachiyomi.data.backup.restore.RestoreOptions
import eu.kanade.tachiyomi.util.system.DeviceUtil
import eu.kanade.tachiyomi.util.system.workManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Security
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.LabeledCheckbox
import tachiyomi.presentation.core.components.LazyColumnWithAction
import tachiyomi.presentation.core.components.SectionCard
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource

class RestoreBackupScreen(
    private val uri: String,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel =
            assistedMetroViewModel<RestoreBackupViewModel, RestoreBackupViewModel.Factory> {
                create(uri = uri)
            }
        val state by viewModel.state.collectAsState()
        var showPinDialog by remember { mutableStateOf(false) }

        if (showPinDialog) {
            ParentPinDialog(
                mode = ParentPinDialogMode.UNLOCK,
                onDismissRequest = { showPinDialog = false },
                onSuccess = {
                    showPinDialog = false
                    viewModel.setPinVerified()
                },
                customVerifier = { pin -> viewModel.verifyPin(pin) },
            )
        }

        Scaffold(
            topBar = {
                AppBar(
                    title = stringResource(MR.strings.pref_restore_backup),
                    navigateUp = navigator::pop,
                    scrollBehavior = it,
                )
            },
        ) { contentPadding ->
            LazyColumnWithAction(
                contentPadding = contentPadding,
                actionLabel = stringResource(MR.strings.action_restore),
                actionEnabled = state.canRestore &&
                    state.options.canRestore() &&
                    (!state.isPinRequired || state.isPinVerified),
                onClickAction = {
                    viewModel.startRestore()
                    navigator.pop()
                },
            ) {
                if (DeviceUtil.isMiui && DeviceUtil.isMiuiOptimizationDisabled()) {
                    item {
                        WarningBanner(MR.strings.restore_miui_warning)
                    }
                }

                if (state.isPinRequired) {
                    item {
                        SectionCard {
                            Column(
                                modifier = Modifier.padding(MaterialTheme.padding.medium),
                                verticalArrangement = Arrangement.spacedBy(
                                    MaterialTheme.padding.small,
                                ),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Security,
                                        contentDescription = null,
                                        tint = if (state.isPinVerified) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        },
                                    )
                                    Text(
                                        text = stringResource(MR.strings.parental_controls),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }

                                if (state.isPinVerified) {
                                    Text(
                                        text = "✓ Parental PIN verified. Restoration authorized.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                } else {
                                    val promptText = if (state.parentalControls?.enabled == true) {
                                        "This backup is protected by Parental Controls. " +
                                            "You must enter the Parental PIN to restore."
                                    } else {
                                        "Parental Controls are active on this device. " +
                                            "You must enter your PIN to authorize restoration."
                                    }
                                    Text(
                                        text = promptText,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(
                                        onClick = { showPinDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text(text = stringResource(MR.strings.parental_enter_pin))
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.canRestore) {
                    item {
                        SectionCard {
                            RestoreOptions.options.forEach { option ->
                                LabeledCheckbox(
                                    label = stringResource(option.label),
                                    checked = option.getter(state.options),
                                    onCheckedChange = {
                                        viewModel.toggle(option.setter, it)
                                    },
                                )
                            }
                        }
                    }
                }

                if (state.error != null) {
                    errorMessageItem(state.error)
                }
            }
        }
    }

    private fun LazyListScope.errorMessageItem(
        error: Any?,
    ) {
        item {
            SectionCard {
                Column(
                    modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                ) {
                    val msg = buildAnnotatedString {
                        when (error) {
                            is MissingRestoreComponents -> {
                                appendLine(stringResource(MR.strings.backup_restore_content_full))
                                if (error.sources.isNotEmpty()) {
                                    appendLine()
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        appendLine(
                                            stringResource(
                                                MR.strings.backup_restore_missing_sources,
                                            ),
                                        )
                                    }
                                    error.sources.joinTo(
                                        this,
                                        separator = "\n- ",
                                        prefix = "- ",
                                    )
                                }
                                if (error.trackers.isNotEmpty()) {
                                    appendLine()
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        appendLine(
                                            stringResource(
                                                MR.strings.backup_restore_missing_trackers,
                                            ),
                                        )
                                    }
                                    error.trackers.joinTo(
                                        this,
                                        separator = "\n- ",
                                        prefix = "- ",
                                    )
                                }
                            }

                            is InvalidRestore -> {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    appendLine(stringResource(MR.strings.invalid_backup_file))
                                }
                                appendLine(error.uri.toString())

                                appendLine()

                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    appendLine(
                                        stringResource(
                                            MR.strings.invalid_backup_file_error,
                                        ),
                                    )
                                }
                                appendLine(error.message)
                            }

                            else -> {
                                appendLine(error.toString())
                            }
                        }
                    }

                    SelectionContainer {
                        Text(text = msg)
                    }
                }
            }
        }
    }
}

@AssistedInject
class RestoreBackupViewModel(
    @Assisted private val uri: String,
    private val backupFileValidator: BackupFileValidator,
    private val context: Context,
    private val securityPreferences: SecurityPreferences,
) : ViewModel() {

    val state: StateFlow<RestoreBackupViewModel.State>
        field = MutableStateFlow<RestoreBackupViewModel.State>(State())

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(uri: String): RestoreBackupViewModel
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            validate(uri.toUri())
        }
    }

    fun toggle(setter: (RestoreOptions, Boolean) -> RestoreOptions, enabled: Boolean) {
        state.update {
            it.copy(
                options = setter(it.options, enabled),
            )
        }
    }

    fun verifyPin(pin: String): Boolean {
        val backupParental = state.value.parentalControls
        val verified = if (backupParental != null &&
            backupParental.enabled &&
            backupParental.pinHash.isNotBlank()
        ) {
            ParentalStateManager.verifyPin(
                pin,
                backupParental.pinSalt,
                backupParental.pinHash,
            )
        } else {
            ParentalStateManager.unlockWithPin(pin, securityPreferences)
        }
        if (verified) {
            setPinVerified()
        }
        return verified
    }

    fun setPinVerified() {
        state.update { it.copy(isPinVerified = true) }
    }

    fun startRestore() {
        BackupRestoreWorker.start(
            workManager = context.workManager,
            uri = uri.toUri(),
            options = state.value.options,
        )
    }

    private suspend fun validate(uri: Uri) {
        val results = try {
            backupFileValidator.validate(uri)
        } catch (e: Exception) {
            setError(
                error = InvalidRestore(uri, e.message.toString()),
                canRestore = false,
                parentalControls = null,
                isPinRequired = false,
            )
            return
        }

        val backupParental = results.parentalControls
        val isBackupLocked = backupParental != null &&
            backupParental.enabled &&
            backupParental.pinHash.isNotBlank()
        val isDeviceLocked = ParentalStateManager.isEnabled(securityPreferences)
        val isPinRequired = isBackupLocked || isDeviceLocked

        if (results.missingSources.isNotEmpty() || results.missingTrackers.isNotEmpty()) {
            setError(
                error = MissingRestoreComponents(
                    uri,
                    results.missingSources,
                    results.missingTrackers,
                ),
                canRestore = true,
                parentalControls = backupParental,
                isPinRequired = isPinRequired,
            )
            return
        }

        setError(
            error = null,
            canRestore = true,
            parentalControls = backupParental,
            isPinRequired = isPinRequired,
        )
    }

    private fun setError(
        error: Any?,
        canRestore: Boolean,
        parentalControls: BackupParentalControls?,
        isPinRequired: Boolean,
    ) {
        state.update {
            it.copy(
                error = error,
                canRestore = canRestore,
                parentalControls = parentalControls,
                isPinRequired = isPinRequired,
                isPinVerified = false,
            )
        }
    }

    @Immutable
    data class State(
        val error: Any? = null,
        val canRestore: Boolean = false,
        val options: RestoreOptions = RestoreOptions(),
        val parentalControls: BackupParentalControls? = null,
        val isPinRequired: Boolean = false,
        val isPinVerified: Boolean = false,
    )
}

private data class MissingRestoreComponents(
    val uri: Uri,
    val sources: List<String>,
    val trackers: List<String>,
)

private data class InvalidRestore(
    val uri: Uri? = null,
    val message: String,
)

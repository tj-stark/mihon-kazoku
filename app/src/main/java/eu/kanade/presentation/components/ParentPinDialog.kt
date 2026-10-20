package eu.kanade.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.core.security.ParentalStateManager
import eu.kanade.tachiyomi.core.security.SecurityPreferences
import mihon.app.di.appGraph
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

enum class ParentPinDialogMode {
    UNLOCK,
    SET_PIN,
}

@Composable
fun ParentPinDialog(
    mode: ParentPinDialogMode,
    onDismissRequest: () -> Unit,
    onSuccess: () -> Unit,
) {
    val context = LocalContext.current
    val securityPreferences: SecurityPreferences = remember { context.appGraph.securityPreferences }

    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val incorrectPinText = stringResource(MR.strings.parental_pin_incorrect)
    val pinMismatchText = stringResource(MR.strings.parental_pin_mismatch)

    val onConfirm = {
        when (mode) {
            ParentPinDialogMode.UNLOCK -> {
                if (ParentalStateManager.unlockWithPin(pin, securityPreferences)) {
                    onSuccess()
                    onDismissRequest()
                } else {
                    errorMessage = incorrectPinText
                }
            }
            ParentPinDialogMode.SET_PIN -> {
                if (pin.length < 4) {
                    errorMessage = "PIN must be at least 4 digits"
                } else if (pin != confirmPin) {
                    errorMessage = pinMismatchText
                } else {
                    ParentalStateManager.setPin(pin, securityPreferences)
                    onSuccess()
                    onDismissRequest()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(
                    if (mode == ParentPinDialogMode.UNLOCK) {
                        MR.strings.parental_enter_pin
                    } else {
                        MR.strings.parental_set_pin
                    },
                ),
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (mode == ParentPinDialogMode.UNLOCK) {
                    Text(
                        text = stringResource(MR.strings.parental_locked_explanation),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        pin = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = stringResource(
                                if (mode == ParentPinDialogMode.UNLOCK) {
                                    MR.strings.parental_enter_pin
                                } else {
                                    MR.strings.parental_set_pin
                                },
                            ),
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = if (mode == ParentPinDialogMode.UNLOCK) ImeAction.Done else ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onConfirm() },
                    ),
                    singleLine = true,
                    isError = errorMessage != null,
                )

                if (mode == ParentPinDialogMode.SET_PIN) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            confirmPin = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(MR.strings.parental_confirm_pin)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onConfirm() },
                        ),
                        singleLine = true,
                        isError = errorMessage != null,
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(
                        if (mode == ParentPinDialogMode.UNLOCK) {
                            MR.strings.action_ok
                        } else {
                            MR.strings.action_save
                        },
                    ),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(MR.strings.action_cancel))
            }
        },
    )
}

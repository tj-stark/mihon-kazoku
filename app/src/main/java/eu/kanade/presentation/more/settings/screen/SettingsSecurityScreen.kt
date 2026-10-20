package eu.kanade.presentation.more.settings.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import eu.kanade.presentation.components.ParentPinDialog
import eu.kanade.presentation.components.ParentPinDialogMode
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.tachiyomi.core.security.ParentalStateManager
import eu.kanade.tachiyomi.core.security.PrivacyPreferences
import eu.kanade.tachiyomi.core.security.SecurityPreferences
import eu.kanade.tachiyomi.util.system.AuthenticatorUtil.authenticate
import eu.kanade.tachiyomi.util.system.AuthenticatorUtil.isAuthenticationSupported
import eu.kanade.tachiyomi.util.system.telemetryIncluded
import mihon.app.di.appGraph
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

object SettingsSecurityScreen : SearchableSettings {

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = MR.strings.pref_category_security

    @Composable
    override fun getPreferences(): List<Preference> {
        val context = LocalContext.current
        val securityPreferences = remember { context.appGraph.securityPreferences }
        val privacyPreferences = remember { context.appGraph.privacyPreferences }
        return buildList {
            add(getParentalControlsGroup(securityPreferences))
            add(getSecurityGroup(securityPreferences))
            if (!telemetryIncluded) return@buildList
            add(getFirebaseGroup(privacyPreferences))
        }
    }

    @Composable
    private fun getParentalControlsGroup(
        securityPreferences: SecurityPreferences,
    ): Preference.PreferenceGroup {
        val enabledPref = securityPreferences.parentalControlsEnabled
        val isEnabled by enabledPref.collectAsState()
        val hasPin = securityPreferences.parentalPinHash.get().isNotBlank()
        var showPinDialog by remember { mutableStateOf(false) }

        if (showPinDialog) {
            ParentPinDialog(
                mode = ParentPinDialogMode.SET_PIN,
                onDismissRequest = { showPinDialog = false },
                onSuccess = { showPinDialog = false },
            )
        }

        return Preference.PreferenceGroup(
            title = stringResource(MR.strings.parental_controls),
            preferenceItems = listOf(
                Preference.PreferenceItem.SwitchPreference(
                    preference = enabledPref,
                    title = stringResource(MR.strings.parental_controls),
                    subtitle = stringResource(MR.strings.parental_controls_summary),
                    onValueChanged = { newValue ->
                        if (newValue && !hasPin) {
                            showPinDialog = true
                            false
                        } else {
                            if (!newValue) {
                                ParentalStateManager.disable(securityPreferences)
                            }
                            true
                        }
                    },
                ),
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(
                        if (hasPin) MR.strings.parental_change_pin else MR.strings.parental_set_pin,
                    ),
                    visible = isEnabled || !hasPin,
                    onClick = { showPinDialog = true },
                ),
                Preference.PreferenceItem.ListPreference(
                    preference = securityPreferences.parentalSessionTimeout,
                    entries = listOf(0, 1, 5, 10, 15, 30)
                        .associateWith {
                            when (it) {
                                0 -> stringResource(MR.strings.lock_always)
                                else -> pluralStringResource(MR.plurals.lock_after_mins, count = it, it)
                            }
                        },
                    title = stringResource(MR.strings.parental_session_timeout),
                    subtitle = stringResource(MR.strings.parental_session_timeout_summary),
                    visible = isEnabled && hasPin,
                ),
            ),
        )
    }

    @Composable
    private fun getSecurityGroup(
        securityPreferences: SecurityPreferences,
    ): Preference.PreferenceGroup {
        val context = LocalContext.current
        val authSupported = remember { context.isAuthenticationSupported() }
        val useAuthPref = securityPreferences.useAuthenticator
        val useAuth by useAuthPref.collectAsState()

        return Preference.PreferenceGroup(
            title = stringResource(MR.strings.pref_security),
            preferenceItems = listOf(
                Preference.PreferenceItem.SwitchPreference(
                    preference = useAuthPref,
                    title = stringResource(MR.strings.lock_with_biometrics),
                    visible = authSupported,
                    onValueChanged = {
                        (context as FragmentActivity).authenticate(
                            title = context.stringResource(MR.strings.lock_with_biometrics),
                        )
                    },
                ),
                Preference.PreferenceItem.ListPreference(
                    preference = securityPreferences.lockAppAfter,
                    entries = LockAfterValues
                        .associateWith {
                            when (it) {
                                -1 -> stringResource(MR.strings.lock_never)
                                0 -> stringResource(MR.strings.lock_always)
                                else -> pluralStringResource(MR.plurals.lock_after_mins, count = it, it)
                            }
                        },
                    title = stringResource(MR.strings.lock_when_idle),
                    visible = authSupported && useAuth,
                    onValueChanged = {
                        (context as FragmentActivity).authenticate(
                            title = context.stringResource(MR.strings.lock_when_idle),
                        )
                    },
                ),

                Preference.PreferenceItem.SwitchPreference(
                    preference = securityPreferences.hideNotificationContent,
                    title = stringResource(MR.strings.hide_notification_content),
                ),
                Preference.PreferenceItem.ListPreference(
                    preference = securityPreferences.secureScreen,
                    entries = SecurityPreferences.SecureScreenMode.entries
                        .associateWith { stringResource(it.titleRes) },
                    title = stringResource(MR.strings.secure_screen),
                ),
                Preference.PreferenceItem.InfoPreference(stringResource(MR.strings.secure_screen_summary)),
            ),
        )
    }

    @Composable
    private fun getFirebaseGroup(
        privacyPreferences: PrivacyPreferences,
    ): Preference.PreferenceGroup {
        return Preference.PreferenceGroup(
            title = stringResource(MR.strings.pref_firebase),
            preferenceItems = listOf(
                Preference.PreferenceItem.SwitchPreference(
                    preference = privacyPreferences.crashlytics,
                    title = stringResource(MR.strings.onboarding_permission_crashlytics),
                    subtitle = stringResource(MR.strings.onboarding_permission_crashlytics_description),
                ),
                Preference.PreferenceItem.SwitchPreference(
                    preference = privacyPreferences.analytics,
                    title = stringResource(MR.strings.onboarding_permission_analytics),
                    subtitle = stringResource(MR.strings.onboarding_permission_analytics_description),
                ),
                Preference.PreferenceItem.InfoPreference(stringResource(MR.strings.firebase_summary)),
            ),
        )
    }
}

private val LockAfterValues = listOf(
    0, // Always
    1,
    2,
    5,
    10,
    -1, // Never
)

package eu.kanade.presentation.library.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import eu.kanade.tachiyomi.core.security.ParentalStateManager
import mihon.app.di.appGraph
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
internal fun GlobalSearchItem(
    searchQuery: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val securityPreferences = remember { context.appGraph.securityPreferences }
    val isParentModeActive by ParentalStateManager.isParentModeActive.collectAsState()

    if (ParentalStateManager.isEnabled(securityPreferences) && !isParentModeActive) {
        return
    }

    TextButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Text(
            text = stringResource(MR.strings.action_global_search_query, searchQuery),
            modifier = Modifier.zIndex(99f),
        )
    }
}

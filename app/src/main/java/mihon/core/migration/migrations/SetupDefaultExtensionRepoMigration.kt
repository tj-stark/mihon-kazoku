package mihon.core.migration.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import eu.kanade.domain.source.service.SourcePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logcat.LogPriority
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import mihon.domain.extension.model.ExtensionStore
import mihon.domain.extension.repository.ExtensionStoreRepository
import tachiyomi.core.common.util.system.logcat

@Inject
@ContributesIntoSet(AppScope::class)
class SetupDefaultExtensionRepoMigration(
    private val sourcePreferences: SourcePreferences,
    private val repository: ExtensionStoreRepository,
) : Migration {
    override val version: Float = Migration.ALWAYS

    override suspend fun invoke(migrationContext: MigrationContext): Boolean = withContext(Dispatchers.IO) {
        if (!sourcePreferences.defaultExtensionRepoInitialized.get()) {
            try {
                if (repository.getAll().isEmpty()) {
                    repository.upsert(
                        ExtensionStore(
                            indexUrl = "https://github.com/keiyoushi/extensions/raw/repo/index.pb",
                            name = "Keiyoushi",
                            badgeLabel = "Keiyoushi",
                            signingKey = "9add655a78e96c4ec7a53ef89dccb557cb5d767489fac5e785d671a5a75d4da2",
                            contact = ExtensionStore.Contact(
                                website = "https://keiyoushi.github.io",
                                discord = "https://discord.gg/3FbCpdKbdY",
                            ),
                            isLegacy = false,
                            extensionListUrl = null,
                        ),
                    )
                    logcat(LogPriority.INFO) { "Initialized default Keiyoushi extension repository" }
                }
            } catch (e: Exception) {
                logcat(LogPriority.ERROR, e) { "Failed to initialize default extension repository" }
            } finally {
                sourcePreferences.defaultExtensionRepoInitialized.set(true)
            }
        }
        return@withContext true
    }
}

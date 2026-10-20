package eu.kanade.tachiyomi.core.security

import eu.kanade.tachiyomi.util.lang.Hash
import java.security.SecureRandom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ParentalStateManager {

    private val _isParentModeActive = MutableStateFlow(false)
    val isParentModeActive: StateFlow<Boolean> = _isParentModeActive.asStateFlow()

    private var unlockedAtTimestamp: Long = 0L

    private val random = SecureRandom()

    fun isEnabled(preferences: SecurityPreferences): Boolean {
        return preferences.parentalControlsEnabled.get() && preferences.parentalPinHash.get().isNotBlank()
    }

    fun isParentMode(preferences: SecurityPreferences): Boolean {
        return !isEnabled(preferences) || _isParentModeActive.value
    }

    fun unlockWithPin(pin: String, preferences: SecurityPreferences): Boolean {
        if (!isEnabled(preferences)) {
            _isParentModeActive.value = true
            unlockedAtTimestamp = System.currentTimeMillis()
            return true
        }

        val salt = preferences.parentalPinSalt.get()
        val storedHash = preferences.parentalPinHash.get()
        val computedHash = Hash.sha256(salt + pin)

        if (computedHash.equals(storedHash, ignoreCase = true)) {
            _isParentModeActive.value = true
            unlockedAtTimestamp = System.currentTimeMillis()
            return true
        }
        return false
    }

    fun lock() {
        _isParentModeActive.value = false
        unlockedAtTimestamp = 0L
    }

    fun checkTimeout(preferences: SecurityPreferences) {
        if (!_isParentModeActive.value) return
        val timeoutMinutes = preferences.parentalSessionTimeout.get()
        if (timeoutMinutes <= 0) {
            lock()
            return
        }
        val elapsed = System.currentTimeMillis() - unlockedAtTimestamp
        if (elapsed >= timeoutMinutes * 60_000L) {
            lock()
        }
    }

    fun setPin(newPin: String, preferences: SecurityPreferences) {
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        val salt = saltBytes.joinToString("") { "%02x".format(it) }
        val hash = Hash.sha256(salt + newPin)

        preferences.parentalPinSalt.set(salt)
        preferences.parentalPinHash.set(hash)
        preferences.parentalControlsEnabled.set(true)
        _isParentModeActive.value = true
        unlockedAtTimestamp = System.currentTimeMillis()
    }

    fun disable(preferences: SecurityPreferences) {
        preferences.parentalControlsEnabled.set(false)
        preferences.parentalPinHash.set("")
        preferences.parentalPinSalt.set("")
        lock()
    }
}

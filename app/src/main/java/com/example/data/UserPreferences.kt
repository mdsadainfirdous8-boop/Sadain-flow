package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom

private val Context.hisabDataStore: DataStore<Preferences> by preferencesDataStore(name = "hisab_preferences")

data class HisabSettings(
    val openingBalancePaise: Long = 0L,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isPinEnabled: Boolean = false,
    val pinHash: String = "",
    val pinSalt: String = "",
    val isDemoMode: Boolean = false
)

class UserPreferences(private val context: Context) {
    private object Keys {
        val OPENING_BALANCE_PAISE = longPreferencesKey("opening_balance_paise")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_PIN_ENABLED = booleanPreferencesKey("is_pin_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val IS_DEMO_MODE = booleanPreferencesKey("is_demo_mode")
    }

    val settingsFlow: Flow<HisabSettings> = context.hisabDataStore.data.map { prefs ->
        HisabSettings(
            openingBalancePaise = prefs[Keys.OPENING_BALANCE_PAISE] ?: 0L,
            themeMode = ThemeMode.fromString(prefs[Keys.THEME_MODE]),
            isPinEnabled = prefs[Keys.IS_PIN_ENABLED] ?: false,
            pinHash = prefs[Keys.PIN_HASH] ?: "",
            pinSalt = prefs[Keys.PIN_SALT] ?: "",
            isDemoMode = prefs[Keys.IS_DEMO_MODE] ?: false
        )
    }

    suspend fun setOpeningBalancePaise(paise: Long) {
        context.hisabDataStore.edit { prefs ->
            prefs[Keys.OPENING_BALANCE_PAISE] = paise
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.hisabDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDemoMode(enabled: Boolean) {
        context.hisabDataStore.edit { prefs ->
            prefs[Keys.IS_DEMO_MODE] = enabled
        }
    }

    suspend fun enablePin(pin: String) {
        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)
        val saltHex = saltBytes.joinToString("") { "%02x".format(it) }
        val hash = hashPin(pin, saltHex)
        context.hisabDataStore.edit { prefs ->
            prefs[Keys.PIN_SALT] = saltHex
            prefs[Keys.PIN_HASH] = hash
            prefs[Keys.IS_PIN_ENABLED] = true
        }
    }

    suspend fun disablePin() {
        context.hisabDataStore.edit { prefs ->
            prefs[Keys.IS_PIN_ENABLED] = false
            prefs[Keys.PIN_HASH] = ""
            prefs[Keys.PIN_SALT] = ""
        }
    }

    fun verifyPin(pin: String, settings: HisabSettings): Boolean {
        if (!settings.isPinEnabled || settings.pinHash.isEmpty()) return true
        return hashPin(pin, settings.pinSalt) == settings.pinHash
    }

    private fun hashPin(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$salt:$pin".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

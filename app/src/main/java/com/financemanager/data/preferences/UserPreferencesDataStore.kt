package com.financemanager.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.financemanager.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "finance_user_preferences",
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserPreferencesRepository {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val PROFILE_DISPLAY_NAME = stringPreferencesKey("profile_display_name")
        val PROFILE_EMAIL = stringPreferencesKey("profile_email")
    }

    override suspend fun setThemeMode(mode: String) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }

    override fun observeThemeMode(): Flow<String> =
        context.userPrefsDataStore.data.map { prefs ->
            prefs[Keys.THEME_MODE] ?: "DARK"
        }

    override suspend fun setOnboardingComplete(done: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_DONE] = done
        }
    }

    override fun observeOnboardingComplete(): Flow<Boolean> =
        context.userPrefsDataStore.data.map { prefs ->
            prefs[Keys.ONBOARDING_DONE] ?: false
        }

    override suspend fun setProfileDisplayName(name: String) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.PROFILE_DISPLAY_NAME] = name.trim()
        }
    }

    override fun observeProfileDisplayName(): Flow<String> =
        context.userPrefsDataStore.data.map { prefs ->
            prefs[Keys.PROFILE_DISPLAY_NAME] ?: ""
        }

    override suspend fun setProfileEmail(email: String) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.PROFILE_EMAIL] = email.trim()
        }
    }

    override fun observeProfileEmail(): Flow<String> =
        context.userPrefsDataStore.data.map { prefs ->
            prefs[Keys.PROFILE_EMAIL] ?: ""
        }
}

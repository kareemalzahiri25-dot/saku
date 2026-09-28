package com.example.data.service

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * Secure API Key Configuration Service using EncryptedSharedPreferences backed by Android Keystore.
 * Migrates existing plaintext keys from DefaultApiKeyConfigService on first run.
 */
class EncryptedApiKeyConfigService(context: Context) : ApiKeyConfigService {

    private val prefs: SharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "saku_api_config_encrypted",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val legacyPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("saku_api_config", Context.MODE_PRIVATE)
    }

    private var migrationDone = false

    private fun ensureMigration() {
        if (migrationDone) return
        migrationDone = true

        // Migrate Gemini API key if exists in legacy and not in encrypted
        val legacyGemini = legacyPrefs.getString("gemini_api_key", null)
        if (!legacyGemini.isNullOrBlank()) {
            val currentEncrypted = prefs.getString("gemini_api_key", null)
            if (currentEncrypted.isNullOrBlank()) {
                prefs.edit().putString("gemini_api_key", legacyGemini.trim()).apply()
            }
        }

        // Migrate OCR API key if exists in legacy and not in encrypted
        val legacyOcr = legacyPrefs.getString("ocr_api_key", null)
        if (!legacyOcr.isNullOrBlank()) {
            val currentEncrypted = prefs.getString("ocr_api_key", null)
            if (currentEncrypted.isNullOrBlank()) {
                prefs.edit().putString("ocr_api_key", legacyOcr.trim()).apply()
            }
        }
    }

    companion object {
        private const val KEY_GEMINI_API = "gemini_api_key"
        private const val KEY_OCR_API = "ocr_api_key"
    }

    override fun getGeminiApiKey(): String? {
        ensureMigration()
        val stored = prefs.getString(KEY_GEMINI_API, null)
        return if (!stored.isNullOrBlank()) stored else null
    }

    override fun setGeminiApiKey(key: String) {
        ensureMigration()
        prefs.edit().putString(KEY_GEMINI_API, key.trim()).apply()
    }

    override fun isGeminiConfigured(): Boolean {
        return !getGeminiApiKey().isNullOrBlank()
    }

    override fun getOcrApiKey(): String? {
        ensureMigration()
        val stored = prefs.getString(KEY_OCR_API, null)
        return if (!stored.isNullOrBlank()) stored else null
    }

    override fun setOcrApiKey(key: String) {
        ensureMigration()
        prefs.edit().putString(KEY_OCR_API, key.trim()).apply()
    }
}
package com.example.data.service

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.screens.profile.ApiKeyVerificationStatus

/**
 * Layanan Konfigurasi Kunci API (API-Key Configuration)
 * Menghubungkan penyimpanan lokal yang aman atau BuildConfig/Secrets panel
 */
interface ApiKeyConfigService {
    fun getGeminiApiKey(): String?
    fun setGeminiApiKey(key: String)
    fun isGeminiConfigured(): Boolean
    fun getVerificationStatus(): ApiKeyVerificationStatus
    fun setVerificationStatus(status: ApiKeyVerificationStatus)
    fun getOcrApiKey(): String?
    fun setOcrApiKey(key: String)
}

class DefaultApiKeyConfigService(context: Context) : ApiKeyConfigService {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("saku_api_config", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GEMINI_API = "gemini_api_key"
        private const val KEY_OCR_API = "ocr_api_key"
        private const val KEY_VERIFICATION_STATUS = "verification_status"
    }

    override fun getGeminiApiKey(): String? {
        val stored = prefs.getString(KEY_GEMINI_API, null)
        return if (!stored.isNullOrBlank()) stored.trim() else null
    }

    override fun setGeminiApiKey(key: String) {
        prefs.edit().putString(KEY_GEMINI_API, key.trim()).apply()
    }

    override fun isGeminiConfigured(): Boolean {
        return !getGeminiApiKey().isNullOrBlank()
    }

    override fun getVerificationStatus(): ApiKeyVerificationStatus {
        val stored = prefs.getString(KEY_VERIFICATION_STATUS, null)
        return stored?.let { ApiKeyVerificationStatus.valueOf(it) } ?: ApiKeyVerificationStatus.Unknown
    }

    override fun setVerificationStatus(status: ApiKeyVerificationStatus) {
        prefs.edit().putString(KEY_VERIFICATION_STATUS, status.name).apply()
    }

    override fun getOcrApiKey(): String? {
        val stored = prefs.getString(KEY_OCR_API, null)
        return if (!stored.isNullOrBlank()) stored else null
    }

    override fun setOcrApiKey(key: String) {
        prefs.edit().putString(KEY_OCR_API, key.trim()).apply()
    }
}

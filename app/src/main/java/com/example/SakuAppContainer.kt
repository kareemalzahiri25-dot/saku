package com.example

import android.content.Context
import com.example.data.local.SakuDatabase
import com.example.data.repository.SakuRepositoryImpl
import com.example.data.service.ApiKeyConfigService
import com.example.data.service.CurrencyConversionService
import com.example.data.service.DefaultApiKeyConfigService
import com.example.data.service.DefaultCurrencyConversionService
import com.example.data.service.DefaultReceiptScannerService
import com.example.data.service.ReceiptScannerService
import com.example.domain.repository.SakuRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class SakuAppContainer(context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: SakuDatabase = SakuDatabase.getDatabase(context, applicationScope)
    val repository: SakuRepository = SakuRepositoryImpl(database)
    val apiKeyConfigService: ApiKeyConfigService = DefaultApiKeyConfigService(context)
    val receiptScannerService: ReceiptScannerService = DefaultReceiptScannerService(apiKeyConfigService)
    val currencyConversionService: CurrencyConversionService = DefaultCurrencyConversionService()
}

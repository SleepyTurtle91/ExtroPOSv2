package com.extrotarget.extroposv2.core.network.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiProvider @Inject constructor() {

    /**
     * Provides a configured GenerativeModel instance.
     * In a real app, the API Key should be retrieved from a secure backend or EncryptedSharedPreferences.
     */
    fun getModel(apiKey: String): GenerativeModel {
        val config = generationConfig {
            temperature = 0.1f // Low temperature for precise auditing/repair tasks
            topK = 32
            topP = 0.95f
            maxOutputTokens = 2048
        }

        return GenerativeModel(
            modelName = "gemini-3.1-flash-lite", // Updated to 3.1 Flash-Lite as per user request
            apiKey = apiKey,
            generationConfig = config
        )
    }
}

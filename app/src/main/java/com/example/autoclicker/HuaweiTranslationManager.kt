package com.example.autoclicker

import android.content.Context
import android.util.Log
import com.huawei.hms.mlsdk.model.download.MLModelDownloadListener
import com.huawei.hms.mlsdk.model.download.MLModelDownloadStrategy
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslateSetting
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslator
import java.util.concurrent.ConcurrentHashMap

object HuaweiTranslationManager {
    private const val TAG = "HuaweiTranslator"

    private val translators = ConcurrentHashMap<String, MLLocalTranslator>()
    private val readyModels = ConcurrentHashMap<String, Boolean>()

    private fun getPairKey(src: String, target: String): String = "${src.lowercase()}_${target.lowercase()}"

    fun getLocalTranslator(srcLang: String, targetLang: String): MLLocalTranslator {
        val key = getPairKey(srcLang, targetLang)
        return translators.getOrPut(key) {
            val setting = MLLocalTranslateSetting.Factory()
                .setSourceLangCode(srcLang.lowercase())
                .setTargetLangCode(targetLang.lowercase())
                .create()
            MLTranslatorFactory.getInstance().getLocalTranslator(setting)
        }
    }

    /**
     * Downloads and prepares the on-device ML translation model.
     * Allows cellular data by default so offline model can be fetched on demand.
     */
    fun prepareModel(
        srcLang: String,
        targetLang: String,
        onProgress: ((Long, Long) -> Unit)? = null,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null
    ) {
        val key = getPairKey(srcLang, targetLang)
        val translator = getLocalTranslator(srcLang, targetLang)

        val strategy = MLModelDownloadStrategy.Factory()
            .needWifi() // Wi-Fi preferred, can download in background
            .create()

        val listener = object : MLModelDownloadListener {
            override fun onProcess(alreadyDownLength: Long, totalLength: Long) {
                onProgress?.invoke(alreadyDownLength, totalLength)
            }
        }

        translator.preparedModel(strategy, listener)
            .addOnSuccessListener {
                readyModels[key] = true
                Log.i(TAG, "Offline translation model ready: $key")
                onSuccess?.invoke()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Offline translation model prepare failed: $key - ${e.message}")
                onFailure?.invoke(e)
            }
    }

    /**
     * Translates arbitrary text using Huawei On-Device ML translation.
     * Falls back to OfflineTranslationEngine if the ML model is not yet prepared or in case of error.
     */
    fun translate(
        text: String,
        srcLang: String,
        targetLang: String,
        onResult: (String) -> Unit
    ) {
        val cleanSrc = when (srcLang) {
            "mix" -> "de" // For mixed texts with German/English, attempt source
            else -> srcLang.lowercase()
        }
        val cleanTarget = targetLang.lowercase()

        if (text.isBlank() || cleanSrc == cleanTarget) {
            onResult(text)
            return
        }

        try {
            val translator = getLocalTranslator(cleanSrc, cleanTarget)
            translator.asyncTranslate(text)
                .addOnSuccessListener { translatedText ->
                    if (!translatedText.isNullOrBlank()) {
                        onResult(translatedText)
                    } else {
                        // Fallback to phrase & dictionary engine
                        val fallback = OfflineTranslationEngine.translate(text, srcLang, targetLang)
                        onResult(fallback)
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "ML Kit asyncTranslate fallback due to: ${e.message}")
                    val fallback = OfflineTranslationEngine.translate(text, srcLang, targetLang)
                    onResult(fallback)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during ML translation: ${e.message}")
            val fallback = OfflineTranslationEngine.translate(text, srcLang, targetLang)
            onResult(fallback)
        }
    }

    fun release() {
        for ((_, translator) in translators) {
            try {
                translator.stop()
            } catch (e: Exception) {}
        }
        translators.clear()
        readyModels.clear()
    }
}

package com.example.autoclicker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.ScreenshotResult
import android.accessibilityservice.AccessibilityService.TakeScreenshotCallback
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import android.widget.Toast
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.text.MLLocalTextSetting
import com.huawei.hms.mlsdk.text.MLText
import com.huawei.hms.mlsdk.text.MLTextAnalyzer

class AutoClickService : AccessibilityService() {

    lateinit var uiManager: UIManager
    val handler = Handler(Looper.getMainLooper())

    companion object {
        var instance: AutoClickService? = null
    }

    private val ocrLock = Any()
    private var mlTextAnalyzer: MLTextAnalyzer? = null

    var targetLanguage: String = "ru"

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        try {
            uiManager = UIManager(this)
            
            val prefs = getSharedPreferences("ScreenTranslatorPrefs", MODE_PRIVATE)
            targetLanguage = prefs.getString("TargetLanguage", "ru") ?: "ru"
            uiManager.currentTargetLanguage = targetLanguage

            uiManager.showFloatingTrigger()
            Toast.makeText(this, "Переводчик экрана готов! Нажмите 文/A для перевода.", Toast.LENGTH_LONG).show()

            // Asynchronously prepare Huawei on-device neural translation models
            Thread {
                try {
                    HuaweiTranslationManager.prepareModel("en", "ru")
                    HuaweiTranslationManager.prepareModel("de", "ru")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setTargetLang(lang: String) {
        targetLanguage = lang
        uiManager.currentTargetLanguage = lang
        getSharedPreferences("ScreenTranslatorPrefs", MODE_PRIVATE)
            .edit()
            .putString("TargetLanguage", lang)
            .apply()
    }

    private fun getRealScreenSize(): Pair<Int, Int> {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.maximumWindowMetrics.bounds
            Pair(bounds.width(), bounds.height())
        } else {
            val dm = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(dm)
            Pair(dm.widthPixels, dm.heightPixels)
        }
    }

    fun scanAndTranslateScreen() {
        Toast.makeText(this, "Сканирование экрана...", Toast.LENGTH_SHORT).show()

        try {
            takeScreenshot(Display.DEFAULT_DISPLAY, mainExecutor, object : TakeScreenshotCallback {
                override fun onSuccess(screenshot: ScreenshotResult) {
                    val buffer = screenshot.hardwareBuffer
                    val hwBitmap = Bitmap.wrapHardwareBuffer(buffer, screenshot.colorSpace)
                    if (hwBitmap != null) {
                        val bitmap = hwBitmap.copy(Bitmap.Config.ARGB_8888, false)
                        hwBitmap.recycle()
                        buffer.close()

                        if (bitmap != null) {
                            processScreenshotForTranslation(bitmap)
                            return
                        }
                    }
                    buffer.close()
                    Toast.makeText(this@AutoClickService, "Не удалось получить снимок экрана", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(errorCode: Int) {
                    Toast.makeText(this@AutoClickService, "Ошибка захвата экрана: $errorCode", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processScreenshotForTranslation(bitmap: Bitmap) {
        Thread {
            try {
                val (screenW, screenH) = getRealScreenSize()
                val scaleX = screenW.toFloat() / bitmap.width
                val scaleY = screenH.toFloat() / bitmap.height

                // Native 1:1 frame input to avoid displacement or distortion
                val analyzer = getHuaweiAnalyzer("ru")
                val frame = MLFrame.fromBitmap(bitmap)

                analyzer.asyncAnalyseFrame(frame)
                    .addOnSuccessListener { result: MLText? ->
                        if (!bitmap.isRecycled) bitmap.recycle()

                        val rawItems = mutableListOf<RawOcrItem>()

                        if (result != null) {
                            var itemCounter = 0
                            for (block in result.blocks) {
                                for (line in block.contents) {
                                    val lineText = line.stringValue
                                    if (lineText.isNullOrBlank()) continue
                                    
                                    // Clean and sanitize OCR homoglyphs immediately on input
                                    val cleanText = OfflineTranslationEngine.sanitizeOcrHomoglyphs(lineText.trim())

                                    // Filter out noise (single letter headers like 'D', symbols, icons)
                                    if (LanguageDetectorOffline.isIgnorableNoise(cleanText)) {
                                        continue
                                    }

                                    val border = line.border ?: Rect(0, 0, 0, 0)
                                    // Map directly to real screen coordinates
                                    val mappedRect = Rect(
                                        (border.left * scaleX).toInt(),
                                        (border.top * scaleY).toInt(),
                                        (border.right * scaleX).toInt(),
                                        (border.bottom * scaleY).toInt()
                                    )

                                    rawItems.add(
                                        RawOcrItem(
                                            id = "line_${itemCounter++}",
                                            text = cleanText,
                                            rect = mappedRect
                                        )
                                    )
                                }
                            }
                        }

                        // Geometric clustering with language isolation (does not glue different languages or rows)
                        val clustered = GeometryHelper.clusterBlocksGeometrically(rawItems, 0.65f)

                        // Process translations & nearest corners
                        val translationBlocks = clustered.mapIndexed { index, item ->
                            val detected = LanguageDetectorOffline.detect(item.text)
                            // Check if text contains any translatable content into target language
                            val hasTranslatable = LanguageDetectorOffline.hasTranslatableContent(item.text, targetLanguage)
                            val isSkipped = !hasTranslatable

                            val nearest = GeometryHelper.calculateNearestCorner(item.rect, screenW, screenH)

                            val translated = if (isSkipped) {
                                item.text
                            } else {
                                OfflineTranslationEngine.translate(item.text, detected, targetLanguage)
                            }

                            TranslationBlock(
                                id = "block_$index",
                                originalText = item.text,
                                translatedText = translated,
                                detectedLang = detected,
                                targetLang = targetLanguage,
                                rect = item.rect,
                                nearestCorner = nearest,
                                isSkippedSameLang = isSkipped
                            )
                        }

                        handler.post {
                            if (::uiManager.isInitialized) {
                                uiManager.renderTranslationBlocks(translationBlocks)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        if (!bitmap.isRecycled) bitmap.recycle()
                        handler.post {
                            Toast.makeText(this@AutoClickService, "Ошибка распознавания: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                if (!bitmap.isRecycled) bitmap.recycle()
                handler.post {
                    Toast.makeText(this@AutoClickService, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun getHuaweiAnalyzer(lang: String): MLTextAnalyzer {
        synchronized(ocrLock) {
            val hLang = if (lang == "eng") "en" else "ru"
            if (mlTextAnalyzer != null) return mlTextAnalyzer!!

            MLApplication.getInstance().apiKey = "dummy_api_key_for_local_use_only"
            val setting = MLLocalTextSetting.Factory()
                .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
                .setLanguage(hLang)
                .create()
            mlTextAnalyzer = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting)
            return mlTextAnalyzer!!
        }
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {}

    override fun onInterrupt() {
        if (::uiManager.isInitialized) {
            uiManager.removeAllViews()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::uiManager.isInitialized) {
            uiManager.removeAllViews()
        }
        if (mlTextAnalyzer != null) {
            try { mlTextAnalyzer!!.stop() } catch (e: Exception) {}
            mlTextAnalyzer = null
        }
        handler.removeCallbacksAndMessages(null)
        instance = null
    }
}

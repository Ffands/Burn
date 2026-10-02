package com.example.autoclicker

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.widget.Toast
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.text.MLAnalyzerFactory
import com.huawei.hms.mlsdk.text.MLLocalTextSetting
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
            Toast.makeText(this, "Переводчик экрана активирован! Нажмите 文/A для перевода.", Toast.LENGTH_LONG).show()
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
                val enhanced = enhanceBitmapForOcr(bitmap)
                val analyzer = getHuaweiAnalyzer("ru")
                val frame = MLFrame.fromBitmap(enhanced)
                val task = analyzer.asyncAnalyseFrame(frame)
                val result = com.huawei.hmf.tasks.Tasks.await(task)

                if (enhanced != bitmap) enhanced.recycle()

                val rawItems = mutableListOf<RawOcrItem>()
                val metrics = resources.displayMetrics
                val screenW = metrics.widthPixels
                val screenH = metrics.heightPixels

                if (result != null) {
                    var itemCounter = 0
                    for (block in result.blocks) {
                        for (line in block.contents) {
                            val lineText = line.stringValue
                            if (lineText.isNullOrBlank()) continue
                            
                            val normText = normalizeCyrillic(lineText)
                            rawItems.add(
                                RawOcrItem(
                                    id = "line_${itemCounter++}",
                                    text = normText,
                                    rect = line.border ?: Rect(0, 0, 0, 0)
                                )
                            )
                        }
                    }
                }
                bitmap.recycle()

                // Pure geometric clustering (no punctuation dependency)
                val clustered = GeometryHelper.clusterBlocksGeometrically(rawItems, 1.25f)

                // Process translations & nearest corners
                val translationBlocks = clustered.mapIndexed { index, item ->
                    val detected = LanguageDetectorOffline.detect(item.text)
                    val isSkipped = detected == targetLanguage
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
            } catch (e: Exception) {
                e.printStackTrace()
                handler.post {
                    Toast.makeText(this@AutoClickService, "Ошибка распознавания: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    fun enhanceBitmapForOcr(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val scale = if (w < 150 || h < 150) 2f else 1.5f
        val sw = (w * scale).toInt()
        val sh = (h * scale).toInt()
        return Bitmap.createScaledBitmap(src, sw, sh, true)
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

    fun normalizeCyrillic(str: String): String {
        var s = str.lowercase().replace(Regex("\\s+"), " ").trim()

        s = s.replace("llo", "лю")
            .replace("io", "ю")
            .replace("lo", "ю")
            .replace("10", "ю")
            .replace("wa", "ща")
            .replace("sh", "ш")
            .replace("ch", "ч")
            .replace("ya", "я")
            .replace("ji", "л")
            .replace("tl", "п")
            .replace("lļ,", "ц")
            .replace("lļ", "ц")
            .replace("ll,", "ц")
            .replace("li,", "ц")
            .replace("n,", "и,")

        s = s.replace("6", "б")
            .replace("0", "о")
            .replace("3", "з")
            .replace("4", "ч")
            .replace("9", " э")
            .replace("a", "а")
            .replace("b", "ь")
            .replace("c", "с")
            .replace("e", "е")
            .replace("k", "к")
            .replace("m", "м")
            .replace("h", "н")
            .replace("o", "о")
            .replace("p", "р")
            .replace("t", "т")
            .replace("x", "х")
            .replace("y", "у")
            .replace("n", "и")
            .replace("l", "л")
            .replace("u", "и")
            .replace("ñ", "й")
            .replace("r", "г")
            .replace("ó", "ф")

        return s.replace(Regex("\\s+"), " ").trim()
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

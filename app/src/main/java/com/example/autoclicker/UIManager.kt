package com.example.autoclicker

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*

class UIManager(private val service: AutoClickService) {

    val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val density = service.resources.displayMetrics.density

    fun dpToPx(dp: Int): Int = (dp * density).toInt()

    private var floatingTriggerView: View? = null
    private var triggerParams: WindowManager.LayoutParams? = null

    private var overlayContainerView: FrameLayout? = null
    private var overlayContainerParams: WindowManager.LayoutParams? = null

    private var fullscreenDialogView: View? = null

    var currentTargetLanguage: String = "ru"

    fun showFloatingTrigger() {
        if (floatingTriggerView != null) return

        val button = TextView(service).apply {
            text = "文/A"
            setTextColor(Color.WHITE)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#2563EB")) // Blue 600
                setStroke(dpToPx(2), Color.parseColor("#38BDF8")) // Light blue ring
            }
            background = bg
            elevation = dpToPx(8).toFloat()
        }

        val size = dpToPx(56)
        triggerParams = WindowManager.LayoutParams(
            size, size,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = service.resources.displayMetrics.widthPixels - size - dpToPx(16)
            y = service.resources.displayMetrics.heightPixels / 2 - size / 2
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = triggerParams!!.x
                    initialY = triggerParams!!.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (Math.hypot(dx.toDouble(), dy.toDouble()) > dpToPx(8)) {
                        isClick = false
                    }
                    triggerParams!!.x = initialX + dx.toInt()
                    triggerParams!!.y = initialY + dy.toInt()
                    windowManager.updateViewLayout(button, triggerParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        service.scanAndTranslateScreen()
                    }
                    true
                }
                else -> false
            }
        }

        floatingTriggerView = button
        windowManager.addView(button, triggerParams)
    }

    fun removeFloatingTrigger() {
        floatingTriggerView?.let {
            windowManager.removeView(it)
            floatingTriggerView = null
        }
    }

    fun renderTranslationBlocks(blocks: List<TranslationBlock>) {
        clearTranslationOverlay()

        val container = FrameLayout(service)
        
        // FIX 1 & 2: Fullscreen spanning physical display edge-to-edge (eliminates notch/status bar offset)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }

        // Close overlay if tapped anywhere in background
        container.setOnClickListener {
            clearTranslationOverlay()
        }

        for (block in blocks) {
            val rect = block.rect

            // 1. Block boundary highlight
            val boxView = View(service).apply {
                val strokeColor = if (block.isSkippedSameLang) Color.parseColor("#4471717A") else Color.parseColor("#903B82F6")
                val fillColor = if (block.isSkippedSameLang) Color.parseColor("#1071717A") else Color.parseColor("#252563EB")

                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dpToPx(6).toFloat()
                    setStroke(dpToPx(1), strokeColor)
                    setColor(fillColor)
                }
                background = drawable
            }

            val boxParams = FrameLayout.LayoutParams(rect.width(), rect.height()).apply {
                leftMargin = rect.left
                topMargin = rect.top
            }
            container.addView(boxView, boxParams)

            // 2. Corner badge for translation (only for translatable items)
            if (!block.isSkippedSameLang) {
                val badgeSize = dpToPx(32)
                val badgeBtn = TextView(service).apply {
                    text = "文/A"
                    setTextColor(Color.WHITE)
                    textSize = 10f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER

                    val badgeBg = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.parseColor("#2563EB"))
                        setStroke(dpToPx(1), Color.WHITE)
                    }
                    background = badgeBg
                    elevation = dpToPx(6).toFloat()

                    setOnClickListener {
                        showFullscreenTranslation(block)
                    }
                }

                // Place strictly at nearest corner to center of screen
                val nearest = block.nearestCorner
                val badgeParams = FrameLayout.LayoutParams(badgeSize, badgeSize).apply {
                    leftMargin = (nearest.x - badgeSize / 2).toInt()
                    topMargin = (nearest.y - badgeSize / 2).toInt()
                }
                container.addView(badgeBtn, badgeParams)
            }
        }

        overlayContainerView = container
        overlayContainerParams = params
        windowManager.addView(container, params)
    }

    fun clearTranslationOverlay() {
        overlayContainerView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) {}
            overlayContainerView = null
        }
    }

    fun showFullscreenTranslation(block: TranslationBlock) {
        dismissFullscreenTranslation()

        val root = FrameLayout(service).apply {
            setBackgroundColor(Color.parseColor("#D9000000")) // 85% black backdrop
            setOnClickListener { dismissFullscreenTranslation() }
        }

        val card = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#171717")) // Neutral 900
                cornerRadius = dpToPx(20).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#262626"))
            }
            background = bg
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            isClickable = true
        }

        // Header
        val header = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val langTag = TextView(service).apply {
            val fromTag = when (block.detectedLang) {
                "mix" -> "СМЕШАННЫЙ"
                block.targetLang -> "СМЕШАННЫЙ"
                else -> block.detectedLang.uppercase()
            }
            text = "$fromTag → ${block.targetLang.uppercase()}"
            setTextColor(Color.parseColor("#60A5FA"))
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            val tagBg = GradientDrawable().apply {
                setColor(Color.parseColor("#1E3A8A"))
                cornerRadius = dpToPx(6).toFloat()
            }
            background = tagBg
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
        }

        val closeBtn = TextView(service).apply {
            text = "✕"
            setTextColor(Color.parseColor("#A3A3A3"))
            textSize = 18f
            setPadding(dpToPx(12), dpToPx(4), dpToPx(12), dpToPx(4))
            setOnClickListener { dismissFullscreenTranslation() }
        }

        header.addView(langTag, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(closeBtn)
        card.addView(header)

        // Translation View (Main text)
        val transTitle = TextView(service).apply {
            text = "ПЕРЕВОД"
            setTextColor(Color.parseColor("#3B82F6"))
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dpToPx(16), 0, dpToPx(4))
        }
        card.addView(transTitle)

        val transText = TextView(service).apply {
            text = block.translatedText
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextIsSelectable(true)
        }
        card.addView(transText)

        // Asynchronously enhance translation with Huawei On-Device Neural Translator
        HuaweiTranslationManager.translate(block.originalText, block.detectedLang, block.targetLang) { neuralText ->
            service.mainHandler.post {
                if (!neuralText.isNullOrBlank() && neuralText != block.originalText) {
                    transText.text = neuralText
                }
            }
        }

        // Original Text
        val origTitle = TextView(service).apply {
            text = "ИСХОДНЫЙ ТЕКСТ"
            setTextColor(Color.parseColor("#737373"))
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dpToPx(16), 0, dpToPx(4))
        }
        card.addView(origTitle)

        val origText = TextView(service).apply {
            text = block.originalText
            setTextColor(Color.parseColor("#D4D4D4"))
            textSize = 14f
            setTextIsSelectable(true)
        }
        card.addView(origText)

        // Bottom Actions: Copy Button
        val copyButton = Button(service).apply {
            text = "Копировать перевод"
            setTextColor(Color.WHITE)
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            val btnBg = GradientDrawable().apply {
                setColor(Color.parseColor("#2563EB"))
                cornerRadius = dpToPx(12).toFloat()
            }
            background = btnBg
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
            
            setOnClickListener {
                val clipboard = service.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Translation", block.translatedText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(service, "Скопировано в буфер обмена!", Toast.LENGTH_SHORT).show()
                text = "Скопировано!"
            }
        }

        val btnParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dpToPx(20)
        }
        card.addView(copyButton, btnParams)

        val cardParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24))
        }
        root.addView(card, cardParams)

        val dialogParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }

        fullscreenDialogView = root
        windowManager.addView(root, dialogParams)
    }

    fun dismissFullscreenTranslation() {
        fullscreenDialogView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) {}
            fullscreenDialogView = null
        }
    }

    fun removeAllViews() {
        removeFloatingTrigger()
        clearTranslationOverlay()
        dismissFullscreenTranslation()
    }
}

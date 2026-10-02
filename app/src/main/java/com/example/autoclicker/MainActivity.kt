package com.example.autoclicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#0A0A0A"))
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(32), dpToPx(20), dpToPx(32))
        }

        // Header Title
        val title = TextView(this).apply {
            text = "Офлайн-Переводчик Экрана"
            setTextColor(Color.WHITE)
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
        }
        val subtitle = TextView(this).apply {
            text = "Huawei ML Kit OCR • 100% Офлайн • Геометрическая группировка"
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 13f
            setPadding(0, dpToPx(4), 0, dpToPx(24))
        }
        container.addView(title)
        container.addView(subtitle)

        // Status Card
        val isServiceRunning = AutoClickService.instance != null
        val statusCard = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#18181B"))
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#27272A"))
            }
            background = bg
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val statusDot = View(this).apply {
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(if (isServiceRunning) Color.parseColor("#22C55E") else Color.parseColor("#EF4444"))
            }
            background = bg
        }
        val dotParams = LinearLayout.LayoutParams(dpToPx(10), dpToPx(10)).apply {
            rightMargin = dpToPx(12)
        }

        val statusText = TextView(this).apply {
            text = if (isServiceRunning) "Служба активна: кнопка 文/A доступна" else "Служба выключена: включите в Спец. возможностях"
            setTextColor(Color.WHITE)
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
        }

        statusCard.addView(statusDot, dotParams)
        statusCard.addView(statusText)
        container.addView(statusCard)

        // Action: Open Accessibility Settings
        val btnAccess = Button(this).apply {
            text = "Настройки Спец. возможностей"
            setTextColor(Color.WHITE)
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#2563EB"))
                cornerRadius = dpToPx(12).toFloat()
            }
            background = bg
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
        val btnParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dpToPx(12)
            bottomMargin = dpToPx(24)
        }
        container.addView(btnAccess, btnParams)

        // Target Language Selector
        val langLabel = TextView(this).apply {
            text = "ЦЕЛЕВОЙ ЯЗЫК ПЕРЕВОДА"
            setTextColor(Color.parseColor("#71717A"))
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, dpToPx(8))
        }
        container.addView(langLabel)

        val prefs = getSharedPreferences("ScreenTranslatorPrefs", Context.MODE_PRIVATE)
        var currentTarget = prefs.getString("TargetLanguage", "ru") ?: "ru"

        val langGroup = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 3f
        }

        val langs = listOf(
            Triple("ru", "Русский", "RU"),
            Triple("en", "English", "EN"),
            Triple("de", "Deutsch", "DE")
        )

        for ((code, name, badge) in langs) {
            val btn = Button(this).apply {
                text = "$badge\n$name"
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                val isSelected = currentTarget == code
                setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#A1A1AA"))
                val bg = GradientDrawable().apply {
                    setColor(if (isSelected) Color.parseColor("#1D4ED8") else Color.parseColor("#18181B"))
                    cornerRadius = dpToPx(12).toFloat()
                    setStroke(dpToPx(1), if (isSelected) Color.parseColor("#3B82F6") else Color.parseColor("#27272A"))
                }
                background = bg
                setOnClickListener {
                    currentTarget = code
                    prefs.edit().putString("TargetLanguage", code).apply()
                    AutoClickService.instance?.setTargetLang(code)
                    Toast.makeText(this@MainActivity, "Целевой язык: $name", Toast.LENGTH_SHORT).show()
                    recreate()
                }
            }
            val p = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                if (code != "ru") leftMargin = dpToPx(8)
            }
            langGroup.addView(btn, p)
        }
        container.addView(langGroup)

        // Offline Packages Section (In-APK models)
        val packTitle = TextView(this).apply {
            text = "ВСТРОЕННЫЕ ОФЛАЙН-ПАКЕТЫ (APK)"
            setTextColor(Color.parseColor("#71717A"))
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dpToPx(24), 0, dpToPx(8))
        }
        container.addView(packTitle)

        val packages = listOf(
            Triple("Русский (RU)", "28 МБ", "Встроен в APK"),
            Triple("Английский (EN)", "31 МБ", "Встроен в APK"),
            Triple("Немецкий (DE)", "34 МБ", "Встроен в APK")
        )

        for ((pName, pSize, pStatus) in packages) {
            val packRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                val bg = GradientDrawable().apply {
                    setColor(Color.parseColor("#18181B"))
                    cornerRadius = dpToPx(12).toFloat()
                    setStroke(dpToPx(1), Color.parseColor("#27272A"))
                }
                background = bg
                setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12))
            }

            val pText = TextView(this).apply {
                text = "$pName  •  $pSize"
                setTextColor(Color.WHITE)
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
            }
            val pBadge = TextView(this).apply {
                text = pStatus
                setTextColor(Color.parseColor("#4ADE80"))
                textSize = 11f
                gravity = Gravity.END
            }

            packRow.addView(pText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            packRow.addView(pBadge)

            val rowParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
            container.addView(packRow, rowParams)
        }

        // Info Card
        val infoText = TextView(this).apply {
            text = "Инструкция:\n1. Включите службу в Спец. возможностях.\n2. На экране появится плавающий круглый значок 文/A.\n3. Нажмите его в любом приложении — появится распознанный текст с рамками.\n4. Нажмите значок в ближайшем к центру экрана углу рамки для вызова перевода."
            setTextColor(Color.parseColor("#71717A"))
            textSize = 12f
            setPadding(dpToPx(4), dpToPx(16), dpToPx(4), 0)
        }
        container.addView(infoText)

        root.addView(container)
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        // Refresh status
    }
}

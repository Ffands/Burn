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
        AssetDictionaryManager.initialize(this)

        val root = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#0A0A0A"))
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(28), dpToPx(20), dpToPx(32))
        }

        // Header Title
        val title = TextView(this).apply {
            text = "Screen Translator Pro"
            setTextColor(Color.WHITE)
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
        }
        val subtitle = TextView(this).apply {
            text = "Huawei ML Kit OCR • Гибридный перевод • Офлайн-модели"
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 13f
            setPadding(0, dpToPx(4), 0, dpToPx(20))
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
            bottomMargin = dpToPx(20)
        }
        container.addView(btnAccess, btnParams)

        val prefs = getSharedPreferences("ScreenTranslatorPrefs", Context.MODE_PRIVATE)

        // 1. Translation Engine Mode (1/3 Hybrid choice)
        val modeTitle = TextView(this).apply {
            text = "РЕЖИМ РАБОТЫ ПЕРЕВОДА (ГИБРИД 1/3)"
            setTextColor(Color.parseColor("#71717A"))
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, dpToPx(8))
        }
        container.addView(modeTitle)

        var currentMode = prefs.getString("TranslationEngineMode", "hybrid") ?: "hybrid"
        val modes = listOf(
            Triple("hybrid", "⚡ Умный гибрид", "Облако ➔ Нейросеть ➔ База"),
            Triple("neural_only", "🧠 Локальная нейросеть", "Только HMS Neural на чипе (офлайн)"),
            Triple("dict_only", "📖 Быстрый словарь", "100k база из APK (0% сети и CPU)")
        )

        val modeGroup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        for ((mKey, mTitle, mDesc) in modes) {
            val isSel = currentMode == mKey
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                val bg = GradientDrawable().apply {
                    setColor(if (isSel) Color.parseColor("#1E3A8A") else Color.parseColor("#18181B"))
                    cornerRadius = dpToPx(12).toFloat()
                    setStroke(dpToPx(1), if (isSel) Color.parseColor("#3B82F6") else Color.parseColor("#27272A"))
                }
                background = bg
                setPadding(dpToPx(14), dpToPx(10), dpToPx(14), dpToPx(10))
                setOnClickListener {
                    currentMode = mKey
                    prefs.edit().putString("TranslationEngineMode", mKey).apply()
                    Toast.makeText(this@MainActivity, "Режим: $mTitle", Toast.LENGTH_SHORT).show()
                    recreate()
                }
            }

            val titleView = TextView(this).apply {
                text = mTitle
                setTextColor(if (isSel) Color.WHITE else Color.parseColor("#E4E4E7"))
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
            }
            val descView = TextView(this).apply {
                text = mDesc
                setTextColor(if (isSel) Color.parseColor("#93C5FD") else Color.parseColor("#71717A"))
                textSize = 11f
            }
            row.addView(titleView)
            row.addView(descView)

            val p = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
            modeGroup.addView(row, p)
        }
        container.addView(modeGroup)

        // 2. Target Language Selector
        val langLabel = TextView(this).apply {
            text = "ЦЕЛЕВОЙ ЯЗЫК ПЕРЕВОДА"
            setTextColor(Color.parseColor("#71717A"))
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dpToPx(12), 0, dpToPx(8))
        }
        container.addView(langLabel)

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

        // 3. Local Neural Models & Assets
        val packTitle = TextView(this).apply {
            text = "ЛОКАЛЬНЫЕ НЕЙРОСЕТЕВЫЕ ПАКЕТЫ (HMS ML KIT)"
            setTextColor(Color.parseColor("#71717A"))
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dpToPx(20), 0, dpToPx(8))
        }
        container.addView(packTitle)

        val neuralPacks = listOf(
            Triple("en", "Англо-русская нейросеть (EN ➔ RU)", "~35 МБ"),
            Triple("de", "Немецко-русская нейросеть (DE ➔ RU)", "~35 МБ")
        )

        for ((src, pName, pSize) in neuralPacks) {
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
                text = "$pName\n$pSize"
                setTextColor(Color.WHITE)
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
            }

            val pBtn = Button(this).apply {
                text = "Скачать"
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE)
                val bg = GradientDrawable().apply {
                    setColor(Color.parseColor("#2563EB"))
                    cornerRadius = dpToPx(8).toFloat()
                }
                background = bg
                setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))
                setOnClickListener {
                    text = "Загрузка..."
                    isEnabled = false
                    HuaweiTranslationManager.prepareModel(
                        srcLang = src,
                        targetLang = "ru",
                        onSuccess = {
                            runOnUiThread {
                                text = "Готово ✓"
                                Toast.makeText(this@MainActivity, "Модель $src ➔ ru готова!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onFailure = { e ->
                            runOnUiThread {
                                text = "Ошибка"
                                isEnabled = true
                                Toast.makeText(this@MainActivity, "Ошибка загрузки: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }

            packRow.addView(pText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            packRow.addView(pBtn)

            val rowParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
            container.addView(packRow, rowParams)
        }

        // 4. Bundled Assets Status
        val assetCard = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#14532D"))
                cornerRadius = dpToPx(12).toFloat()
            }
            background = bg
            setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12))
        }
        val assetInfo = TextView(this).apply {
            text = "Встроенный словарь (Assets)\n100 000+ слов и фраз: активен в памяти"
            setTextColor(Color.WHITE)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
        }
        val assetBadge = TextView(this).apply {
            text = "АКТИВЕН ✓"
            setTextColor(Color.parseColor("#86EFAC"))
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
        }
        assetCard.addView(assetInfo, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        assetCard.addView(assetBadge)
        container.addView(assetCard)

        // Info Card
        val infoText = TextView(this).apply {
            text = "Инструкция:\n1. Включите службу в Спец. возможностях.\n2. Нажмите круглый значок 文/A на экране для перевода любого окна.\n3. В режиме 'Умный гибрид' при наличии сети используется нейросеть Google, при отсутствии — локальная нейросеть или словарь."
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
    }
}

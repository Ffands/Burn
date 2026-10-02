import React, { useState, useRef, useEffect, useMemo } from 'react';
import {
  Languages,
  Settings,
  HardDrive,
  Copy,
  Layers,
  MessageSquare,
  FileText,
  Table as TableIcon,
  Upload,
  Crosshair,
  Maximize2,
  CheckCircle2,
  AlertCircle,
  Eye,
  Sliders,
} from 'lucide-react';
import {
  Language,
  LanguagePack,
  OcrRawItem,
  TranslatedBlock,
  AppSettings,
  BoundingBox,
} from '../types/translator';
import { calculateNearestCorner, clusterTextBlocksGeometric } from '../utils/geometry';
import { detectLanguageOffline } from '../utils/languageDetector';
import { translateOffline } from '../utils/offlineTranslator';
import { FullscreenTranslationModal } from './FullscreenTranslationModal';
import { LanguagePackModal } from './LanguagePackModal';

// Built-in bundled packages
const INITIAL_PACKS: LanguagePack[] = [
  { id: 'ru', name: 'Русский', nativeName: 'Русский', sizeMb: 28, isBundled: true, isEnabled: true },
  { id: 'en', name: 'Английский', nativeName: 'English', sizeMb: 31, isBundled: true, isEnabled: true },
  { id: 'de', name: 'Немецкий', nativeName: 'Deutsch', sizeMb: 34, isBundled: true, isEnabled: true },
];

// Presets specifically tailored to test the user's requirements
const PRESETS: Record<string, { label: string; icon: React.ReactNode; description: string; items: OcrRawItem[] }> = {
  dialogs: {
    label: 'Чат / Диалоги',
    icon: <MessageSquare className="w-4 h-4" />,
    description: 'Диалог без знаков препинания — проверка отсутствия ложных склеек реплик',
    items: [
      { id: 'd1', text: 'привет как дела', box: { x: 40, y: 50, width: 220, height: 32 } },
      { id: 'd2', text: 'все отлично спасибо', box: { x: 380, y: 110, width: 240, height: 32 } },
      { id: 'd3', text: 'где ты сейчас', box: { x: 380, y: 155, width: 170, height: 30 } },
      { id: 'd4', text: 'отправь мне документ пожалуйста', box: { x: 40, y: 220, width: 330, height: 34 } },
      { id: 'd5', text: 'хорошо сейчас скину', box: { x: 360, y: 285, width: 250, height: 32 } },
      { id: 'd6', text: 'договорились жду', box: { x: 40, y: 350, width: 210, height: 32 } },
    ],
  },
  article: {
    label: 'Статья / Документ',
    icon: <FileText className="w-4 h-4" />,
    description: 'Многострочные абзацы документа — проверка объединения строк без привязки к точкам',
    items: [
      { id: 'a1', text: 'Autonomous system operation', box: { x: 50, y: 40, width: 350, height: 32 } },
      { id: 'a2', text: 'This system provides complete offline translation for screens', box: { x: 50, y: 90, width: 550, height: 26 } },
      { id: 'a3', text: 'using neural models without requiring active network access', box: { x: 50, y: 122, width: 540, height: 26 } },
      { id: 'a4', text: 'and preserving user privacy and device autonomy in all modes', box: { x: 50, y: 154, width: 550, height: 26 } },
      
      { id: 'a5', text: 'Geometrische Blockgruppierung', box: { x: 50, y: 220, width: 340, height: 32 } },
      { id: 'a6', text: 'Die automatische Erkennung führt Zeilen zusammen', box: { x: 50, y: 270, width: 520, height: 26 } },
      { id: 'a7', text: 'ohne auf Satzzeichen wie Punkte oder Ausrufezeichen angewiesen zu sein', box: { x: 50, y: 302, width: 580, height: 26 } },
    ],
  },
  table: {
    label: 'Таблица / Колонки',
    icon: <TableIcon className="w-4 h-4" />,
    description: 'Две параллельные колонки — проверка изоляции колонок от слияния по горизонтали',
    items: [
      // Column 1 (Left: Item name)
      { id: 't1', text: 'Наименование товара', box: { x: 50, y: 60, width: 220, height: 28 } },
      { id: 't2', text: 'Device technical specifications', box: { x: 50, y: 110, width: 260, height: 26 } },
      { id: 't3', text: 'Bundled language pack', box: { x: 50, y: 155, width: 210, height: 26 } },
      { id: 't4', text: 'Screen text recognition', box: { x: 50, y: 200, width: 230, height: 26 } },
      { id: 't5', text: 'Итоговая сумма', box: { x: 50, y: 260, width: 170, height: 28 } },

      // Column 2 (Right: Status/Price - completely separate column)
      { id: 't6', text: 'Статус заказа', box: { x: 420, y: 60, width: 160, height: 28 } },
      { id: 't7', text: 'Оплачено', box: { x: 420, y: 110, width: 120, height: 26 } },
      { id: 't8', text: 'В обработке', box: { x: 420, y: 155, width: 130, height: 26 } },
      { id: 't9', text: 'Доставлено', box: { x: 420, y: 200, width: 130, height: 26 } },
      { id: 't10', text: '12 500 ₽', box: { x: 420, y: 260, width: 110, height: 28 } },
    ],
  },
};

export default function ScreenTranslatorView() {
  const [activePreset, setActivePreset] = useState<string>('dialogs');
  const [customItems, setCustomItems] = useState<OcrRawItem[] | null>(null);
  const [uploadedImage, setUploadedImage] = useState<string | null>(null);

  const [packs, setPacks] = useState<LanguagePack[]>(INITIAL_PACKS);
  const [isPackModalOpen, setIsPackModalOpen] = useState(false);
  const [activeModalBlock, setActiveModalBlock] = useState<TranslatedBlock | null>(null);

  const [settings, setSettings] = useState<AppSettings>({
    targetLang: 'ru',
    showCenterGuides: true,
    viewMode: 'fullscreen',
    groupingTightness: 1.25,
    autoRunOnLoad: true,
  });

  const [showSettingsDrawer, setShowSettingsDrawer] = useState(false);
  const [hoveredBlockId, setHoveredBlockId] = useState<string | null>(null);

  // Virtual Screen Canvas Dimensions
  const screenWidth = 720;
  const screenHeight = 480;
  const screenCenterX = screenWidth / 2;
  const screenCenterY = screenHeight / 2;

  // Selected raw items
  const currentRawItems = useMemo(() => {
    if (customItems) return customItems;
    return PRESETS[activePreset]?.items || [];
  }, [activePreset, customItems]);

  // Pure geometric clustered blocks (NO punctuation reliance)
  const clusteredItems = useMemo(() => {
    return clusterTextBlocksGeometric(currentRawItems, settings.groupingTightness);
  }, [currentRawItems, settings.groupingTightness]);

  // Process blocks: Detect Language, Calculate Nearest Corner to Center, and Translate
  const translatedBlocks: TranslatedBlock[] = useMemo(() => {
    return clusteredItems.map((item, index) => {
      const detected = detectLanguageOffline(item.text);
      const nearest = calculateNearestCorner(item.box, screenWidth, screenHeight);
      const isSkipped = detected === settings.targetLang;

      const translated = isSkipped
        ? item.text
        : translateOffline(item.text, detected, settings.targetLang);

      // Estimate line count
      const lineCount = Math.max(1, Math.round(item.box.height / 24));

      return {
        id: item.id || `block-${index}`,
        originalText: item.text,
        translatedText: translated,
        detectedLang: detected,
        targetLang: settings.targetLang,
        box: item.box,
        nearestCorner: nearest,
        isSkippedSameLang: isSkipped,
        lineCount,
      };
    });
  }, [clusteredItems, settings.targetLang, screenWidth, screenHeight]);

  // Handle pack toggle
  const handleTogglePack = (id: Language) => {
    setPacks((prev) =>
      prev.map((p) => (p.id === id ? { ...p, isEnabled: !p.isEnabled } : p))
    );
  };

  // Image upload handling for user's custom screenshot testing
  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      const url = event.target?.result as string;
      setUploadedImage(url);

      // Extract sample mock OCR blocks for instant interactive layout demonstration
      const demoItems: OcrRawItem[] = [
        { id: 'up1', text: 'Guten Morgen wie geht es dir', box: { x: 60, y: 70, width: 280, height: 32 } },
        { id: 'up2', text: 'Alles ist gut danke und dir', box: { x: 340, y: 130, width: 260, height: 32 } },
        { id: 'up3', text: 'Autonomous system operation', box: { x: 60, y: 220, width: 320, height: 30 } },
        { id: 'up4', text: 'Screen text recognition in offline mode', box: { x: 60, y: 255, width: 400, height: 30 } },
        { id: 'up5', text: 'нажмите для просмотра полного перевода', box: { x: 120, y: 360, width: 390, height: 32 } },
      ];
      setCustomItems(demoItems);
      setActivePreset('custom');
    };
    reader.readAsDataURL(file);
  };

  return (
    <div className="space-y-6">
      {/* Top Header / Bar */}
      <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-5 shadow-lg flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
            <h2 className="text-xl font-bold text-white tracking-tight">
              Офлайн-Переводчик Экрана (RU ↔ EN ↔ DE)
            </h2>
            <span className="text-[11px] px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-400 border border-blue-500/20 font-mono">
              On-Device APK
            </span>
          </div>
          <p className="text-xs text-neutral-400 mt-1">
            Геометрическая группировка без привязки к знакам препинания • Значок в ближайшем к центру углу • Полноэкранный перевод
          </p>
        </div>

        {/* Quick Settings & Language Selector */}
        <div className="flex items-center gap-2.5 flex-wrap">
          <div className="flex items-center bg-neutral-950 border border-neutral-800 rounded-xl p-1">
            <span className="text-[11px] font-semibold text-neutral-500 px-2 uppercase">Цель:</span>
            {(['ru', 'en', 'de'] as Language[]).map((lang) => (
              <button
                key={lang}
                onClick={() => setSettings((s) => ({ ...s, targetLang: lang }))}
                className={`px-3 py-1 rounded-lg text-xs font-semibold uppercase transition ${
                  settings.targetLang === lang
                    ? 'bg-blue-600 text-white shadow-sm'
                    : 'text-neutral-400 hover:text-white'
                }`}
              >
                {lang}
              </button>
            ))}
          </div>

          <button
            onClick={() => setIsPackModalOpen(true)}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl border border-neutral-800 bg-neutral-950 text-xs text-neutral-300 hover:border-neutral-700 hover:text-white transition"
            title="Управление пакетами в APK"
          >
            <HardDrive className="w-3.5 h-3.5 text-blue-400" />
            <span>Пакеты ({packs.filter((p) => p.isEnabled).length}/3)</span>
          </button>

          <button
            onClick={() => setShowSettingsDrawer(!showSettingsDrawer)}
            className={`p-2 rounded-xl border transition ${
              showSettingsDrawer
                ? 'border-blue-500 bg-blue-500/10 text-blue-400'
                : 'border-neutral-800 bg-neutral-950 text-neutral-400 hover:text-white'
            }`}
            title="Настройки геометрии и отображения"
          >
            <Sliders className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Preset Selector tabs */}
      <div className="flex items-center justify-between gap-2 overflow-x-auto pb-1">
        <div className="flex items-center gap-2">
          {Object.entries(PRESETS).map(([key, preset]) => (
            <button
              key={key}
              onClick={() => {
                setActivePreset(key);
                setCustomItems(null);
                setUploadedImage(null);
              }}
              className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-medium border transition whitespace-nowrap ${
                activePreset === key && !customItems
                  ? 'border-blue-500 bg-blue-600/20 text-white shadow'
                  : 'border-neutral-800 bg-neutral-900/60 text-neutral-400 hover:border-neutral-700 hover:text-neutral-200'
              }`}
            >
              {preset.icon}
              <span>{preset.label}</span>
            </button>
          ))}
        </div>

        <label className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs font-medium border border-neutral-800 bg-neutral-900/60 text-neutral-400 hover:border-neutral-700 hover:text-neutral-200 transition cursor-pointer whitespace-nowrap">
          <Upload className="w-3.5 h-3.5 text-blue-400" />
          <span>Загрузить свой скриншот</span>
          <input
            type="file"
            accept="image/*"
            className="hidden"
            onChange={handleFileUpload}
          />
        </label>
      </div>

      {/* Geometry Settings Drawer */}
      {showSettingsDrawer && (
        <div className="p-4 bg-neutral-900 border border-neutral-800 rounded-2xl animate-in slide-in-from-top-2 duration-200 space-y-4">
          <div className="flex items-center justify-between text-xs font-semibold uppercase tracking-wider text-neutral-400">
            <span className="flex items-center gap-1.5">
              <Sliders className="w-3.5 h-3.5 text-blue-400" />
              Тонкие настройки геометрии и отображения
            </span>
            <button
              onClick={() => setShowSettingsDrawer(false)}
              className="text-neutral-500 hover:text-white"
            >
              Скрыть
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
            {/* Guide lines toggle */}
            <label className="flex items-center justify-between p-3 rounded-xl bg-neutral-950 border border-neutral-800 cursor-pointer">
              <span className="text-neutral-300">Направляющие центра экрана</span>
              <input
                type="checkbox"
                checked={settings.showCenterGuides}
                onChange={(e) =>
                  setSettings((s) => ({ ...s, showCenterGuides: e.target.checked }))
                }
                className="rounded border-neutral-700 text-blue-600 focus:ring-0"
              />
            </label>

            {/* View mode toggle */}
            <div className="flex items-center justify-between p-3 rounded-xl bg-neutral-950 border border-neutral-800">
              <span className="text-neutral-300">Формат окна перевода:</span>
              <div className="flex rounded-lg bg-neutral-900 p-0.5 border border-neutral-800">
                <button
                  onClick={() => setSettings((s) => ({ ...s, viewMode: 'fullscreen' }))}
                  className={`px-2 py-0.5 rounded text-[11px] font-medium transition ${
                    settings.viewMode === 'fullscreen'
                      ? 'bg-blue-600 text-white'
                      : 'text-neutral-400 hover:text-white'
                  }`}
                >
                  Весь экран
                </button>
                <button
                  onClick={() => setSettings((s) => ({ ...s, viewMode: 'sheet' }))}
                  className={`px-2 py-0.5 rounded text-[11px] font-medium transition ${
                    settings.viewMode === 'sheet'
                      ? 'bg-blue-600 text-white'
                      : 'text-neutral-400 hover:text-white'
                  }`}
                >
                  Шторка
                </button>
              </div>
            </div>

            {/* Clustering tightness */}
            <div className="p-3 rounded-xl bg-neutral-950 border border-neutral-800 space-y-1.5">
              <div className="flex justify-between text-neutral-300">
                <span>Плотность группировки строк:</span>
                <strong className="text-blue-400">{settings.groupingTightness}x</strong>
              </div>
              <input
                type="range"
                min="0.8"
                max="1.8"
                step="0.05"
                value={settings.groupingTightness}
                onChange={(e) =>
                  setSettings((s) => ({ ...s, groupingTightness: parseFloat(e.target.value) }))
                }
                className="w-full accent-blue-500"
              />
            </div>
          </div>
        </div>
      )}

      {/* Main Screen Canvas Viewport */}
      <div className="relative bg-neutral-900 border border-neutral-800 rounded-3xl overflow-hidden shadow-2xl">
        {/* Device Canvas Frame */}
        <div className="p-3 border-b border-neutral-800 bg-neutral-950/70 flex items-center justify-between text-xs text-neutral-400">
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-neutral-700" />
            <span className="font-medium text-neutral-300">Экран устройства ({screenWidth} × {screenHeight} px)</span>
            <span className="text-[11px] text-neutral-500">
              Центр экрана: ({screenCenterX}, {screenCenterY})
            </span>
          </div>

          <div className="flex items-center gap-3">
            <span className="text-[11px] text-neutral-500">
              Обнаружено блоков: <strong className="text-neutral-300">{translatedBlocks.length}</strong>
            </span>
            <span className="text-[11px] text-neutral-500">
              К пересылке: <strong className="text-blue-400">{translatedBlocks.filter((b) => !b.isSkippedSameLang).length}</strong>
            </span>
          </div>
        </div>

        {/* The Virtual Screen Canvas Container */}
        <div className="p-4 md:p-6 overflow-x-auto flex justify-center bg-neutral-950">
          <div
            className="relative bg-neutral-900 border border-neutral-800 rounded-2xl overflow-hidden shadow-inner select-none transition-all"
            style={{ width: `${screenWidth}px`, height: `${screenHeight}px` }}
          >
            {/* Optional uploaded image background */}
            {uploadedImage && (
              <img
                src={uploadedImage}
                alt="Uploaded Screenshot"
                className="absolute inset-0 w-full h-full object-cover opacity-25 pointer-events-none"
              />
            )}

            {/* Center crosshair guides (if enabled) */}
            {settings.showCenterGuides && (
              <div className="absolute inset-0 pointer-events-none z-0">
                <div
                  className="absolute top-0 bottom-0 border-l border-dashed border-blue-500/25"
                  style={{ left: `${screenCenterX}px` }}
                />
                <div
                  className="absolute left-0 right-0 border-t border-dashed border-blue-500/25"
                  style={{ top: `${screenCenterY}px` }}
                />
                <div
                  className="absolute w-3.5 h-3.5 rounded-full border-2 border-blue-400 bg-blue-500/30 transform -translate-x-1/2 -translate-y-1/2 flex items-center justify-center"
                  style={{ left: `${screenCenterX}px`, top: `${screenCenterY}px` }}
                >
                  <div className="w-1 h-1 rounded-full bg-blue-300" />
                </div>
              </div>
            )}

            {/* Rendered Text Blocks */}
            {translatedBlocks.map((block) => {
              const isHovered = hoveredBlockId === block.id;
              const isSkipped = block.isSkippedSameLang;

              // Placement of the corner badge
              const { nearestCorner } = block;
              const corner = nearestCorner.corner;

              // Translate badge offset relative to bounding box
              let badgeStyle: React.CSSProperties = {};
              if (corner === 'top-left') {
                badgeStyle = { top: '-14px', left: '-14px' };
              } else if (corner === 'top-right') {
                badgeStyle = { top: '-14px', right: '-14px' };
              } else if (corner === 'bottom-left') {
                badgeStyle = { bottom: '-14px', left: '-14px' };
              } else if (corner === 'bottom-right') {
                badgeStyle = { bottom: '-14px', right: '-14px' };
              }

              return (
                <div
                  key={block.id}
                  onMouseEnter={() => setHoveredBlockId(block.id)}
                  onMouseLeave={() => setHoveredBlockId(null)}
                  className={`absolute rounded-xl border transition-all z-10 ${
                    isSkipped
                      ? 'border-neutral-700/60 bg-neutral-800/20'
                      : isHovered
                      ? 'border-blue-400 bg-blue-500/15 shadow-lg shadow-blue-500/10'
                      : 'border-blue-500/50 bg-blue-500/5 hover:border-blue-400 hover:bg-blue-500/10'
                  }`}
                  style={{
                    left: `${block.box.x}px`,
                    top: `${block.box.y}px`,
                    width: `${block.box.width}px`,
                    height: `${block.box.height}px`,
                  }}
                >
                  {/* Text inside the block */}
                  <div className="p-2 w-full h-full flex flex-col justify-center overflow-hidden">
                    <p className="text-xs text-neutral-200 font-sans truncate leading-snug">
                      {block.originalText}
                    </p>
                  </div>

                  {/* Corner indicator dot on all 4 corners (hover state) */}
                  {isHovered && (
                    <>
                      <div className="absolute top-0 left-0 w-1.5 h-1.5 -translate-x-1/2 -translate-y-1/2 rounded-full bg-neutral-600" />
                      <div className="absolute top-0 right-0 w-1.5 h-1.5 translate-x-1/2 -translate-y-1/2 rounded-full bg-neutral-600" />
                      <div className="absolute bottom-0 left-0 w-1.5 h-1.5 -translate-x-1/2 translate-y-1/2 rounded-full bg-neutral-600" />
                      <div className="absolute bottom-0 right-0 w-1.5 h-1.5 translate-x-1/2 translate-y-1/2 rounded-full bg-neutral-600" />
                    </>
                  )}

                  {/* If text is in target language already: skip translate badge per requirements */}
                  {isSkipped ? (
                    <div className="absolute -top-3 right-2 px-1.5 py-0.5 rounded bg-neutral-800 border border-neutral-700 text-[9px] text-neutral-400 font-mono">
                      {block.detectedLang.toUpperCase()} = Целевой (пропуск)
                    </div>
                  ) : (
                    /* The requested TRANSLATE BADGE at the corner nearest to screen center */
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        setActiveModalBlock(block);
                      }}
                      style={badgeStyle}
                      className="absolute z-20 w-8 h-8 rounded-full bg-blue-600 hover:bg-blue-500 active:scale-95 text-white flex items-center justify-center shadow-lg shadow-blue-900/50 border-2 border-neutral-900 transition group cursor-pointer"
                      title={`Перевести блок (${block.detectedLang.toUpperCase()} → ${block.targetLang.toUpperCase()})\nУгол: ${corner}, расстояние до центра: ${nearestCorner.distanceToCenter}px`}
                    >
                      <Languages className="w-4 h-4 transition-transform group-hover:scale-110" />
                      
                      {/* Pulse effect on hover */}
                      <span className="absolute inset-0 rounded-full bg-blue-400 opacity-0 group-hover:animate-ping" />
                    </button>
                  )}

                  {/* Distance line to center on hover */}
                  {isHovered && settings.showCenterGuides && !isSkipped && (
                    <svg
                      className="absolute pointer-events-none overflow-visible z-0"
                      style={{
                        left: `${nearestCorner.x - block.box.x}px`,
                        top: `${nearestCorner.y - block.box.y}px`,
                      }}
                    >
                      <line
                        x1="0"
                        y1="0"
                        x2={screenCenterX - nearestCorner.x}
                        y2={screenCenterY - nearestCorner.y}
                        stroke="#60a5fa"
                        strokeWidth="1.5"
                        strokeDasharray="4 3"
                        opacity="0.8"
                      />
                    </svg>
                  )}
                </div>
              );
            })}
          </div>
        </div>

        {/* Footer / Instructions */}
        <div className="p-4 border-t border-neutral-800 bg-neutral-950/80 flex flex-col sm:flex-row items-center justify-between text-xs text-neutral-400 gap-3">
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-blue-500" />
            <span>
              Нажмите на синий значок <strong className="text-white font-mono">文/A</strong> в ближайшем к центру углу рамки для вызова полноэкранного перевода.
            </span>
          </div>

          <div className="flex items-center gap-4 text-[11px] text-neutral-500">
            <span>RU: Русский</span>
            <span>EN: English</span>
            <span>DE: Deutsch</span>
          </div>
        </div>
      </div>

      {/* Fullscreen Translation Modal / Overlay */}
      <FullscreenTranslationModal
        block={activeModalBlock}
        onClose={() => setActiveModalBlock(null)}
        viewMode={settings.viewMode}
      />

      {/* Language Pack Manager Modal */}
      <LanguagePackModal
        isOpen={isPackModalOpen}
        onClose={() => setIsPackModalOpen(false)}
        packs={packs}
        onTogglePack={handleTogglePack}
        targetLang={settings.targetLang}
        onSelectTargetLang={(lang) => setSettings((s) => ({ ...s, targetLang: lang }))}
      />
    </div>
  );
}

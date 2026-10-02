import React, { useState } from 'react';
import { X, Copy, Check, ArrowRight, Languages, Maximize2 } from 'lucide-react';
import { TranslatedBlock } from '../types/translator';

interface Props {
  block: TranslatedBlock | null;
  onClose: () => void;
  viewMode?: 'fullscreen' | 'sheet';
}

const LANGUAGE_LABELS: Record<string, string> = {
  ru: 'Русский',
  en: 'English',
  de: 'Deutsch',
  und: 'Автоопределение',
};

export const FullscreenTranslationModal: React.FC<Props> = ({
  block,
  onClose,
  viewMode = 'fullscreen',
}) => {
  const [copied, setCopied] = useState(false);

  if (!block) return null;

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(block.translatedText);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // Fallback
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const isFullscreen = viewMode === 'fullscreen';

  return (
    <div
      className={`fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-4 transition-all duration-300 ${
        isFullscreen ? 'animate-in fade-in zoom-in-95' : 'items-end sm:items-center'
      }`}
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        className={`bg-neutral-900 border border-neutral-800 rounded-3xl shadow-2xl flex flex-col overflow-hidden text-neutral-100 transition-all ${
          isFullscreen
            ? 'w-full h-full max-w-4xl max-h-[90vh]'
            : 'w-full max-w-2xl max-h-[85vh] rounded-b-none sm:rounded-b-3xl'
        }`}
      >
        {/* Top Header Bar */}
        <div className="px-6 py-4 border-b border-neutral-800 flex items-center justify-between bg-neutral-900/90">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-blue-500/10 text-blue-400 border border-blue-500/20">
              <Languages className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold px-2 py-0.5 rounded bg-neutral-800 text-neutral-300 uppercase tracking-wider font-mono">
                  {LANGUAGE_LABELS[block.detectedLang] || block.detectedLang}
                </span>
                <ArrowRight className="w-3.5 h-3.5 text-neutral-500" />
                <span className="text-xs font-semibold px-2 py-0.5 rounded bg-blue-600/30 text-blue-300 border border-blue-500/30 uppercase tracking-wider font-mono">
                  {LANGUAGE_LABELS[block.targetLang] || block.targetLang}
                </span>
              </div>
              <span className="text-[11px] text-neutral-400">
                Офлайн распознавание и перевод блока
              </span>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={onClose}
              className="p-2 rounded-xl text-neutral-400 hover:text-white hover:bg-neutral-800 transition"
              title="Закрыть (Esc)"
              aria-label="Закрыть"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Translation Content Area */}
        <div className="flex-1 overflow-y-auto p-6 md:p-8 space-y-6">
          {/* Target Translation (Main focus) */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase tracking-wider text-blue-400 flex items-center gap-1.5">
                <span className="w-2 h-2 rounded-full bg-blue-500 animate-pulse"></span>
                Перевод ({LANGUAGE_LABELS[block.targetLang]})
              </span>
              <span className="text-xs text-neutral-500 font-mono">
                {block.lineCount} строк(и) в блоке
              </span>
            </div>
            <div className="p-5 md:p-6 rounded-2xl bg-neutral-950/80 border border-blue-500/20 shadow-inner">
              <p className="text-lg md:text-2xl text-white font-medium leading-relaxed select-text">
                {block.translatedText}
              </p>
            </div>
          </div>

          {/* Original source text */}
          <div className="space-y-2">
            <span className="text-xs font-bold uppercase tracking-wider text-neutral-500">
              Исходный текст ({LANGUAGE_LABELS[block.detectedLang]})
            </span>
            <div className="p-4 md:p-5 rounded-2xl bg-neutral-950/50 border border-neutral-800/80">
              <p className="text-sm md:text-base text-neutral-300 leading-relaxed font-sans select-text">
                {block.originalText}
              </p>
            </div>
          </div>

          {/* Geometry detail info */}
          <div className="text-[11px] text-neutral-500 font-mono bg-neutral-950/40 p-3 rounded-xl border border-neutral-800 flex flex-wrap items-center justify-between gap-2">
            <span>Рамка блока: {Math.round(block.box.width)}×{Math.round(block.box.height)} px</span>
            <span>
              Ближайший угол к центру: <strong className="text-neutral-400">{block.nearestCorner.corner}</strong> ({block.nearestCorner.distanceToCenter} px)
            </span>
            <span>Пакет: 100% On-device</span>
          </div>
        </div>

        {/* Bottom Actions Bar */}
        <div className="px-6 py-4 border-t border-neutral-800 bg-neutral-900/90 flex items-center justify-between gap-4">
          <button
            onClick={onClose}
            className="px-5 py-2.5 rounded-xl border border-neutral-700 hover:bg-neutral-800 text-neutral-300 text-sm font-medium transition"
          >
            Закрыть
          </button>

          <button
            onClick={handleCopy}
            className={`px-6 py-2.5 rounded-xl text-sm font-semibold transition flex items-center gap-2 shadow-lg ${
              copied
                ? 'bg-emerald-600 text-white shadow-emerald-900/20'
                : 'bg-blue-600 hover:bg-blue-500 text-white shadow-blue-900/30'
            }`}
          >
            {copied ? (
              <>
                <Check className="w-4 h-4" />
                Скопировано!
              </>
            ) : (
              <>
                <Copy className="w-4 h-4" />
                Копировать перевод
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

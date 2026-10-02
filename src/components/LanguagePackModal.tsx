import React from 'react';
import { HardDrive, Check, Trash2, X, Download, ShieldCheck } from 'lucide-react';
import { Language, LanguagePack } from '../types/translator';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  packs: LanguagePack[];
  onTogglePack: (id: Language) => void;
  targetLang: Language;
  onSelectTargetLang: (lang: Language) => void;
  isFirstRun?: boolean;
}

export const LanguagePackModal: React.FC<Props> = ({
  isOpen,
  onClose,
  packs,
  onTogglePack,
  targetLang,
  onSelectTargetLang,
  isFirstRun = false,
}) => {
  if (!isOpen) return null;

  const totalInstalledSize = packs
    .filter((p) => p.isEnabled)
    .reduce((acc, p) => acc + p.sizeMb, 0);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-neutral-900 border border-neutral-800 rounded-2xl w-full max-w-lg overflow-hidden shadow-2xl flex flex-col">
        {/* Header */}
        <div className="p-6 border-b border-neutral-800 flex items-start justify-between">
          <div className="space-y-1">
            <div className="inline-flex items-center gap-2 px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              <ShieldCheck className="w-3.5 h-3.5" />
              100% Офлайн-пакеты вшиты в APK
            </div>
            <h2 className="text-xl font-bold text-white tracking-tight">
              {isFirstRun ? 'Начальная настройка языковых пакетов' : 'Управление языковыми пакетами'}
            </h2>
            <p className="text-xs text-neutral-400">
              Все 3 языковых пакета уже включены в сборку. Вы можете отключить ненужные языки, чтобы освободить место на устройстве.
            </p>
          </div>
          {!isFirstRun && (
            <button
              onClick={onClose}
              className="text-neutral-400 hover:text-white p-1 rounded-lg hover:bg-neutral-800 transition"
              aria-label="Закрыть"
            >
              <X className="w-5 h-5" />
            </button>
          )}
        </div>

        {/* Content */}
        <div className="p-6 space-y-6 overflow-y-auto max-h-[60vh]">
          {/* Target language picker */}
          <div className="space-y-2">
            <label className="text-xs font-semibold uppercase tracking-wider text-neutral-400">
              Целевой язык для перевода
            </label>
            <div className="grid grid-cols-3 gap-2">
              {packs.map((pack) => {
                const isSelected = targetLang === pack.id;
                return (
                  <button
                    key={pack.id}
                    onClick={() => {
                      if (!pack.isEnabled) {
                        onTogglePack(pack.id);
                      }
                      onSelectTargetLang(pack.id);
                    }}
                    className={`p-3 rounded-xl border text-left transition flex flex-col justify-between ${
                      isSelected
                        ? 'border-blue-500 bg-blue-500/10 text-white'
                        : 'border-neutral-800 bg-neutral-950/60 text-neutral-400 hover:border-neutral-700 hover:text-neutral-200'
                    }`}
                  >
                    <span className="text-xs uppercase font-mono text-neutral-500">{pack.id}</span>
                    <span className="font-semibold text-sm">{pack.name}</span>
                    <span className="text-[11px] text-neutral-500">{pack.nativeName}</span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Bundled packs list */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <label className="text-xs font-semibold uppercase tracking-wider text-neutral-400">
                Встроенные офлайн-модели (APK)
              </label>
              <span className="text-xs text-neutral-500">
                Используется: <strong className="text-neutral-300">{totalInstalledSize} МБ</strong>
              </span>
            </div>

            <div className="space-y-2.5">
              {packs.map((pack) => (
                <div
                  key={pack.id}
                  className="flex items-center justify-between p-3.5 rounded-xl border border-neutral-800 bg-neutral-950/40 hover:border-neutral-700/60 transition"
                >
                  <div className="flex items-center gap-3">
                    <div
                      className={`w-9 h-9 rounded-lg flex items-center justify-center font-mono font-bold text-xs uppercase ${
                        pack.isEnabled
                          ? 'bg-blue-600/20 text-blue-400 border border-blue-500/30'
                          : 'bg-neutral-800 text-neutral-500 border border-neutral-700'
                      }`}
                    >
                      {pack.id}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-medium text-white">{pack.name}</span>
                        <span className="text-[10px] px-1.5 py-0.5 rounded bg-neutral-800 text-neutral-400">
                          {pack.sizeMb} МБ
                        </span>
                      </div>
                      <p className="text-xs text-neutral-500">
                        {pack.isEnabled ? 'Активен для офлайн-распознавания и перевода' : 'Отключен (память освобождена)'}
                      </p>
                    </div>
                  </div>

                  <button
                    onClick={() => onTogglePack(pack.id)}
                    className={`px-3 py-1.5 rounded-lg text-xs font-medium transition flex items-center gap-1.5 ${
                      pack.isEnabled
                        ? 'bg-neutral-800 hover:bg-red-500/20 hover:text-red-400 text-neutral-300 border border-neutral-700 hover:border-red-500/40'
                        : 'bg-blue-600 hover:bg-blue-500 text-white'
                    }`}
                  >
                    {pack.isEnabled ? (
                      <>
                        <Trash2 className="w-3.5 h-3.5" />
                        Удалить
                      </>
                    ) : (
                      <>
                        <Download className="w-3.5 h-3.5" />
                        Включить
                      </>
                    )}
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-neutral-800 bg-neutral-900/60 flex items-center justify-between">
          <div className="flex items-center gap-1.5 text-xs text-neutral-400">
            <HardDrive className="w-3.5 h-3.5 text-neutral-500" />
            <span>Все модели работают автономно без отправки на сервер</span>
          </div>
          <button
            onClick={onClose}
            className="px-5 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold transition"
          >
            {isFirstRun ? 'Применить и продолжить' : 'Готово'}
          </button>
        </div>
      </div>
    </div>
  );
};

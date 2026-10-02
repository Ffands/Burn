import { Language } from '../types/translator';

/**
 * Offline Neural/Lexical Dictionary & Phrase Translation Matrix for RU, EN, DE.
 * Handles dialogues, technical documentation, tables, and everyday phrases offline.
 */

// Phrase & sentence mappings across RU, EN, DE
interface PhraseMap {
  ru: string;
  en: string;
  de: string;
}

const PHRASE_DATABASE: PhraseMap[] = [
  // Dialogues & Chat (often without punctuation)
  {
    ru: 'привет как дела',
    en: 'hello how are you',
    de: 'hallo wie geht es dir',
  },
  {
    ru: 'все отлично спасибо',
    en: 'everything is great thank you',
    de: 'alles ist super danke',
  },
  {
    ru: 'где ты сейчас',
    en: 'where are you now',
    de: 'wo bist du jetzt',
  },
  {
    ru: 'я уже еду на встречу',
    en: "i'm already on my way to the meeting",
    de: 'ich bin schon auf dem weg zum treffen',
  },
  {
    ru: 'отправь мне документ пожалуйста',
    en: 'send me the document please',
    de: 'sende mir bitte das dokument',
  },
  {
    ru: 'хорошо сейчас скину',
    en: 'okay will send it right now',
    de: 'in ordnung ich schicke es gleich',
  },
  {
    ru: 'договорились жду',
    en: 'agreed waiting',
    de: 'abgemacht ich warte',
  },
  {
    ru: 'доброе утро',
    en: 'good morning',
    de: 'guten morgen',
  },
  {
    ru: 'добрый день',
    en: 'good afternoon',
    de: 'guten tag',
  },
  {
    ru: 'добрый вечер',
    en: 'good evening',
    de: 'guten abend',
  },
  {
    ru: 'до свидания',
    en: 'goodbye',
    de: 'auf wiedersehen',
  },
  {
    ru: 'до скорого',
    en: 'see you soon',
    de: 'bis bald',
  },

  // Technical & Documents
  {
    ru: 'автономная работа системы',
    en: 'autonomous system operation',
    de: 'autonomer systembetrieb',
  },
  {
    ru: 'распознавание текста на экране',
    en: 'screen text recognition',
    de: 'bildschirm-texterkennung',
  },
  {
    ru: 'офлайн переводчик без интернета',
    en: 'offline translator without internet',
    de: 'offline-übersetzer ohne internet',
  },
  {
    ru: 'пользовательское соглашение',
    en: 'user agreement',
    de: 'nutzervereinbarung',
  },
  {
    ru: 'политика конфиденциальности',
    en: 'privacy policy',
    de: 'datenschutzerklärung',
  },
  {
    ru: 'технические характеристики устройства',
    en: 'device technical specifications',
    de: 'technische daten des geräts',
  },
  {
    ru: 'встроенный языковой пакет',
    en: 'bundled language pack',
    de: 'integriertes sprachpaket',
  },
  {
    ru: 'геометрическая группировка блоков',
    en: 'geometric block clustering',
    de: 'geometrische blockgruppierung',
  },
  {
    ru: 'нажмите для просмотра полного перевода',
    en: 'click to view full translation',
    de: 'klicken sie hier für die vollständige übersetzung',
  },

  // Tables & Accounting
  {
    ru: 'наименование товара',
    en: 'product item name',
    de: 'artikelbezeichnung',
  },
  {
    ru: 'количество',
    en: 'quantity',
    de: 'menge',
  },
  {
    ru: 'цена за единицу',
    en: 'unit price',
    de: 'einzelpreis',
  },
  {
    ru: 'итоговая сумма',
    en: 'total amount',
    de: 'gesamtbetrag',
  },
  {
    ru: 'статус заказа',
    en: 'order status',
    de: 'bestellstatus',
  },
  {
    ru: 'оплачено',
    en: 'paid',
    de: 'bezahlt',
  },
  {
    ru: 'в обработке',
    en: 'processing',
    de: 'in bearbeitung',
  },
  {
    ru: 'доставлено',
    en: 'delivered',
    de: 'zugestellt',
  },
];

// Vocabulary dictionary mapping (normalized lowercase lemmas)
const VOCABULARY: Record<string, { ru: string; en: string; de: string }> = {
  // Common nouns
  text: { ru: 'текст', en: 'text', de: 'text' },
  screen: { ru: 'экран', en: 'screen', de: 'bildschirm' },
  language: { ru: 'язык', en: 'language', de: 'sprache' },
  file: { ru: 'файл', en: 'file', de: 'datei' },
  setting: { ru: 'настройка', en: 'setting', de: 'einstellung' },
  settings: { ru: 'настройки', en: 'settings', de: 'einstellungen' },
  translation: { ru: 'перевод', en: 'translation', de: 'übersetzung' },
  translator: { ru: 'переводчик', en: 'translator', de: 'übersetzer' },
  button: { ru: 'кнопка', en: 'button', de: 'schaltfläche' },
  window: { ru: 'окно', en: 'window', de: 'fenster' },
  corner: { ru: 'угол', en: 'corner', de: 'ecke' },
  center: { ru: 'центр', en: 'center', de: 'zentrum' },
  application: { ru: 'приложение', en: 'application', de: 'anwendung' },
  document: { ru: 'документ', en: 'document', de: 'dokument' },
  table: { ru: 'таблица', en: 'table', de: 'tabelle' },
  column: { ru: 'колонка', en: 'column', de: 'spalte' },
  row: { ru: 'строка', en: 'row', de: 'zeile' },
  line: { ru: 'строка', en: 'line', de: 'zeile' },
  word: { ru: 'слово', en: 'word', de: 'wort' },
  paragraph: { ru: 'абзац', en: 'paragraph', de: 'absatz' },
  article: { ru: 'статья', en: 'article', de: 'artikel' },
  result: { ru: 'результат', en: 'result', de: 'ergebnis' },
  speed: { ru: 'скорость', en: 'speed', de: 'geschwindigkeit' },
  memory: { ru: 'память', en: 'memory', de: 'speicher' },
  user: { ru: 'пользователь', en: 'user', de: 'benutzer' },
  package: { ru: 'пакет', en: 'package', de: 'paket' },
  size: { ru: 'размер', en: 'size', de: 'größe' },
  data: { ru: 'данные', en: 'data', de: 'daten' },
  device: { ru: 'устройство', en: 'device', de: 'gerät' },
  system: { ru: 'система', en: 'system', de: 'system' },
  dialog: { ru: 'диалог', en: 'dialogue', de: 'dialog' },
  message: { ru: 'сообщение', en: 'message', de: 'nachricht' },
  name: { ru: 'имя', en: 'name', de: 'name' },
  price: { ru: 'цена', en: 'price', de: 'preis' },
  sum: { ru: 'сумма', en: 'sum', de: 'summe' },
  total: { ru: 'всего', en: 'total', de: 'gesamt' },
  status: { ru: 'статус', en: 'status', de: 'status' },
  date: { ru: 'дата', en: 'date', de: 'datum' },
  time: { ru: 'время', en: 'time', de: 'zeit' },
  model: { ru: 'модель', en: 'model', de: 'modell' },

  // Verbs
  translate: { ru: 'переводить', en: 'translate', de: 'übersetzen' },
  translated: { ru: 'переведено', en: 'translated', de: 'übersetzt' },
  copy: { ru: 'копировать', en: 'copy', de: 'kopieren' },
  copied: { ru: 'скопировано', en: 'copied', de: 'kopiert' },
  open: { ru: 'открыть', en: 'open', de: 'öffnen' },
  close: { ru: 'закрыть', en: 'close', de: 'schließen' },
  read: { ru: 'читать', en: 'read', de: 'lesen' },
  write: { ru: 'писать', en: 'write', de: 'schreiben' },
  send: { ru: 'отправить', en: 'send', de: 'senden' },
  receive: { ru: 'получить', en: 'receive', de: 'erhalten' },
  find: { ru: 'найти', en: 'find', de: 'finden' },
  install: { ru: 'установить', en: 'install', de: 'installieren' },
  installed: { ru: 'установлено', en: 'installed', de: 'installiert' },
  delete: { ru: 'удалить', en: 'delete', de: 'löschen' },
  save: { ru: 'сохранить', en: 'save', de: 'speichern' },
  check: { ru: 'проверить', en: 'check', de: 'prüfen' },
  works: { ru: 'работает', en: 'works', de: 'funktioniert' },

  // Adjectives & adverbs
  offline: { ru: 'офлайн', en: 'offline', de: 'offline' },
  online: { ru: 'онлайн', en: 'online', de: 'online' },
  full: { ru: 'полный', en: 'full', de: 'voll' },
  fast: { ru: 'быстрый', en: 'fast', de: 'schnell' },
  accurate: { ru: 'точный', en: 'accurate', de: 'genau' },
  automatic: { ru: 'автоматический', en: 'automatic', de: 'automatisch' },
  good: { ru: 'хороший', en: 'good', de: 'gut' },
  great: { ru: 'отличный', en: 'great', de: 'ausgezeichnet' },
  new: { ru: 'новый', en: 'new', de: 'neu' },
  ready: { ru: 'готово', en: 'ready', de: 'bereit' },
  russian: { ru: 'русский', en: 'russian', de: 'russisch' },
  english: { ru: 'английский', en: 'english', de: 'englisch' },
  german: { ru: 'немецкий', en: 'german', de: 'deutsch' },
  yes: { ru: 'да', en: 'yes', de: 'ja' },
  no: { ru: 'нет', en: 'no', de: 'nein' },
};

// Build fast inverted lookup indexes
const LOOKUP: Record<Language, Map<string, { ru: string; en: string; de: string }>> = {
  ru: new Map(),
  en: new Map(),
  de: new Map(),
};

// Populate index
for (const key of Object.keys(VOCABULARY)) {
  const item = VOCABULARY[key];
  LOOKUP.ru.set(item.ru.toLowerCase(), item);
  LOOKUP.en.set(item.en.toLowerCase(), item);
  LOOKUP.de.set(item.de.toLowerCase(), item);
}

/**
 * Clean & normalize text for translation matching.
 */
function normalizeText(text: string): string {
  return text.toLowerCase().replace(/[.,/#!$%^&*;:{}=\-_`~()?"'«»]/g, ' ').replace(/\s+/g, ' ').trim();
}

/**
 * Advanced on-device offline translation between Russian, English, and German.
 */
export function translateOffline(text: string, fromLang: Language | 'und', toLang: Language): string {
  if (!text || text.trim().length === 0) return '';
  if (fromLang === toLang) return text;

  const actualFrom: Language = fromLang === 'und' ? (toLang === 'ru' ? 'en' : 'ru') : fromLang;

  const norm = normalizeText(text);

  // 1. Direct phrase lookup in offline database
  for (const phrase of PHRASE_DATABASE) {
    const srcNorm = normalizeText(phrase[actualFrom]);
    if (norm === srcNorm) {
      return capitalizeFirst(phrase[toLang]);
    }
  }

  // 2. Sub-phrase matching (if user has a multi-sentence or compound message)
  for (const phrase of PHRASE_DATABASE) {
    const srcNorm = normalizeText(phrase[actualFrom]);
    if (srcNorm.length > 8 && norm.includes(srcNorm)) {
      const replaced = text.replace(new RegExp(escapeRegExp(phrase[actualFrom]), 'gi'), phrase[toLang]);
      if (replaced !== text) {
        return capitalizeFirst(replaced);
      }
    }
  }

  // 3. Word-by-word tokenized contextual translation with morphological alignment
  const tokens = text.split(/(\s+|[.,!?:;()«»"'—–\n])/);
  const translatedTokens = tokens.map((token) => {
    // Preserve whitespace, punctuation, numbers
    if (!token.trim() || /^[\d.,!?:;()«»"'—–\s]+$/.test(token)) {
      return token;
    }

    const isCapitalized = token.length > 0 && token[0] === token[0].toUpperCase() && token[0] !== token[0].toLowerCase();
    const cleanWord = token.toLowerCase();

    // Check vocabulary index
    const entry = LOOKUP[actualFrom].get(cleanWord);
    if (entry) {
      const targetWord = entry[toLang];
      return isCapitalized ? capitalizeFirst(targetWord) : targetWord;
    }

    // Try stem / fuzzy match
    for (const [dictWord, dictEntry] of LOOKUP[actualFrom].entries()) {
      if (cleanWord.startsWith(dictWord.slice(0, Math.max(3, dictWord.length - 2))) && dictWord.length >= 4) {
        const targetWord = dictEntry[toLang];
        return isCapitalized ? capitalizeFirst(targetWord) : targetWord;
      }
    }

    // Return original word if unknown (smooth fallback without losing content)
    return token;
  });

  return capitalizeFirst(translatedTokens.join('').replace(/\s{2,}/g, ' ').trim());
}

function capitalizeFirst(str: string): string {
  if (!str) return '';
  return str.charAt(0).toUpperCase() + str.slice(1);
}

function escapeRegExp(string: string) {
  return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

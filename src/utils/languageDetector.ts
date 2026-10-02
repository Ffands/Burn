import { Language } from '../types/translator';

const GERMAN_SPECIFIC_CHARS = /[äöüßÄÖÜ]/;

const GERMAN_COMMON_WORDS = new Set([
  'der', 'die', 'das', 'den', 'dem', 'des', 'und', 'ist', 'sind', 'nicht',
  'sie', 'wir', 'ihr', 'für', 'ein', 'eine', 'einer', 'einem', 'einen',
  'mit', 'auf', 'nach', 'zu', 'im', 'in', 'von', 'als', 'auch', 'es',
  'an', 'er', 'hat', 'haben', 'dass', 'wenn', 'aber', 'hier', 'bitte',
  'danke', 'guten', 'tag', 'morgen', 'abend', 'wie', 'geht', 'schon',
  'noch', 'nur', 'kann', 'können', 'muss', 'müssen', 'jetzt', 'immer',
  'sehr', 'gut', 'schlecht', 'arbeit', 'zeit', 'heute', 'morgen'
]);

const ENGLISH_COMMON_WORDS = new Set([
  'the', 'and', 'of', 'to', 'in', 'is', 'are', 'that', 'this', 'was',
  'were', 'for', 'it', 'with', 'as', 'on', 'be', 'at', 'by', 'have',
  'has', 'had', 'from', 'or', 'but', 'not', 'what', 'all', 'we', 'when',
  'your', 'you', 'can', 'could', 'there', 'their', 'which', 'do', 'how',
  'will', 'would', 'about', 'out', 'many', 'then', 'them', 'these', 'so',
  'some', 'time', 'make', 'like', 'into', 'just', 'know', 'take', 'person',
  'see', 'come', 'think', 'look', 'want', 'give', 'first', 'new', 'good'
]);

/**
 * Robust on-device offline language detector for RU, EN, DE.
 */
export function detectLanguageOffline(text: string): Language | 'und' {
  if (!text || text.trim().length === 0) return 'und';

  const clean = text.trim();

  // 1. Cyrillic check
  let cyrillicCount = 0;
  let latinCount = 0;

  for (let i = 0; i < clean.length; i++) {
    const code = clean.charCodeAt(i);
    // Cyrillic range: \u0400 to \u04FF
    if (code >= 0x0400 && code <= 0x04ff) {
      cyrillicCount++;
    } else if ((code >= 65 && code <= 90) || (code >= 97 && code <= 122)) {
      latinCount++;
    }
  }

  if (cyrillicCount > 0 && cyrillicCount >= latinCount) {
    return 'ru';
  }

  // If there are distinctive German umlauts or 'ß'
  if (GERMAN_SPECIFIC_CHARS.test(clean)) {
    return 'de';
  }

  // Tokenize words
  const words = clean
    .toLowerCase()
    .replace(/[^\p{L}\s]/gu, ' ')
    .split(/\s+/)
    .filter((w) => w.length > 1);

  if (words.length === 0) {
    if (cyrillicCount > 0) return 'ru';
    if (latinCount > 0) return 'en';
    return 'und';
  }

  let deScore = 0;
  let enScore = 0;

  for (const word of words) {
    if (GERMAN_COMMON_WORDS.has(word)) deScore += 2;
    if (ENGLISH_COMMON_WORDS.has(word)) enScore += 2;

    // German capitalization patterns (nouns capitalized in middle of text)
    // Common German prefixes/suffixes
    if (word.endsWith('ung') || word.endsWith('keit') || word.endsWith('lich') || word.endsWith('schaft')) {
      deScore += 1.5;
    }
    if (word.endsWith('tion') || word.endsWith('ing') || word.endsWith('ness') || word.endsWith('able')) {
      enScore += 1;
    }
  }

  if (deScore > enScore && deScore > 0) {
    return 'de';
  }

  if (enScore >= deScore && enScore > 0) {
    return 'en';
  }

  // Fallback heuristic based on character counts
  if (cyrillicCount > 0) return 'ru';
  if (latinCount > 0) return 'en';

  return 'und';
}

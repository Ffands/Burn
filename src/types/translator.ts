export type Language = 'ru' | 'en' | 'de';

export interface BoundingBox {
  x: number;
  y: number;
  width: number;
  height: number;
}

export type Corner = 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right';

export interface CornerInfo {
  corner: Corner;
  x: number;
  y: number;
  distanceToCenter: number;
}

export interface OcrRawItem {
  id: string;
  text: string;
  box: BoundingBox;
  confidence?: number;
}

export interface TranslatedBlock {
  id: string;
  originalText: string;
  translatedText: string;
  detectedLang: Language | 'und';
  targetLang: Language;
  box: BoundingBox;
  nearestCorner: CornerInfo;
  isSkippedSameLang: boolean;
  lineCount: number;
}

export interface LanguagePack {
  id: Language;
  name: string;
  nativeName: string;
  sizeMb: number;
  isBundled: boolean;
  isEnabled: boolean;
}

export interface AppSettings {
  targetLang: Language;
  showCenterGuides: boolean;
  viewMode: 'fullscreen' | 'sheet';
  groupingTightness: number; // 0.8 to 1.8 multiplier
  autoRunOnLoad: boolean;
}

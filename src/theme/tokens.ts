import { ColorTag } from '../types';

export const HAVN_COLORS = {
  sage: '#516351',
  clay: '#9A5637',
  amber: '#8A6B22',
  slate: '#7E929A',
  sand: '#A89878',
  plum: '#6B5F7A',
};

export const AVATAR_COLORS = [
  '#516351', // sage
  '#9A5637', // clay
  '#8A6B22', // amber
  '#7E929A', // slate
  '#A89878', // sand
  '#6B5F7A', // plum
];

export const COLOR_TAGS: ColorTag[] = ['sage', 'clay', 'amber', 'slate', 'sand'];

export function medAccent(tag: string | ColorTag): string {
  switch (tag.toLowerCase()) {
    case 'clay':
      return HAVN_COLORS.clay;
    case 'amber':
      return HAVN_COLORS.amber;
    case 'slate':
      return HAVN_COLORS.slate;
    case 'sand':
      return HAVN_COLORS.sand;
    case 'plum':
      return HAVN_COLORS.plum;
    case 'sage':
    default:
      return HAVN_COLORS.sage;
  }
}

export interface ThemeTokens {
  isDark: boolean;
  canvas: string;
  surface: string;
  surfaceSunken: string;
  surfaceRaised: string;
  hairline: string;
  textPrimary: string;
  textSecondary: string;
  textTertiary: string;
  accent: string;
  accentSoft: string;
  onAccent: string;
  warning: string;
  warningSoft: string;
  onWarningSoft: string;
  danger: string;
  dangerSoft: string;
  onDangerSoft: string;
}

export const lightTokens: ThemeTokens = {
  isDark: false,
  canvas: '#FBFBFA',
  surface: '#F4F4F0',
  surfaceSunken: '#ECEBE4',
  surfaceRaised: '#FFFFFF',
  hairline: '#E2E1D9',
  textPrimary: '#141613',
  textSecondary: '#5E645A',
  textTertiary: '#8C9287',
  accent: '#516351',
  accentSoft: '#E6ECE5',
  onAccent: '#FFFFFF',
  warning: '#8A6B22',
  warningSoft: '#F6EEDB',
  onWarningSoft: '#544111',
  danger: '#9A5637',
  dangerSoft: '#F8E8E2',
  onDangerSoft: '#682D16',
};

export const darkTokens: ThemeTokens = {
  isDark: true,
  canvas: '#141613',
  surface: '#1C1F1A',
  surfaceSunken: '#171A15',
  surfaceRaised: '#242822',
  hairline: '#292E26',
  textPrimary: '#EDEDEA',
  textSecondary: '#A3A89F',
  textTertiary: '#73796E',
  accent: '#7B947B',
  accentSoft: '#222B22',
  onAccent: '#0E130E',
  warning: '#D1AC54',
  warningSoft: '#2D2614',
  onWarningSoft: '#F2D788',
  danger: '#D97A52',
  dangerSoft: '#2F1B14',
  onDangerSoft: '#F2B59D',
};

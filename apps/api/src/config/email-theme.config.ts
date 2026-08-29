import { EmailThemeOptions } from "@nabarun-ngo/nestjs-shared-correspondence";

/**
 * Branding handed to the correspondence base email layout.
 *
 * Mirrors the web apps' design tokens (system fonts, brand red `#e74c3c` with
 * the amber `#f39c12` gradient, orange accent `#f97316`, slate neutrals and
 * the `#2c3e50` dark band). No remote font or logo URLs — clients must not
 * fetch external assets.
 */
export const buildEmailTheme = (): EmailThemeOptions => ({
    fontUrl: '',
    fontFamily: "'Segoe UI', Roboto, Helvetica, Arial, sans-serif",
    headingFontFamily: "'Segoe UI', Roboto, Helvetica, Arial, sans-serif",
    baseFontSize: '15px',

    pageBackground: '#f1f5f9',
    surfaceBackground: '#ffffff',
    subtleBackground: '#f8fafc',
    borderColor: '#e2e8f0',
    cardRadius: '16px',
    cardShadow: '0 4px 12px rgba(15, 23, 42, 0.08)',

    textColor: '#334155',
    mutedTextColor: '#64748b',
    headingColor: '#2c3e50',
    linkColor: '#e74c3c',
    primaryColor: '#e74c3c',

    logoUrl: '',
    logoAlt: 'Nabarun',
    logoWidth: '180',

    headerBackground: 'linear-gradient(135deg, #e74c3c 0%, #f39c12 100%)',
    headerFallbackColor: '#e74c3c',
    headerTextColor: '#ffffff',
    headerSubTextColor: 'rgba(255, 255, 255, 0.92)',

    highlightBackground: '#f8fafc',
    highlightBorderColor: '#e74c3c',

    noticeBackground: '#fff7ed',
    noticeBorderColor: '#f97316',
    noticeTextColor: '#9a3412',

    buttonBackground: 'linear-gradient(135deg, #e74c3c 0%, #f39c12 100%)',
    buttonFallbackColor: '#e74c3c',
    buttonTextColor: '#ffffff',
    buttonRadius: '12px',
    buttonShadow: '0 4px 12px rgba(231, 76, 60, 0.28)',

    footerPanelBackground: 'linear-gradient(135deg, #2c3e50 0%, #1a252f 100%)',
    footerPanelFallbackColor: '#2c3e50',
    footerPanelTextColor: '#ffffff',
    footerPanelDividerColor: 'rgba(255, 255, 255, 0.45)',

    footerBackground: '#f1f5f9',
    footerTextColor: '#64748b',
});

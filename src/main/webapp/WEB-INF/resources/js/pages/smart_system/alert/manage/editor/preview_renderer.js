/**
 * TemplatePreviewRenderer: Resolves template placeholder variables dynamically
 * based on strategy-specific API metadata or default mockup fallbacks.
 */
export const TemplatePreviewRenderer = {
  /**
   * Interpolate raw template text with strategy token values.
   * @param {string} templateText - Raw string containing placeholders.
   * @param {Array<{id: string, label: string, value: string}>} tokens - Active tokens.
   * @returns {string} Fully compiled text.
   */
  render(templateText, tokens = []) {
    if (!templateText) return '';

    const tokenMap = new Map(tokens.map(t => [t.id, t]));

    return templateText.replace(/\{\{\s*([a-zA-Z0-9_.[\]]+)\s*\}\}/g, (match, tokenId) => {
      const cleanId = tokenId.trim();
      const token = tokenMap.get(cleanId);
      return token && token.value !== undefined ? token.value : `[${cleanId}]`;
    });
  }
};

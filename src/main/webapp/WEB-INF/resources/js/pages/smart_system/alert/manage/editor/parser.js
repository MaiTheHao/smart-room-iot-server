/**
 * TemplateParser: Handles conversions between raw string template format and token arrays / visual editor DOM structure.
 * Purely functional and independent of presentation styling.
 */
export const TemplateParser = {
  /**
   * Parse a raw template string containing `{{token}}` placeholders.
   * Supports dynamic white spaces inside the curly braces.
   * @param {string} text - Raw string template.
   * @returns {Array<{type: 'text'|'token', value: string}>} Array of structured token descriptions.
   */
  parse(text) {
    const result = [];
    if (!text) return result;

    // Matches placeholders like {{ rule_name }}, {{cond0_value}} with optional spaces
    const regex = /\{\{\s*([a-zA-Z0-9_.[\]]+)\s*\}\}/g;
    let lastIndex = 0;
    let match;

    while ((match = regex.exec(text)) !== null) {
      const textBefore = text.slice(lastIndex, match.index);
      if (textBefore) {
        result.push({ type: 'text', value: textBefore });
      }
      result.push({ type: 'token', value: match[1].trim() });
      lastIndex = regex.lastIndex;
    }

    const textAfter = text.slice(lastIndex);
    if (textAfter) {
      result.push({ type: 'text', value: textAfter });
    }

    return result;
  },

  /**
   * Serializes the visual editor DOM nodes into a clean template string with {{token}} tokens.
   * Uses a safe DOM tree walker instead of string replacement or innerHTML regex.
   * @param {HTMLElement} editorEl - The contenteditable editor root element.
   * @returns {string} String template format.
   */
  serialize(editorEl) {
    let text = '';
    const walk = (node) => {
      if (node.nodeType === Node.TEXT_NODE) {
        text += node.textContent.replace(/\u200B/g, '');
      } else if (node.nodeType === Node.ELEMENT_NODE) {
        if (node.classList.contains('template-token')) {
          const tokenVal = node.getAttribute('data-token');
          text += `{{${tokenVal}}}`;
        } else if (node.tagName === 'BR') {
          text += '\n';
        } else {
          node.childNodes.forEach(walk);
        }
      }
    };
    editorEl.childNodes.forEach(walk);
    return text;
  }
};

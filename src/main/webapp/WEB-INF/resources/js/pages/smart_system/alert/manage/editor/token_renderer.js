/**
 * TokenRenderer: Manages visual markup creation for template tags (badges/chips).
 */
export const TokenRenderer = {
  /**
   * Generates a styled, non-editable span element representing a template variable.
   * @param {string} tokenId - Underlying identifier (e.g. 'rule_name').
   * @param {string} label - Friendly label displayed to user (e.g. 'Rule Name').
   * @returns {HTMLSpanElement} Visual element to insert in contenteditable.
   */
  createBadge(tokenId, label) {
    const badge = document.createElement('span');
    badge.className = 'template-token badge bg-primary-subtle text-primary border border-primary-subtle px-2 py-1 mx-1 align-middle';
    badge.setAttribute('contenteditable', 'false');
    badge.setAttribute('data-token', tokenId);
    badge.setAttribute('data-type', 'variable');
    badge.textContent = label;
    return badge;
  }
};

/**
 * SelectionManager: Tracks selection ranges safely within the contenteditable element
 * and handles node insertion without relying on deprecated APIs.
 */
export class SelectionManager {
  /**
   * @param {HTMLElement} editorEl - The contenteditable editor root element.
   */
  constructor(editorEl) {
    this.editorEl = editorEl;
    this.savedRange = null;
    this._init();
  }

  _init() {
    // Listen to selection changes in the document globally.
    // We filter the saved range to make sure it belongs inside the editor boundaries.
    document.addEventListener('selectionchange', () => {
      const sel = window.getSelection();
      if (sel.rangeCount > 0) {
        const range = sel.getRangeAt(0);
        if (this.editorEl.contains(range.commonAncestorContainer)) {
          this.savedRange = range.cloneRange();
        }
      }
    });
  }

  /**
   * Safely restores the last saved range, or focuses at the end if none exists.
   */
  restore() {
    const sel = window.getSelection();
    sel.removeAllRanges();
    if (this.savedRange) {
      sel.addRange(this.savedRange);
    } else {
      this.editorEl.focus();
      const range = document.createRange();
      range.selectNodeContents(this.editorEl);
      range.collapse(false); // Move caret to the end
      sel.addRange(range);
      this.savedRange = range;
    }
  }

  /**
   * Inserts a node at the current caret position.
   * @param {Node} node - The DOM node to insert.
   */
  insertNode(node) {
    this.restore();
    const range = this.savedRange;
    if (!range) return;

    range.deleteContents();
    range.insertNode(node);

    let focusNode = node;
    // If it's a template token, append a zero-width space (\u200B) text node right after it
    if (node.nodeType === Node.ELEMENT_NODE && node.classList.contains('template-token')) {
      const spacer = document.createTextNode('\u200B');
      node.parentNode.insertBefore(spacer, node.nextSibling);
      focusNode = spacer;
    }

    // Reposition the selection cursor inside/after the inserted node
    const newRange = document.createRange();
    if (focusNode.nodeType === Node.TEXT_NODE) {
      newRange.setStart(focusNode, 1);
      newRange.setEnd(focusNode, 1);
    } else {
      newRange.setStartAfter(focusNode);
      newRange.setEndAfter(focusNode);
    }

    const sel = window.getSelection();
    sel.removeAllRanges();
    sel.addRange(newRange);
    this.savedRange = newRange;
  }
}

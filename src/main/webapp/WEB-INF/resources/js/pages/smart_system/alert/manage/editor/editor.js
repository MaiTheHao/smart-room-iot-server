import { TemplateParser } from './parser.js';
import { TokenRenderer } from './token_renderer.js';
import { SelectionManager } from './selection_manager.js';
import { TemplatePreviewRenderer } from './preview_renderer.js';

/**
 * TemplateEditor: Orchestrates the contenteditable text field, selection manager,
 * dynamic variable buttons toolbar, and live preview rendering.
 */
export class TemplateEditor {
  /**
   * @param {Object} config
   * @param {HTMLElement} config.editorEl - contenteditable element.
   * @param {HTMLTextAreaElement} config.textareaEl - Hidden textarea (Source of Truth).
   * @param {HTMLElement} config.previewEl - Preview display block.
   * @param {HTMLElement} config.variablesContainerEl - Variable button list container.
   */
  constructor({ editorEl, textareaEl, previewEl, variablesContainerEl }) {
    this.editorEl = editorEl;
    this.textareaEl = textareaEl;
    this.previewEl = previewEl;
    this.variablesContainerEl = variablesContainerEl;

    this.selectionManager = new SelectionManager(this.editorEl);

    this.currentTokens = [];

    this._bindEvents();
  }

  _bindEvents() {
    const handleInput = () => {
      this.syncToTextarea();
      this.updatePreview();
    };

    this.editorEl.addEventListener('input', handleInput);

    // Standard clipboard paste API avoiding deprecated execCommand
    this.editorEl.addEventListener('paste', (e) => {
      e.preventDefault();
      const text = e.clipboardData.getData('text/plain');
      if (text) {
        const textNode = document.createTextNode(text);
        this.selectionManager.insertNode(textNode);
        handleInput();
      }
    });
  }

  /**
   * Updates only tokens and preview definitions.  /**
   * Keeps existing text content and cursor selection intact.
   * @param {Array<{id: string, label: string, value: string}>} tokens
   */
  updateTokens(tokens) {
    this.currentTokens = tokens;

    this.renderVariableButtons();
    this.updatePreview();
  }

  /**
   * Re-loads complete template text and parser structures.
   * Called when opening the form modal or loading fresh data.
   * @param {string} templateText
   * @param {Array<{id: string, label: string, value: string}>} tokens
   */
  load(templateText, tokens = []) {
    this.currentTokens = tokens;

    this.renderVariableButtons();

    // Rebuild editor DOM from string parsing
    this.editorEl.replaceChildren();
    const parsedItems = TemplateParser.parse(templateText);
    const tokenMap = new Map(tokens.map(t => [t.id, t]));

    parsedItems.forEach(item => {
      if (item.type === 'text') {
        this.editorEl.appendChild(document.createTextNode(item.value));
      } else if (item.type === 'token') {
        const schema = tokenMap.get(item.value);
        const badge = TokenRenderer.createBadge(item.value, schema ? schema.label : item.value);
        this.editorEl.appendChild(badge);
        this.editorEl.appendChild(document.createTextNode('\u200B'));
      }
    });

    this.syncToTextarea();
    this.updatePreview();
  }

  /**
   * Renders clickable badges for inserting variables.
   */
  renderVariableButtons() {
    this.variablesContainerEl.replaceChildren();
    if (this.currentTokens.length === 0) {
      const placeholder = document.createElement('span');
      placeholder.className = 'text-muted small italic';
      placeholder.textContent = 'No variables available for this namespace.';
      this.variablesContainerEl.appendChild(placeholder);
      return;
    }

    this.currentTokens.forEach(token => {
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'btn btn-outline-secondary btn-sm rounded-pill py-1 px-3 fw-medium transition-all hover-shadow';
      btn.textContent = `+ ${token.label}`;

      // Prevent input blur & selection loss by intercepting mousedown
      btn.addEventListener('mousedown', (e) => {
        e.preventDefault();
      });

      btn.addEventListener('click', () => {
        const badge = TokenRenderer.createBadge(token.id, token.label);
        this.selectionManager.insertNode(badge);
        this.syncToTextarea();
        this.updatePreview();
      });

      this.variablesContainerEl.appendChild(btn);
    });
  }

  /**
   * Serializes visual editor contents back to the hidden textarea (Source of Truth).
   */
  syncToTextarea() {
    const rawText = TemplateParser.serialize(this.editorEl);
    this.textareaEl.value = rawText;
    this.textareaEl.dispatchEvent(new Event('change', { bubbles: true }));
  }

  /**
   * Compiles the text templates dynamically and renders into the live preview block.
   */
  updatePreview() {
    const template = this.textareaEl.value;
    const previewText = TemplatePreviewRenderer.render(
      template,
      this.currentTokens
    );
    this.previewEl.textContent = previewText || 'Live preview will display here...';
  }

  /**
   * Clean up DOM hooks.
   */
  destroy() {
    this.editorEl.replaceChildren();
    this.variablesContainerEl.replaceChildren();
  }
}

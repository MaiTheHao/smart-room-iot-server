/**
 * Base class defining the namespace-specific alert template metadata.
 * @interface
 */
export class BaseStrategy {
  /**
   * Fetch external context data (e.g. Rule detail objects)
   * @param {string|number} sourceId
   * @returns {Promise<any>} Strategy context data.
   */
  async fetchData(sourceId) {
    return null;
  }

  /**
   * Returns list of supported token schemas for this strategy.
   * @param {any} contextData - Context data returned by fetchData.
   * @returns {Array<{id: string, label: string, defaultValue: string}>}
   */
  getTokens(contextData) {
    return [];
  }
}

import { BaseStrategy } from './base_strategy.js';
import { getRuleById, getRuleConditions } from '../../../../../api/rule.api.js';

const EMPTY_RULE = Object.freeze({
  id: '',
  name: '',
  conditions: [],
});

/**
 * Rule Strategy: Fetches rule metadata and defines dynamic condition token schemas.
 */
export class RuleStrategy extends BaseStrategy {
  /**
   * Fetches rule details from API with conditions merged.
   * Uses Null Object Pattern: Returns safe fallback object on failure.
   * @param {string|number} sourceId - The rule ID.
   * @returns {Promise<{id: string|number, name: string, conditions: Array}>} Rule details.
   */
  async fetchData(sourceId) {
    const fallback = { id: sourceId || '', name: '', conditions: [] };
    if (!sourceId) return fallback;

    const [err, res] = await getRuleById(sourceId);
    if (err) {
      console.error('[RuleStrategy] Failed to fetch rule', err);
      return fallback;
    }

    const [, condRes] = await getRuleConditions(sourceId);
    return {
      ...res.data,
      conditions: condRes?.data || [],
    };
  }

  /**
   * Generates dynamic list of tokens based on the fetched rule's conditions.
   * @param {{id: string|number, name: string, conditions: Array}} rule - Rule data.
   * @returns {Array<{id: string, label: string, value: string}>} Available tokens.
   */
  getTokens(rule = EMPTY_RULE) {
    const safeRule = rule || EMPTY_RULE;
    const conditions = Array.isArray(safeRule.conditions) ? safeRule.conditions : [];

    const tokens = [
      { id: 'rule_id', label: 'Rule ID', value: safeRule.id ? String(safeRule.id) : '[Rule ID]' },
      { id: 'rule_name', label: 'Rule Name', value: safeRule.name || '[Rule Name]' },
      { id: 'total_conditions', label: 'Total Conditions', value: String(conditions.length) },
    ];

    conditions.forEach((cond) => {
      const order = cond.sortOrder ?? 0;
      const prop = cond.property || cond.resourceParam?.property || 'Value';
      tokens.push(
        { id: `cond${order}_value`, label: `Condition-${order} Value`, value: `[Sensor: ${prop}]` },
        { id: `cond${order}_threshold`, label: `Condition-${order} Threshold`, value: String(cond.value ?? '') },
        { id: `cond${order}_operator`, label: `Condition-${order} Operator`, value: cond.operator || '' },
      );
    });

    return tokens;
  }
}

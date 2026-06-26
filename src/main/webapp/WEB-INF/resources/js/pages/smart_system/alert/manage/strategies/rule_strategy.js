import { BaseStrategy } from './base_strategy.js';
import { getRuleById } from '../../../../../api/rule.api.js';

/**
 * Rule Strategy: Fetches rule metadata and defines dynamic condition token schemas.
 */
export class RuleStrategy extends BaseStrategy {
  /**
   * Fetches rule details from API.
   * @param {string|number} sourceId - The rule ID.
   * @returns {Promise<any>} Rule details.
   */
  async fetchData(sourceId) {
    if (!sourceId) return null;
    const [err, res] = await getRuleById(sourceId);
    if (err) {
      console.error('[RuleStrategy] Failed to fetch rule', err);
      return null;
    }
    return res.data;
  }

  /**
   * Generates dynamic list of tokens based on the fetched rule's conditions.
   * @param {any} rule - Fetched rule data.
   * @returns {Array<{id: string, label: string, defaultValue: string}>} Available tokens.
   */
  getTokens(rule) {
    const tokens = [
      { id: 'rule_id', label: 'Rule ID', value: rule ? String(rule.id) : '[Rule ID]' },
      { id: 'rule_name', label: 'Rule Name', value: rule ? (rule.name || '') : '[Rule Name]' },
      { id: 'total_conditions', label: 'Total Conditions', value: rule ? String(rule.conditions?.length || 0) : '0' },
    ];

    if (rule && rule.conditions) {
      rule.conditions.forEach((cond) => {
        const order = cond.sortOrder ?? 0;
        tokens.push(
          { id: `cond${order}_value`, label: `Condition-${order} Value`, value: `[Sensor: ${cond.resourceParam?.property ?? 'Value'}]` },
          { id: `cond${order}_threshold`, label: `Condition-${order} Threshold`, value: String(cond.value ?? '') },
          { id: `cond${order}_operator`, label: `Condition-${order} Operator`, value: cond.operator || '' },
        );
      });
    }
    return tokens;
  }
}

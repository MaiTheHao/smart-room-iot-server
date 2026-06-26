import { RuleStrategy } from './rule_strategy.js';
import { GatewayStrategy } from './gateway_strategy.js';
import { SystemStrategy } from './system_strategy.js';

/**
 * Registry mapping alert namespaces to their specific metadata strategies.
 */
export const STRATEGIES = {
  RULE: new RuleStrategy(),
  GATEWAY: new GatewayStrategy(),
  SYSTEM: new SystemStrategy()
};

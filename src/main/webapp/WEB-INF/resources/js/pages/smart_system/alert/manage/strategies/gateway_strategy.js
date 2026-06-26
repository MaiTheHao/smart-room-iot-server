import { BaseStrategy } from './base_strategy.js';

/**
 * Gateway Strategy: Schema for Gateway offline / online alert tokens.
 */
export class GatewayStrategy extends BaseStrategy {
  getTokens() {
    return [];
  }
}

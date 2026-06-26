import { BaseStrategy } from './base_strategy.js';

/**
 * System Strategy: Schema for System exception / status alert tokens.
 */
export class SystemStrategy extends BaseStrategy {
  getTokens() {
    return [];
  }
}

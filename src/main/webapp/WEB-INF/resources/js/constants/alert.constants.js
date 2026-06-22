export const AlertNamespace = {
  RULE: 'RULE',
  GATEWAY: 'GATEWAY',
  SYSTEM: 'SYSTEM',
};

export const Severity = {
  INFO: 'INFO',
  WARNING: 'WARNING',
  CRITICAL: 'CRITICAL',
};

export const AlertStatus = {
  ACTIVE: 'ACTIVE',
  ACKNOWLEDGED: 'ACKNOWLEDGED',
  RESOLVED: 'RESOLVED',
};

export const AlertActionType = {
  TRIGGERED: 'TRIGGERED',
  RE_TRIGGERED: 'RE_TRIGGERED',
  ACKNOWLEDGED: 'ACKNOWLEDGED',
  RESOLVED: 'RESOLVED',
  AUTO_RESOLVED: 'AUTO_RESOLVED',
};

export const AlertActorType = {
  USER: 'USER',
  SYSTEM: 'SYSTEM',
  RULE_ENGINE: 'RULE_ENGINE',
};

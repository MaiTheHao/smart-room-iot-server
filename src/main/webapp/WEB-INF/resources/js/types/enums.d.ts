
export type MetricDomain = 'ENERGY' | 'HEALTH' | 'STATUS';

export type EnergyMetricCategory = 'LIGHT' | 'AIR_CONDITION' | 'FAN' | 'ROOM';

export type ClientType = 'USER' | 'HARDWARE_GATEWAY';

export type DeviceCategory = 'LIGHT' | 'AIR_CONDITION' | 'TEMPERATURE' | 'POWER_CONSUMPTION' | 'FAN';

export type ActuatorPower = 'ON' | 'OFF';

export type ActuatorMode = 'COOL' | 'HEAT' | 'DRY' | 'FAN' | 'AUTO' | 'NORMAL' | 'SLEEP' | 'NATURAL';

export type ActuatorSwing = 'ON' | 'OFF' | 'AUTO' | 'HORIZONTAL' | 'VERTICAL';

export type ActuatorState = 'ON' | 'OFF';

export type RuleDataSource = 'SYSTEM' | 'ROOM' | 'DEVICE' | 'SENSOR';

export type ConditionOperator = '=' | '!=' | '>' | '<' | '>=' | '<=';

export type ConditionLogic = 'AND' | 'OR';

export type JobTargetType = 'LIGHT' | 'AIR_CONDITION' | 'FAN';

export type JobActionType = 'ON' | 'OFF';



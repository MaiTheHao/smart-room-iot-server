
type MetricDomain = 'ENERGY' | 'HEALTH';

type EnergyMetricCategory = 'LIGHT' | 'AIR_CONDITION' | 'FAN' | 'ROOM';

type ClientType = 'USER' | 'HARDWARE_GATEWAY';

type DeviceCategory = 'LIGHT' | 'FAN' | 'AIR_CONDITION';

type ActuatorPower = 'ON' | 'OFF';

type ActuatorMode = 'COOL' | 'HEAT' | 'DRY' | 'FAN' | 'AUTO' | 'NORMAL' | 'SLEEP' | 'NATURAL';

type ActuatorSwing = 'ON' | 'OFF';

type ActuatorState = 'ON' | 'OFF';

type RuleDataSource = 'SYSTEM' | 'ROOM' | 'DEVICE' | 'SENSOR';

type ConditionOperator = '=' | '!=' | '>' | '<' | '>=' | '<=';

type ConditionLogic = 'AND' | 'OR';

type JobTargetType = 'LIGHT' | 'AIR_CONDITION' | 'FAN';

type JobActionType = 'ON' | 'OFF';

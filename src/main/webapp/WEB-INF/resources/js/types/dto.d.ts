
interface ClientDto {
    id: number;
    username: string;
    clientType: ClientType;
    ipAddress?: string;
    macAddress?: string;
    avatarUrl?: string;
    lastLoginAt?: string;
    gatewayPassword?: string;
}

interface CreateClientDto {
    username: string;
    password: string;
    clientType: ClientType;
    ipAddress?: string;
    macAddress?: string;
    avatarUrl?: string;
    gatewayPassword?: string;
}

interface UpdateClientDto {
    password?: string;
    clientType?: ClientType;
    ipAddress?: string;
    macAddress?: string;
    avatarUrl?: string;
    gatewayPassword?: string;
}

interface LoginDto {
    username: string;
    password: string;
}

interface JwtResponse {
    token: string;
    type: string; // Usually "Bearer"
    username: string;
    groups: string[]; // User roles/groups
}

interface TemperatureValueDto {
    timestamp: string;
    avgTempC: number;
}

interface EnergyMetricDto {
    timestamp: string;
    voltage?: number;
    current?: number;
    power?: number;
    energy?: number;
    frequency?: number;
    powerFactor?: number;
}

interface UnifiedDeviceDto {
    id: number;
    naturalId: string;
    name: string;
    description?: string;
    isActive: boolean;
    power: ActuatorPower;
    roomId: number;
    deviceControlId: number;
    category: DeviceCategory;
    level?: number; // For Light
    speed?: number; // For Fan (0-9999)
    mode?: ActuatorMode; // For AC/Fan
    swing?: ActuatorSwing; // For AC/Fan
    light?: ActuatorState; // For Fan light
    temperature?: number; // For AC (16-32)
    fanSpeed?: number; // For AC (0-5)
    type?: string; // For Fan (GPIO/IR)
}

interface ControlDeviceDetail {
    parameter: string;
    success: boolean;
    message: string;
}

interface ControlDeviceResult {
    successCount: number;
    totalCount: number;
    details: ControlDeviceDetail[];
}

interface AirConditionControlRequestBody {
    power?: ActuatorPower;
    temperature?: number;
    mode?: ActuatorMode;
    fanSpeed?: number;
    swing?: ActuatorSwing;
}

interface FanControlRequestBody {
    power?: ActuatorPower;
    mode?: ActuatorMode;
    speed?: number;
    swing?: ActuatorSwing;
    light?: ActuatorState;
}

interface LightControlRequestBody {
    power?: ActuatorPower;
    level?: number;
}

interface HealthDeviceDto {
    naturalId: string;
    category: string;
    isActive: boolean;
}

interface HealthDataDto {
    devices: HealthDeviceDto[];
    roomCode?: string;
}

interface HealthCheckResponseDto {
    status: number;
    message: string;
    data?: HealthDataDto;
    timestamp: string;
}

interface FloorDto {
    id: number;
    name: string;
    code: string;
    level: number;
    description?: string;
}

interface CreateFloorDto {
    name: string;
    code: string;
    level: number;
    description?: string;
    langCode?: string;
}

interface UpdateFloorDto {
    name?: string;
    level?: number;
    description?: string;
    langCode?: string;
}

interface RoomDto {
    id: number;
    name: string;
    code: string;
    floorId: number;
    description?: string;
}

interface CreateRoomDto {
    name: string;
    code: string;
    floorId: number;
    description?: string;
    langCode?: string;
}

interface UpdateRoomDto {
    name?: string;
    floorId?: number;
    description?: string;
    langCode?: string;
}

interface SysFunctionDto {
    id: number;
    functionCode: string;
    name: string;
    description?: string;
}

interface CreateSysFunctionDto {
    functionCode: string;
    name: string;
    description?: string;
    langCode?: string;
}

interface UpdateSysFunctionDto {
    name?: string;
    description?: string;
    langCode?: string;
}

interface SysFunctionWithGroupStatusDto {
    id: number;
    functionCode: string;
    name: string;
    description?: string;
    isAssignedToGroup: boolean;
    roleId?: number;
}

interface RuleConditionDto {
    id: number;
    sortOrder: number;
    dataSource: RuleDataSource;
    resourceParam: any;
    operator: ConditionOperator;
    value: string;
    nextLogic?: ConditionLogic;
}

interface RuleActionDto {
    id: number;
    targetDeviceId: number;
    targetDeviceCategory: DeviceCategory;
    actionParams: any;
    executionOrder: number;
}

interface RuleDto {
    id: number;
    name: string;
    priority: number;
    isActive: boolean;
    intervalSeconds: number;
    conditions: RuleConditionDto[];
    actions: RuleActionDto[];
    createdAt: string;
    updatedAt: string;
}

interface CreateRuleConditionDto {
    sortOrder: number;
    dataSource: RuleDataSource;
    resourceParam: any;
    operator: ConditionOperator;
    value: string;
    nextLogic?: ConditionLogic;
}

interface CreateRuleActionDto {
    targetDeviceId: number;
    targetDeviceCategory: DeviceCategory;
    actionParams: any;
    executionOrder: number;
}

interface CreateRuleDto {
    name: string;
    priority: number;
    intervalSeconds: number;
    conditions: CreateRuleConditionDto[];
    actions: CreateRuleActionDto[];
}

interface UpdateRuleConditionDto {
    sortOrder: number;
    dataSource: RuleDataSource;
    resourceParam: any;
    operator: ConditionOperator;
    value: string;
    nextLogic?: ConditionLogic;
}

interface UpdateRuleActionDto {
    targetDeviceId?: number;
    targetDeviceCategory?: DeviceCategory;
    actionParams?: any;
    executionOrder?: number;
}

interface UpdateRuleDto {
    name?: string;
    priority?: number;
    intervalSeconds?: number;
    isActive?: boolean;
    conditions?: UpdateRuleConditionDto[];
    actions?: UpdateRuleActionDto[];
}

interface AutomationDto {
    id: number;
    name: string;
    cronExpression: string;
    isActive: boolean;
    description?: string;
    createdAt: string;
    updatedAt: string;
}

interface AutomationActionDto {
    id: number;
    automationId: number;
    targetType: JobTargetType;
    targetId: number;
    actionType: JobActionType;
    parameterValue?: string;
    executionOrder: number;
    targetName?: string;
}

interface CreateAutomationDto {
    name: string;
    cronExpression: string;
    isActive?: boolean;
    description?: string;
}

interface UpdateAutomationDto {
    name?: string;
    cronExpression?: string;
    isActive?: boolean;
    description?: string;
}

interface CreateAutomationActionDto {
    targetType: JobTargetType;
    targetId: number;
    actionType: JobActionType;
    parameterValue?: string;
    executionOrder: number;
}

interface BatchOperationResultDto {
    successCount: number;
    failedCount: number;
    skippedCount: number;
    message: string;
}

interface SysGroupDto {
    id: number;
    groupCode: string;
    name: string;
    description?: string;
}

interface CreateSysGroupDto {
    groupCode: string;
    name: string;
    description?: string;
    langCode?: string;
}

interface UpdateSysGroupDto {
    name?: string;
    description?: string;
    langCode?: string;
}

interface SysGroupWithClientStatusDto {
    id: number;
    groupCode: string;
    name: string;
    description?: string;
    isAssignedToClient: boolean;
}

interface UpdateAutomationActionDto {
    targetType: JobTargetType;
    targetId: number;
    actionType: JobActionType;
    parameterValue?: string;
    executionOrder?: number;
}

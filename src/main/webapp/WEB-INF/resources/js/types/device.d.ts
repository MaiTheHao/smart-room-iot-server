import { ActuatorPower, DeviceCategory, ActuatorMode, ActuatorSwing, ActuatorState } from './enums';

export interface UnifiedDeviceDto {
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

export interface ControlDeviceDetail {
  parameter: string;
  success: boolean;
  message: string;
}

export interface ControlDeviceResult {
  successCount: number;
  totalCount: number;
  details: ControlDeviceDetail[];
}

export interface AirConditionControlRequestBody {
  power?: ActuatorPower;
  temperature?: number;
  mode?: ActuatorMode;
  fanSpeed?: number;
  swing?: ActuatorSwing;
}

export interface FanControlRequestBody {
  power?: ActuatorPower;
  mode?: ActuatorMode;
  speed?: number;
  swing?: ActuatorSwing;
  light?: ActuatorPower; // matches java ActuatorPower
}

export interface LightControlRequestBody {
  power?: ActuatorPower;
  level?: number;
}

